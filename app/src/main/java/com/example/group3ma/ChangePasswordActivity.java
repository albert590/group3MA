package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        
        // Get email from intent or session
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            userEmail = new UserSession(this).getEmail();
        }

        TextInputEditText etNewPassword = findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        Button btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        btnUpdatePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmNewPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Strong password policy
            if (newPassword.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePassword(newPassword);
        });
    }

    private void updatePassword(String newPassword) {
        if (userEmail == null) {
            Toast.makeText(this, "Error: User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Update in Firebase Auth if user is currently signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && userEmail.equals(user.getEmail())) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updateDatabasePassword(newPassword);
                } else {
                    Toast.makeText(this, "Auth Update Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // If not logged in or email mismatch, just update DB
            updateDatabasePassword(newPassword);
        }
    }

    private void updateDatabasePassword(String newPassword) {
        String sanitizedEmail = userEmail.replace(".", ",");
        
        // Secure Password Storage: Hash the new password
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", hashedPassword);
        updates.put("lastPasswordUpdate", System.currentTimeMillis());
        
        mDatabase.child("users").child(sanitizedEmail).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChangePasswordActivity.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(ChangePasswordActivity.this, "Failed to update database profile.", Toast.LENGTH_SHORT).show());
    }
}
