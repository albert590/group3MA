package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtpCode;
    private DatabaseReference mDatabase;
    private String email, phone, userType, fullName, admissionNumber;
    private String generatedOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();

        email = getIntent().getStringExtra("email");
        phone = getIntent().getStringExtra("phone");
        userType = getIntent().getStringExtra("userType");
        fullName = getIntent().getStringExtra("fullName");
        admissionNumber = getIntent().getStringExtra("admissionNumber");

        etOtpCode = findViewById(R.id.etOtpCode);
        Button btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        if (savedInstanceState != null) {
            generatedOtp = savedInstanceState.getString("generatedOtp");
        }

        if (generatedOtp == null) {
            generateAndSendOtp();
        } else {
            // Restore UI message for admin/user
            updateOtpUi();
        }

        btnVerifyOtp.setOnClickListener(v -> {
            String enteredOtp = etOtpCode.getText().toString().trim();
            if (!TextUtils.isEmpty(enteredOtp) && enteredOtp.equals(generatedOtp)) {
                proceedToLogin();
            } else {
                Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("generatedOtp", generatedOtp);
    }

    private void updateOtpUi() {
        TextView tvSubtitle = findViewById(R.id.tvOtpSubtitle);
        if ("admin".equals(userType)) {
            if (tvSubtitle != null) {
                tvSubtitle.setText("ADMIN ACCESS: Your Verification Code is " + generatedOtp);
                tvSubtitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } else {
            if (tvSubtitle != null) {
                tvSubtitle.setText("Please enter the 6-digit code sent to " + email);
            }
        }
    }

    private void generateAndSendOtp() {
        // Generate a 6-digit OTP
        Random random = new Random();
        generatedOtp = String.format(Locale.getDefault(), "%06d", random.nextInt(1000000));

        // Store it in Firebase
        String sanitizedEmail = email.replace(".", ",");
        mDatabase.child("otps").child(sanitizedEmail).setValue(generatedOtp);
        
        updateOtpUi();

        if ("admin".equals(userType)) {
            Toast.makeText(this, "Admin OTP: " + generatedOtp, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "OTP: " + generatedOtp, Toast.LENGTH_LONG).show();
            sendOtpEmail(email, generatedOtp);
        }
    }

    private void sendOtpEmail(String email, String otp) {
        String subject = "Your Verification Code - Hostel Application";
        String body = "Dear " + (fullName != null ? fullName : "User") + ",\n\n" +
                "Your OTP for login is: " + otp + "\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Thank you,\nHostel Application Team";

        // This sends the email in the background without user intervention
        new EmailSender(email, subject, body).execute();
    }

    private void proceedToLogin() {
        String role = getIntent().getStringExtra("role");
        if (role == null) role = "student";

        UserSession session = new UserSession(this);
        session.createLoginSession(email, phone, userType, fullName, admissionNumber, role);

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        SystemLog log = new SystemLog(email, userType, timestamp, "Login Successful (OTP Verified)");
        mDatabase.child("system_logs").push().setValue(log);

        String welcomeMessage = "Login Successful. Welcome back, " + (fullName != null ? fullName : "User") + "!";
        Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show();
        
        Intent intent;
        if ("admin".equals(userType)) {
            intent = new Intent(this, Admin.class);
        } else {
            intent = new Intent(this, HomeDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
