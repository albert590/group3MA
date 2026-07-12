package com.example.group3ma;

import android.os.Bundle;
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

public class AdminMaintenanceActivity extends AppCompatActivity {

    private RecyclerView rvMaintenance;
    private DatabaseReference mDatabase;
    private List<MaintenanceReport> reportList;
    private AdminMaintenanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_maintenance);

        rvMaintenance = findViewById(R.id.rvAdminMaintenance);
        rvMaintenance.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        adapter = new AdminMaintenanceAdapter(reportList);
        rvMaintenance.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference().child("maintenance_reports");

        fetchReports();
    }

    private void fetchReports() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MaintenanceReport report = ds.getValue(MaintenanceReport.class);
                    if (report != null) reportList.add(report);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    Toast.makeText(AdminMaintenanceActivity.this, "RULES ERROR: Admin NOT allowed to see maintenance reports.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminMaintenanceActivity.this, "Error fetching reports: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
