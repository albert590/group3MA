package com.example.group3ma;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class hostellistpage extends AppCompatActivity {

    private HostelAdapter adapter;
    private List<Hostel> hostelList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hostellistpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("hostels");

        EditText etSearch = findViewById(R.id.etSearch);
        RecyclerView rvHostelList = findViewById(R.id.rvHostelList);
        rvHostelList.setLayoutManager(new LinearLayoutManager(this));

        hostelList = new ArrayList<>();
        adapter = new HostelAdapter(hostelList);
        rvHostelList.setAdapter(adapter);

        fetchHostels();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchHostels() {
        mDatabase.addValueEventListener(new ValueEventListener() {
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
                adapter.updateList(hostelList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    Toast.makeText(hostellistpage.this, "PERMISSION DENIED: Set Firebase Database Rules to true!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(hostellistpage.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
