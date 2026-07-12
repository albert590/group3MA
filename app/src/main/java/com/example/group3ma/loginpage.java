package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class loginpage extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String TAG = "LOGIN_PAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);
        EdgeToEdge.enable(this);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLoginAction);
        Button btnForgotPassword = findViewById(R.id.btnForgotPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        btnForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Resetting password...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
        
        btnRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Going to Register...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, registerpage.class));
        });
        
        btnAdminLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Admin Login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminLoginActivity.class));
        });
    }

    private void loginUser(String email, String password) {
        Log.d(TAG, "Attempting login for: " + email);
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase Auth login successful");
                    fetchUserData(email);
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Log.w(TAG, "Firebase Auth login failed: " + error);
                    SecurityMonitor.recordFailedAttempt(loginpage.this, email, "student");
                    checkDatabaseForOldUser(email, password);
                }
            });
    }

    private void checkDatabaseForOldUser(String email, String password) {
        Log.d(TAG, "Checking database for old user: " + email);
        String sanitizedEmail = email.replace(".", ",");
        mDatabase.child("users").child(sanitizedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "User found in database");
                    User user = snapshot.getValue(User.class);
                    if (user == null) {
                        Log.e(TAG, "User data is null");
                        Toast.makeText(loginpage.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isValid;
                    boolean needsMigration = false;

                    if (user.password != null && user.password.startsWith("$2")) {
                        isValid = PasswordUtil.checkPassword(password, user.password);
                    } else {
                        isValid = password.equals(user.password);
                        if (isValid) needsMigration = true;
                    }

                    if (isValid) {
                        SecurityMonitor.resetAttempts();
                        if (needsMigration) {
                            String hashed = PasswordUtil.hashPassword(password);
                            mDatabase.child("users").child(sanitizedEmail).child("password").setValue(hashed);
                        }
                        syncUserToAuth(email, password, user);
                    } else {
                        SecurityMonitor.recordFailedAttempt(loginpage.this, email, "student");
                        Toast.makeText(loginpage.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    SecurityMonitor.recordFailedAttempt(loginpage.this, email, "student");
                    Toast.makeText(loginpage.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                 Log.e(TAG, "Database error: " + error.getMessage());
                 Toast.makeText(loginpage.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncUserToAuth(String email, String password, User user) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                navigateToOtp(email, user);
            } else if (task.getException() != null && task.getException().getMessage().contains("already in use")) {
                // User already in Auth, but we need to sign in to establish the session
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(loginTask -> {
                    if (loginTask.isSuccessful()) {
                        navigateToOtp(email, user);
                    } else {
                        Toast.makeText(this, "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Authentication sync failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToOtp(String email, User user) {
        SecurityMonitor.resetAttempts();
        Log.d(TAG, "Navigating to OTP for: " + email);
        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("phone", user.phoneNumber);
        intent.putExtra("userType", "student");
        intent.putExtra("fullName", user.fullName);
        intent.putExtra("admissionNumber", user.admissionNumber);
        startActivity(intent);
        Log.d(TAG, "OtpVerificationActivity started");
        finish();
    }

    private void fetchUserData(String email) {
        String sanitizedEmail = email.replace(".", ",");
        Log.d(TAG, "Fetching user data for: " + sanitizedEmail);
        mDatabase.child("users").child(sanitizedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "fetchUserData onDataChange: exists=" + snapshot.exists());
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        Log.d(TAG, "User data fetched successfully: " + user.fullName);
                        navigateToOtp(email, user);
                    } else {
                        Log.e(TAG, "User data is null even though snapshot exists");
                        Toast.makeText(loginpage.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "User profile not found for: " + sanitizedEmail);
                    Toast.makeText(loginpage.this, "User profile not found.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "fetchUserData onCancelled: " + error.getMessage());
                Toast.makeText(loginpage.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
