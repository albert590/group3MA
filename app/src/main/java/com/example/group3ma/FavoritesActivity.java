package com.example.group3ma;

import android.os.Bundle;
import android.view.View;
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

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private HostelAdapter adapter;
    private List<Hostel> favoriteHostels;
    private TextView tvNoFavorites;
    private DatabaseReference mDatabase;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        session = new UserSession(this);

        rvFavorites = findViewById(R.id.rvFavorites);
        tvNoFavorites = findViewById(R.id.tvNoFavorites);

        favoriteHostels = new ArrayList<>();
        adapter = new HostelAdapter(favoriteHostels);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(adapter);

        if (session.isLoggedIn()) {
            fetchFavorites();
        } else {
            Toast.makeText(this, "Please login to view favorites", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchFavorites() {
        String userId = session.getEmail().replace(".", ",");
        mDatabase.child("favorites").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> hostelIds = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    hostelIds.add(ds.getKey());
                }
                loadHostelDetails(hostelIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadHostelDetails(List<String> hostelIds) {
        if (hostelIds.isEmpty()) {
            favoriteHostels.clear();
            adapter.updateList(favoriteHostels);
            tvNoFavorites.setVisibility(View.VISIBLE);
            return;
        }

        mDatabase.child("hostels").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteHostels.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Hostel hostel = ds.getValue(Hostel.class);
                    if (hostel != null) {
                        if (hostel.getId() == null) {
                            hostel.setId(ds.getKey());
                        }
                        if (hostelIds.contains(hostel.getId())) {
                            favoriteHostels.add(hostel);
                        }
                    }
                }
                
                if (favoriteHostels.isEmpty()) {
                    tvNoFavorites.setVisibility(View.VISIBLE);
                } else {
                    tvNoFavorites.setVisibility(View.GONE);
                }
                adapter.updateList(favoriteHostels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
