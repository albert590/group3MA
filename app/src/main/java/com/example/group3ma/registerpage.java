package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class registerpage extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerpage);
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

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etAdmissionNumber = findViewById(R.id.etAdmissionNumber);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhoneNumber = findViewById(R.id.etPhoneNumber);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        RadioGroup rgGender = findViewById(R.id.rgGender);
        Spinner spYearOfStudy = findViewById(R.id.spYearOfStudy);
        Button btnRegister = findViewById(R.id.btnRegisterAction);
        Button btnLogin = findViewById(R.id.btnLogin);

        String[] years = {"Select Year of Study", "Year One", "Year Two", "Year Three", "Year Four", "Year Five"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYearOfStudy.setAdapter(adapter);

        // Biometrics is optional, proceed directly to keep it simple and responsive
        // setupBiometric(); 

        btnRegister.setOnClickListener(v -> {
            Log.d("REGISTER_PAGE", "Register button clicked");
            Toast.makeText(this, "Processing Registration...", Toast.LENGTH_SHORT).show();
            
            String fullName = etFullName.getText().toString().trim();
            String admissionNumber = etAdmissionNumber.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            if (!isValidRegistration(fullName, admissionNumber, email, phoneNumber, password, confirmPassword, rgGender, spYearOfStudy)) {
                Log.d("REGISTER_PAGE", "Registration validation failed");
                return;
            }

            // Skip biometric check for now to fix unresponsiveness
            proceedWithRegistration();
        });

        btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Going to Login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(registerpage.this, loginpage.class));
        });
    }

    private void setupBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(registerpage.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                proceedWithRegistration(); // Fallback
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                proceedWithRegistration();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Biometric failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm Registration")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
    }

    private void proceedWithRegistration() {
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etAdmissionNumber = findViewById(R.id.etAdmissionNumber);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhoneNumber = findViewById(R.id.etPhoneNumber);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        RadioGroup rgGender = findViewById(R.id.rgGender);
        Spinner spYearOfStudy = findViewById(R.id.spYearOfStudy);

        String fullName = etFullName.getText().toString().trim();
        String admissionNumber = etAdmissionNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        int checkedId = rgGender.getCheckedRadioButtonId();
        String gender = checkedId != -1 ? ((RadioButton) findViewById(checkedId)).getText().toString() : "Not Specified";
        String yearOfStudy = spYearOfStudy.getSelectedItem().toString();

        registerInFirebase(admissionNumber, fullName, email, phoneNumber, password, gender, yearOfStudy);
    }

    private boolean isValidRegistration(String fullName, String admissionNumber, String email, String phoneNumber, String password, String confirmPassword, RadioGroup rgGender, Spinner spYearOfStudy) {
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(admissionNumber) || TextUtils.isEmpty(email) || 
            TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords mismatch", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (rgGender.getCheckedRadioButtonId() == -1 || spYearOfStudy.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Complete all selections", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (admissionNumber.contains(".") || admissionNumber.contains("#") || admissionNumber.contains("$") || admissionNumber.contains("[") || admissionNumber.contains("]")) {
            Toast.makeText(this, "Admission number cannot contain special characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    private void registerInFirebase(String admissionNumber, String fullName, String email, String phone, String password, String gender, String yearOfStudy) {
        Log.d("REGISTER_PAGE", "Registering in Firebase Auth: " + email);
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d("REGISTER_PAGE", "Firebase Auth registration successful");
                    writeNewUser(admissionNumber, fullName, email, phone, password, gender, yearOfStudy);
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Auth error";
                    Log.w("REGISTER_PAGE", "Firebase Auth registration failed: " + error);
                    if (error != null && error.contains("already in use")) {
                         Toast.makeText(this, "Email already registered. Please login.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Registration Failed: " + error, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void writeNewUser(String admissionNumber, String name, String email, String phone, String password, String gender, String yearOfStudy) {
        String sanitizedEmail = email.replace(".", ",");
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        User user = new User(name, admissionNumber, email, phone, gender, yearOfStudy, hashedPassword);

        mDatabase.child("users").child(sanitizedEmail).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                mDatabase.child("system_logs").push().setValue(new SystemLog(email, "Student", timestamp, "Account Created"));
                
                Toast.makeText(this, "Successfully Registered!", Toast.LENGTH_LONG).show();
                
                // Automatically proceed to OTP verification (Login) after registration
                Intent intent = new Intent(this, OtpVerificationActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("phone", phone);
                intent.putExtra("userType", "student");
                intent.putExtra("fullName", name);
                intent.putExtra("admissionNumber", admissionNumber);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Database Error: User profile could not be created.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
