package com.example.group3ma;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeDashboardActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private View cardBookingProgress;
    private TextView tvProgressHostelName, tvBookingStatus, tvProgressStepDescription;
    private LinearProgressIndicator progressBooking;
    private UserSession session;
    private ValueEventListener killSwitchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homedashbordafterlogin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        session = new UserSession(this);

        // Security Check: Kill Switch
        checkKillSwitch();
        
        // Security Check: Password Update Reminder
        checkPasswordUpdateReminder();

        // Initialize progress card views
        cardBookingProgress = findViewById(R.id.cardBookingProgress);
        tvProgressHostelName = findViewById(R.id.tvProgressHostelName);
        tvBookingStatus = findViewById(R.id.tvBookingStatus);
        tvProgressStepDescription = findViewById(R.id.tvProgressStepDescription);
        progressBooking = findViewById(R.id.progressBooking);

        findViewById(R.id.cardAvailableHostel).setOnClickListener(v -> startActivity(new Intent(this, hostellistpage.class)));
        findViewById(R.id.cardMyBooking).setOnClickListener(v -> startActivity(new Intent(this, mybooking.class)));
        findViewById(R.id.cardFavorites).setOnClickListener(v -> startActivity(new Intent(this, FavoritesActivity.class)));
        findViewById(R.id.cardRoommateMatch).setOnClickListener(v -> startActivity(new Intent(this, RoommateMatchingActivity.class)));
        findViewById(R.id.cardMaintenance).setOnClickListener(v -> startActivity(new Intent(this, MaintenanceActivity.class)));
        findViewById(R.id.cardAI).setOnClickListener(v -> startActivity(new Intent(this, AIChatActivity.class)));
        findViewById(R.id.cardProfile).setOnClickListener(v -> startActivity(new Intent(this, profile.class)));
        findViewById(R.id.cardSupport).setOnClickListener(v -> startActivity(new Intent(this, support.class)));

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            logAction();
            session.logoutUser();
            Toast.makeText(HomeDashboardActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(HomeDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        checkBookingProgress();
    }

    private void checkPasswordUpdateReminder() {
        String email = session.getEmail();
        if (email == null) return;

        String key = email.replace(".", ",");
        mDatabase.child("users").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    long lastUpdate = user.lastPasswordUpdate;
                    long ninetyDaysInMillis = 90L * 24 * 60 * 60 * 1000;
                    if (System.currentTimeMillis() - lastUpdate > ninetyDaysInMillis) {
                        showPasswordChangeDialog();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showPasswordChangeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Security Update")
                .setMessage("Your password is over 90 days old. For your security, please consider changing it.")
                .setPositiveButton("Change Now", (dialog, which) -> {
                    Intent intent = new Intent(HomeDashboardActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void checkKillSwitch() {
        killSwitchListener = mDatabase.child("app_settings").child("is_active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                if (isActive != null && !isActive) {
                    Intent intent = new Intent(HomeDashboardActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (killSwitchListener != null) {
            mDatabase.child("app_settings").child("is_active").removeEventListener(killSwitchListener);
        }
    }

    private void logAction() {
        String email = session.getEmail();
        if (email != null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            SystemLog log = new SystemLog(email, "Student", timestamp, "Logged Out");
            mDatabase.child("system_logs").push().setValue(log);
        }
    }

    private void checkBookingProgress() {
        String userEmail = session.getEmail();

        if (userEmail == null) return;

        Query lastBookingQuery = mDatabase.child("bookings").orderByChild("email").equalTo(userEmail).limitToLast(1);
        lastBookingQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    DataSnapshot bookingSnapshot = snapshot.getChildren().iterator().next();
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (booking != null) {
                        updateProgressUI(booking);
                    } else {
                        cardBookingProgress.setVisibility(View.GONE);
                    }
                } else {
                    cardBookingProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silently fail or log
            }
        });
    }

    private void updateProgressUI(Booking booking) {
        cardBookingProgress.setVisibility(View.VISIBLE);
        tvProgressHostelName.setText(getString(R.string.hostel_progress_format, booking.hostel));
        tvBookingStatus.setText(!TextUtils.isEmpty(booking.status) ? booking.status : getString(R.string.unknown_status));

        int progress;
        String description;

        String status = !TextUtils.isEmpty(booking.status) ? booking.status : "";
        switch (status) {
            case "Pending Payment":
                progress = 25;
                description = getString(R.string.status_pending_payment_desc);
                tvBookingStatus.setTextColor(ContextCompat.getColor(this, R.color.accentAmber));
                break;
            case "Pending Approval":
                progress = 50;
                description = getString(R.string.status_pending_approval_desc);
                tvBookingStatus.setTextColor(ContextCompat.getColor(this, R.color.accentAmber));
                break;
            case "Paid":
                progress = 75;
                description = getString(R.string.status_paid_desc);
                tvBookingStatus.setTextColor(ContextCompat.getColor(this, R.color.status_success));
                break;
            case "Approved":
                progress = 100;
                description = getString(R.string.status_approved_desc);
                tvBookingStatus.setTextColor(ContextCompat.getColor(this, R.color.status_success));
                break;
            case "Declined":
                progress = 100;
                description = getString(R.string.status_declined_desc);
                tvBookingStatus.setTextColor(ContextCompat.getColor(this, R.color.status_error));
                break;
            default:
                progress = 10;
                description = getString(R.string.status_generic_desc, booking.status);
                break;
        }

        progressBooking.setProgress(progress);
        tvProgressStepDescription.setText(description);
    }
}
