package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        etUsername = findViewById(R.id.etAdminUsername);
        etPassword = findViewById(R.id.etAdminPassword);
        Button btnLogin;
        btnLogin = findViewById(R.id.btnAdminLogin);

        // Using a slightly more robust way to get the database reference
        databaseReference = FirebaseDatabase
                .getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com")
                .getReference();

        // Sign in anonymously to bypass "Permission Denied" if rules require auth
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously();
        }

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Enter Username");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Enter Password");
                return;
            }

            loginAdmin(username, password);
        });
    }

    private void loginAdmin(String username, String password) {
        databaseReference.child("admins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AdminLoginActivity.this, "Database Connection Error: Admin data not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                    String dbUsername = "";
                    String dbPassword = "";
                    // Default to admin
                    String phone = "";

                    // Look through every single field in the admin node to find data
                    for (DataSnapshot child : adminSnapshot.getChildren()) {
                        String key = child.getKey();
                        if (key == null) continue;
                        
                        String keyLower = key.toLowerCase();
                        Object val = child.getValue();
                        String valStr = (val == null) ? "" : String.valueOf(val).trim();

                        if (keyLower.contains("username") || keyLower.contains("name")) {
                            dbUsername = valStr;
                        } else if (keyLower.contains("password") || keyLower.contains("pass")) {
                            dbPassword = valStr;
                            // Clean up numeric passwords (e.g. 12345.0 -> 12345)
                            if (dbPassword.endsWith(".0")) {
                                dbPassword = dbPassword.substring(0, dbPassword.length() - 2);
                            }
                        } else if (keyLower.contains("role")) {
                        } else if (keyLower.contains("phone")) {
                            phone = valStr;
                        }
                    }

                    // Check if input matches username OR the node ID
                    String nodeKey = adminSnapshot.getKey();
                    if (username.equalsIgnoreCase(dbUsername) || (username.equalsIgnoreCase(nodeKey))) {

                        // Verify Password
                        boolean isCorrect = false;
                        if (!dbPassword.isEmpty()) {
                            // Support BCrypt or plain text
                            isCorrect = PasswordUtil.checkPassword(password, dbPassword) || password.equals(dbPassword);
                        }

                        if (isCorrect) {
                            Toast.makeText(AdminLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            
                            Intent intent = new Intent(AdminLoginActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", username);
                            intent.putExtra("phone", phone);
                            intent.putExtra("userType", "admin");
                            intent.putExtra("fullName", !dbUsername.isEmpty() ? dbUsername : username);
                            intent.putExtra("role", "admin");
                            
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AdminLoginActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                }

                Toast.makeText(AdminLoginActivity.this, "Account '" + username + "' not found in admins list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
