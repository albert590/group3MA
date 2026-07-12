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
import java.util.Collections;
import java.util.List;

public class AdminLogsActivity extends AppCompatActivity {

    private RecyclerView rvLogs;
    private SystemLogAdapter adapter;
    private List<SystemLog> logList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_logs);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();

        rvLogs = findViewById(R.id.rvSystemLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));

        logList = new ArrayList<>();
        adapter = new SystemLogAdapter(logList);
        rvLogs.setAdapter(adapter);

        fetchLogs();
    }

    private void fetchLogs() {
        mDatabase.child("system_logs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                logList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    SystemLog log = postSnapshot.getValue(SystemLog.class);
                    if (log != null) {
                        logList.add(log);
                    }
                }
                // Reverse to show latest logs first
                Collections.reverse(logList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    Toast.makeText(AdminLogsActivity.this, "RULES ERROR: Admin NOT allowed to see system logs.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminLogsActivity.this, "Failed to load logs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
