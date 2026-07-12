package com.example.group3ma;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class profile extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView tvNameValue, tvEmailValue, tvPhoneValue, tvAdmissionValue, tvPasswordValue, tvBookingStatusValue;
    private String userType;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        session = new UserSession(this);

        tvNameValue = findViewById(R.id.tvNameValue);
        tvEmailValue = findViewById(R.id.tvEmailValue);
        tvPhoneValue = findViewById(R.id.tvPhoneValue);
        tvAdmissionValue = findViewById(R.id.tvAdmissionValue);
        tvPasswordValue = findViewById(R.id.tvPasswordValue);
        tvBookingStatusValue = findViewById(R.id.tvBookingStatusValue);
        Button btnLogoutProfile = findViewById(R.id.btnLogoutProfile);

        String identifier = session.getEmail(); // This is email for students, username for admins
        userType = session.getUserType();

        if (session.isLoggedIn() && identifier != null) {
            loadProfileData(identifier);
            if ("student".equals(userType)) {
                loadBookingStatus(identifier);
            } else {
                tvBookingStatusValue.setText("N/A (Admin)");
            }
        } else {
            startActivity(new Intent(this, loginpage.class));
            finish();
        }

        btnLogoutProfile.setOnClickListener(v -> {
            logAction("Logged Out");
            session.logoutUser();
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void logAction(String action) {
        String email = session.getEmail();
        if (email != null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            SystemLog log = new SystemLog(email, "admin".equals(userType) ? "Admin" : "Student", timestamp, action);
            mDatabase.child("system_logs").push().setValue(log);
        }
    }

    private void loadProfileData(String identifier) {
        String node = "admin".equals(userType) ? "admins" : "users";
        String key = "admin".equals(userType) ? identifier : identifier.replace(".", ",");

        mDatabase.child(node).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if ("admin".equals(userType)) {
                        AdminUser admin = snapshot.getValue(AdminUser.class);
                        if (admin != null) {
                            tvNameValue.setText(admin.username);
                            tvEmailValue.setText(admin.username + "@admin.com");
                            tvPhoneValue.setText(admin.phoneNumber);
                            tvAdmissionValue.setText("N/A");
                            tvPasswordValue.setText(admin.password);
                        }
                    } else {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            tvNameValue.setText(user.fullName);
                            tvEmailValue.setText(user.email);
                            tvPhoneValue.setText(user.phoneNumber);
                            tvAdmissionValue.setText(user.admissionNumber);
                            tvPasswordValue.setText(user.password);
                        }
                    }
                } else {
                    Toast.makeText(profile.this, "Profile not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    Toast.makeText(profile.this, "PERMISSION DENIED: Change Rules in Firebase!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadBookingStatus(String email) {
        mDatabase.child("bookings").orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot lastBooking = null;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        lastBooking = child;
                    }
                    if (lastBooking != null) {
                        Booking b = lastBooking.getValue(Booking.class);
                        if (b != null) tvBookingStatusValue.setText(b.status);
                    }
                } else {
                    tvBookingStatusValue.setText("No Bookings Found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
