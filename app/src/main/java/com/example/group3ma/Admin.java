package com.example.group3ma;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Admin extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private AdminBookingAdapter adapter;
    private List<Booking> bookingList;
    private List<Booking> filteredList;
    private DatabaseReference mDatabase;
    private TextView tvBookingsCount;
    private EditText etSearchStudent;
    private UserSession session;
    private SwitchCompat swKillSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        session = new UserSession(this);
        if (!session.isLoggedIn() || !"admin".equals(session.getUserType())) {
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();

        // Ensure Admin has a Firebase Auth session (Anonymous if needed) to avoid Permission Denied
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    initializeAdminPanel();
                } else {
                    Toast.makeText(this, "Firebase Connection Error: Database access may be restricted.", Toast.LENGTH_LONG).show();
                    initializeAdminPanel(); // Try anyway
                }
            });
        } else {
            initializeAdminPanel();
        }
    }

    private void initializeAdminPanel() {
        swKillSwitch = findViewById(R.id.swKillSwitch);
        Button btnEmergencyLockdown = findViewById(R.id.btnEmergencyLockdown);
        tvBookingsCount = findViewById(R.id.tvBookingsCount);
        etSearchStudent = findViewById(R.id.etSearchStudent);
        RecyclerView rvBookings = findViewById(R.id.rvAdminBookings);
        
        Button btnViewLogs = findViewById(R.id.btnViewLogs);
        Button btnMaintenance = findViewById(R.id.btnMaintenanceAdmin);

        // All Admins who have access to this activity can see all features
        btnViewLogs.setVisibility(android.view.View.VISIBLE);
        btnMaintenance.setVisibility(android.view.View.VISIBLE);
        tvBookingsCount.setVisibility(android.view.View.VISIBLE);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AdminBookingAdapter(this, filteredList);
        rvBookings.setAdapter(adapter);

        findViewById(R.id.btnAddStudent).setOnClickListener(v -> startActivity(new Intent(Admin.this, registerpage.class)));
        
        findViewById(R.id.btnDeleteStudent).setOnClickListener(v -> {
            String identifier = etSearchStudent.getText().toString().trim();
            if (TextUtils.isEmpty(identifier)) {
                Toast.makeText(this, "Enter full email in search box to delete account", Toast.LENGTH_SHORT).show();
            } else if (!identifier.contains("@")) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                showDeleteConfirmationDialog(identifier);
            }
        });

        findViewById(R.id.btnDeleteHostel).setOnClickListener(v -> {
            String hostelName = etSearchStudent.getText().toString().trim();
            if (TextUtils.isEmpty(hostelName)) {
                Toast.makeText(this, "Enter hostel name in search box to delete", Toast.LENGTH_SHORT).show();
            } else {
                showDeleteHostelConfirmationDialog(hostelName);
            }
        });

        findViewById(R.id.btnAddHostel).setOnClickListener(v -> startActivity(new Intent(Admin.this, AddEditHostelActivity.class)));

        findViewById(R.id.btnEditHostel).setOnClickListener(v -> {
            String hostelName = etSearchStudent.getText().toString().trim();
            if (TextUtils.isEmpty(hostelName)) {
                Toast.makeText(this, "Enter hostel name in search box to edit", Toast.LENGTH_SHORT).show();
            } else {
                findAndEditHostel(hostelName);
            }
        });

        findViewById(R.id.btnManageHostels).setOnClickListener(v -> startActivity(new Intent(Admin.this, ManageHostelsActivity.class)));
        findViewById(R.id.btnViewLogs).setOnClickListener(v -> startActivity(new Intent(Admin.this, AdminLogsActivity.class)));
        
        findViewById(R.id.btnMaintenanceAdmin).setOnClickListener(v -> startActivity(new Intent(Admin.this, AdminMaintenanceActivity.class)));

        findViewById(R.id.btnLogoutAdmin).setOnClickListener(v -> {
            logAction("Logged Out");
            session.logoutUser();
            Intent intent = new Intent(Admin.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBookings(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupKillSwitch();
        
        btnEmergencyLockdown.setOnClickListener(v -> showEmergencyLockdownDialog());

        fetchBookings();
        checkSmsPermission();
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    private void showEmergencyLockdownDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🚨 EMERGENCY LOCKDOWN")
                .setMessage("This will IMMEDIATELY deactivate the entire system and send a security alert. Are you sure?")
                .setPositiveButton("ACTIVATE LOCKDOWN", (dialog, which) -> {
                    SecurityMonitor.triggerSystemLockdown(this, session.getEmail(), "ADMIN_MANUAL");
                    Toast.makeText(this, "SYSTEM DEACTIVATED & ALERT SENT", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setupKillSwitch() {
        mDatabase.child("app_settings").child("is_active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                if (isActive != null) {
                    swKillSwitch.setChecked(isActive);
                    String statusText = isActive ? getString(R.string.status_active) : getString(R.string.status_deactivated);
                    swKillSwitch.setText(getString(R.string.system_status, statusText));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        swKillSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDatabase.child("app_settings").child("is_active").setValue(isChecked);
            logAction(isChecked ? getString(R.string.system_activated) : getString(R.string.system_deactivated));
        });
    }

    private void logAction(String action) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        SystemLog log = new SystemLog(session.getEmail(), "Admin", timestamp, action);
        mDatabase.child("system_logs").push().setValue(log);
    }

    private void fetchBookings() {
        mDatabase.child("bookings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Booking booking = postSnapshot.getValue(Booking.class);
                    if (booking != null) {
                        booking.bookingId = postSnapshot.getKey();
                        bookingList.add(booking);
                    }
                }
                filterBookings(etSearchStudent.getText().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    Toast.makeText(Admin.this, "RULES ERROR: Permission Denied to see bookings.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void filterBookings(String text) {
        filteredList.clear();
        if (TextUtils.isEmpty(text)) {
            filteredList.addAll(bookingList);
        } else {
            for (Booking booking : bookingList) {
                if ((booking.email != null && booking.email.toLowerCase().contains(text.toLowerCase())) || 
                    (booking.hostel != null && booking.hostel.toLowerCase().contains(text.toLowerCase()))) {
                    filteredList.add(booking);
                }
            }
        }
        adapter.notifyDataSetChanged();
        tvBookingsCount.setText(getString(R.string.total_bookings, filteredList.size()));
    }

    private void showDeleteConfirmationDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_student_button))
                .setMessage(getString(R.string.delete_student_confirmation, email))
                .setPositiveButton(getString(R.string.delete_student_button), (dialog, which) -> deleteStudent(email))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteStudent(String email) {
        String sanitizedEmail = email.replace(".", ",");
        mDatabase.child("users").child(sanitizedEmail).removeValue()
                .addOnSuccessListener(aVoid -> {
                    logAction("Deleted student: " + email);
                    Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteHostelConfirmationDialog(String hostelName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hostel")
                .setMessage("Are you sure you want to delete all hostels containing: " + hostelName + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteHostelByName(hostelName))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteHostelByName(String hostelName) {
        mDatabase.child("hostels").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Hostel hostel = postSnapshot.getValue(Hostel.class);
                    if (hostel != null && hostel.getName() != null && 
                        hostel.getName().toLowerCase().contains(hostelName.toLowerCase())) {
                        postSnapshot.getRef().removeValue();
                        logAction("Deleted hostel: " + hostel.getName());
                        found = true;
                    }
                }
                if (found) {
                    Toast.makeText(Admin.this, "Hostel(s) deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Admin.this, "No hostel found with that name", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findAndEditHostel(String hostelName) {
        mDatabase.child("hostels").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Hostel hostel = postSnapshot.getValue(Hostel.class);
                    if (hostel != null && hostel.getName() != null && 
                        hostel.getName().toLowerCase().contains(hostelName.toLowerCase())) {
                        
                        if (hostel.getId() == null) hostel.setId(postSnapshot.getKey());

                        Intent intent = new Intent(Admin.this, AddEditHostelActivity.class);
                        intent.putExtra("HOSTEL_ID", hostel.getId());
                        intent.putExtra("HOSTEL_NAME", hostel.getName());
                        intent.putExtra("HOSTEL_CAPACITY", hostel.getCapacity());
                        intent.putExtra("HOSTEL_PRICE", hostel.getPrice());
                        intent.putExtra("HOSTEL_DESCRIPTION", hostel.getDescription());
                        intent.putExtra("HOSTEL_LATITUDE", hostel.getLatitude());
                        intent.putExtra("HOSTEL_LONGITUDE", hostel.getLongitude());
                        intent.putExtra("HOSTEL_OWNER", hostel.getOwner());
                        startActivity(intent);
                        return;
                    }
                }
                Toast.makeText(Admin.this, "Hostel not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
