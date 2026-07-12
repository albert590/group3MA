package com.example.group3ma;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText etDescription;
    private RecyclerView rvReports;
    private TextView tvCurrentHostel;
    private DatabaseReference mDatabase;
    private UserSession session;
    private String userEmailKey;
    private String activeHostelName = "No active booking";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        session = new UserSession(this);
        String email = session.getEmail();
        if (email == null) {
            finish();
            return;
        }
        userEmailKey = email.replace(".", ",");

        spinnerCategory = findViewById(R.id.spinnerCategory);
        etDescription = findViewById(R.id.etDescription);
        rvReports = findViewById(R.id.rvReports);
        
        // Ensure there is a place to show the hostel name in activity_maintenance.xml or log it
        rvReports.setLayoutManager(new LinearLayoutManager(this));

        setupCategorySpinner();
        fetchActiveBooking();
        loadMyReports();

        findViewById(R.id.btnSubmitReport).setOnClickListener(v -> submitReport());
    }

    private void setupCategorySpinner() {
        String[] categories = {"Water Issue", "Electricity Problem", "Broken Furniture", "Security Concern", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void fetchActiveBooking() {
        mDatabase.child("bookings").orderByChild("email").equalTo(session.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking b = ds.getValue(Booking.class);
                    if (b != null && "Approved".equalsIgnoreCase(b.status)) {
                        activeHostelName = b.hostel;
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void submitReport() {
        String category = spinnerCategory.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(this, "Please describe the issue", Toast.LENGTH_SHORT).show();
            return;
        }

        if (activeHostelName.equals("No active booking")) {
            Toast.makeText(this, "You need an approved booking to report issues", Toast.LENGTH_LONG).show();
            return;
        }

        String reportId = mDatabase.child("maintenance_reports").push().getKey();
        MaintenanceReport report = new MaintenanceReport(reportId, userEmailKey, activeHostelName, category, description);

        if (reportId != null) {
            mDatabase.child("maintenance_reports").child(reportId).setValue(report)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MaintenanceActivity.this, "Report Submitted for " + activeHostelName, Toast.LENGTH_SHORT).show();
                            etDescription.setText("");
                        }
                    });
        }
    }

    private void loadMyReports() {
        mDatabase.child("maintenance_reports").orderByChild("userId").equalTo(userEmailKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<MaintenanceReport> reports = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MaintenanceReport report = ds.getValue(MaintenanceReport.class);
                            if (report != null) reports.add(report);
                        }
                        rvReports.setAdapter(new MaintenanceAdapter(reports));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
