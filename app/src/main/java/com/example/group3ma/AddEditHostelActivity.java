package com.example.group3ma;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditHostelActivity extends AppCompatActivity {

    private TextInputEditText etName, etCapacity, etPrice, etDescription, etLatitude, etLongitude, etOwner;
    private ChipGroup cgAmenities;
    private DatabaseReference mDatabase;
    private String hostelId = null;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_hostel);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        session = new UserSession(this);

        etName = findViewById(R.id.etHostelName);
        etOwner = findViewById(R.id.etHostelOwner);
        etCapacity = findViewById(R.id.etHostelCapacity);
        etPrice = findViewById(R.id.etHostelPrice);
        etDescription = findViewById(R.id.etHostelDescription);
        etLatitude = findViewById(R.id.etHostelLatitude);
        etLongitude = findViewById(R.id.etHostelLongitude);
        cgAmenities = findViewById(R.id.cgAmenities);
        Button btnSave = findViewById(R.id.btnSaveHostel);
        TextView tvTitle = findViewById(R.id.tvAddEditTitle);

        // Check if editing
        if (getIntent().hasExtra("HOSTEL_ID")) {
            hostelId = getIntent().getStringExtra("HOSTEL_ID");
            etName.setText(getIntent().getStringExtra("HOSTEL_NAME"));
            etOwner.setText(getIntent().getStringExtra("HOSTEL_OWNER"));
            etCapacity.setText(String.valueOf(getIntent().getIntExtra("HOSTEL_CAPACITY", 0)));
            etPrice.setText(getIntent().getStringExtra("HOSTEL_PRICE"));
            etDescription.setText(getIntent().getStringExtra("HOSTEL_DESCRIPTION"));
            etLatitude.setText(String.valueOf(getIntent().getDoubleExtra("HOSTEL_LATITUDE", 0.0)));
            etLongitude.setText(String.valueOf(getIntent().getDoubleExtra("HOSTEL_LONGITUDE", 0.0)));
            tvTitle.setText("Edit Hostel");
            
            // Note: In a production app, you'd fetch the full Hostel object to pre-select chips.
            // For now, we'll assume the admin re-selects them or we'd add an extra intent with the list.
        }

        btnSave.setOnClickListener(v -> saveHostel());
    }

    private void saveHostel() {
        String name = etName.getText().toString().trim();
        String owner = etOwner.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String latStr = etLatitude.getText().toString().trim();
        String lonStr = etLongitude.getText().toString().trim();

        if (name.isEmpty() || owner.isEmpty() || capacityStr.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        double latitude = latStr.isEmpty() ? 0.0 : Double.parseDouble(latStr);
        double longitude = lonStr.isEmpty() ? 0.0 : Double.parseDouble(lonStr);
        
        List<String> selectedAmenities = new ArrayList<>();
        for (int i = 0; i < cgAmenities.getChildCount(); i++) {
            Chip chip = (Chip) cgAmenities.getChildAt(i);
            if (chip.isChecked()) {
                selectedAmenities.add(chip.getText().toString());
            }
        }

        boolean isNew = (hostelId == null);
        if (isNew) {
            hostelId = mDatabase.child("hostels").push().getKey();
        }

        Hostel hostel = new Hostel(hostelId, name, capacity, price, description, R.drawable.zzz, latitude, longitude, owner);
        hostel.setAmenities(selectedAmenities);
        
        // If it's a new hostel, explicitly set initial beds to 4 if capacity is 4
        if (isNew && capacity == 4) {
            hostel.setAvailableBeds(4);
        } else if (isNew) {
            hostel.setAvailableBeds(capacity);
        }
        
        mDatabase.child("hostels").child(hostelId).setValue(hostel)
                .addOnSuccessListener(aVoid -> {
                    logAction((isNew ? "Added" : "Updated") + " hostel: " + name);
                    Toast.makeText(AddEditHostelActivity.this, "Hostel saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddEditHostelActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logAction(String action) {
        String email = session.getEmail();
        if (email != null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            SystemLog log = new SystemLog(email, "Admin", timestamp, action);
            mDatabase.child("system_logs").push().setValue(log);
        }
    }
}
