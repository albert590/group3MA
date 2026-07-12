package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class ManageHostelsActivity extends AppCompatActivity {

    private RecyclerView rvAdminHostels;
    private AdminHostelAdapter adapter;
    private List<Hostel> hostelList;
    private DatabaseReference mDatabase;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_hostels);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        session = new UserSession(this);

        rvAdminHostels = findViewById(R.id.rvAdminHostels);
        rvAdminHostels.setLayoutManager(new LinearLayoutManager(this));

        hostelList = new ArrayList<>();
        adapter = new AdminHostelAdapter(hostelList, new AdminHostelAdapter.OnHostelActionListener() {
            @Override
            public void onEdit(Hostel hostel) {
                Intent intent = new Intent(ManageHostelsActivity.this, AddEditHostelActivity.class);
                intent.putExtra("HOSTEL_ID", hostel.getId());
                intent.putExtra("HOSTEL_NAME", hostel.getName());
                intent.putExtra("HOSTEL_CAPACITY", hostel.getCapacity());
                intent.putExtra("HOSTEL_PRICE", hostel.getPrice());
                intent.putExtra("HOSTEL_DESCRIPTION", hostel.getDescription());
                intent.putExtra("HOSTEL_LATITUDE", hostel.getLatitude());
                intent.putExtra("HOSTEL_LONGITUDE", hostel.getLongitude());
                intent.putExtra("HOSTEL_OWNER", hostel.getOwner());
                startActivity(intent);
            }

            @Override
            public void onDelete(Hostel hostel) {
                showDeleteConfirmationDialog(hostel);
            }
        });
        rvAdminHostels.setAdapter(adapter);

        FloatingActionButton fabAddHostel = findViewById(R.id.fabAddHostel);
        fabAddHostel.setOnClickListener(v -> {
            startActivity(new Intent(ManageHostelsActivity.this, AddEditHostelActivity.class));
        });

        // Add a long-press listener on the title to seed default data if needed
        findViewById(R.id.tvManageHostelsTitle).setOnLongClickListener(v -> {
            showSeedDataDialog();
            return true;
        });

        fetchHostels();
    }

    private void logAction(String action) {
        String email = session.getEmail();
        if (email != null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            SystemLog log = new SystemLog(email, "Admin", timestamp, action);
            mDatabase.child("system_logs").push().setValue(log);
        }
    }

    private void fetchHostels() {
        mDatabase.child("hostels").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hostelList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Hostel hostel = postSnapshot.getValue(Hostel.class);
                    if (hostel != null) {
                        if (hostel.getId() == null) {
                            hostel.setId(postSnapshot.getKey());
                        }
                        hostelList.add(hostel);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageHostelsActivity.this, "Failed to load hostels", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSeedDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Initialize Hostels")
                .setMessage("Do you want to automatically add Hostel 1 to 14 to the database?")
                .setPositiveButton("Yes, Add All", (dialog, which) -> seedDefaultHostels())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void seedDefaultHostels() {
        DatabaseReference hostelsRef = mDatabase.child("hostels");
        
        for (int i = 1; i <= 14; i++) {
            String id = hostelsRef.push().getKey();
            String name = "Hostel " + i;
            int capacity = 4; 
            String price = "Shared: 5,000 | Single: 10,000";
            String desc = "Standard affordable accommodation for college or university students. Secure and comfortable environment.";
            
            Hostel hostel = new Hostel(id, name, capacity, price, desc, R.drawable.zzz, 0.0, 0.0, "University Organization");
            hostel.setAvailableBeds(4);
            if (id != null) {
                hostelsRef.child(id).setValue(hostel);
            }
        }
        logAction("Seeded default hostels (1-14)");
        Toast.makeText(this, "14 Hostels added successfully!", Toast.LENGTH_LONG).show();
    }

    private void showDeleteConfirmationDialog(Hostel hostel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hostel")
                .setMessage("Are you sure you want to delete " + hostel.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDatabase.child("hostels").child(hostel.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                logAction("Deleted hostel: " + hostel.getName());
                                Toast.makeText(ManageHostelsActivity.this, "Hostel deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ManageHostelsActivity.this, "Failed to delete hostel", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
