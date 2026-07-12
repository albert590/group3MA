package com.example.group3ma;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of HostelDetailsActivity that adds "Favorites", "Similar Hostels",
 * "Potential Roommates", and "Owner Contact" functionality.
 */
public class HostelDetailsActivityImpl extends HostelDetailsActivity {

    private RecyclerView rvSimilarHostels, rvHostelRoommates;
    private HostelAdapter similarAdapter;
    private HostelRoommateAdapter roommateAdapter;
    private List<Hostel> similarHostelList;
    private List<User> potentialRoommates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If base class finished the activity due to missing ID, stop here
        if (isFinishing()) return;

        // Find views added in activity_hostel_details.xml but not handled in the base class
        rvSimilarHostels = findViewById(R.id.rvSimilarHostels);
        rvHostelRoommates = findViewById(R.id.rvHostelRoommates);

        setupExtraRecyclerViews();

        if (userSession != null && userSession.isLoggedIn() && hostelId != null) {
            checkIfFavorite();
            fetchPotentialRoommates();
        }

        // Setup Favorites toggle (fabFavorite is a protected field from the base class)
        if (fabFavorite != null) {
            fabFavorite.setOnClickListener(v -> toggleFavorite());
        }

        // Setup Contact Owner (btnContactOwner is a protected field from the base class)
        if (btnContactOwner != null) {
            btnContactOwner.setOnClickListener(v -> contactOwner());
        }

        fetchSimilarHostels();
    }

    private void setupExtraRecyclerViews() {
        // Similar Hostels - Horizontal list
        similarHostelList = new ArrayList<>();
        similarAdapter = new HostelAdapter(similarHostelList);
        if (rvSimilarHostels != null) {
            rvSimilarHostels.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvSimilarHostels.setAdapter(similarAdapter);
        }

        // Potential Roommates - Horizontal list
        potentialRoommates = new ArrayList<>();
        roommateAdapter = new HostelRoommateAdapter(potentialRoommates);
        if (rvHostelRoommates != null) {
            rvHostelRoommates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvHostelRoommates.setAdapter(roommateAdapter);
        }
    }

    private void checkIfFavorite() {
        String email = userSession.getEmail();
        if (email == null) return;
        String userId = email.replace(".", ",");
        mDatabase.child("favorites").child(userId).child(hostelId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFavorite = snapshot.exists();
                        updateFavoriteUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void toggleFavorite() {
        if (!userSession.isLoggedIn() || userSession.getEmail() == null) {
            Toast.makeText(this, "Please login to save favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = userSession.getEmail().replace(".", ",");
        if (isFavorite) {
            mDatabase.child("favorites").child(userId).child(hostelId).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show());
        } else {
            mDatabase.child("favorites").child(userId).child(hostelId).setValue(true)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateFavoriteUI() {
        if (fabFavorite != null) {
            fabFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        }
    }

    private void fetchSimilarHostels() {
        mDatabase.child("hostels").limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                similarHostelList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Hostel hostel = data.getValue(Hostel.class);
                    if (hostel != null) {
                        if (hostel.getId() == null) {
                            hostel.setId(data.getKey());
                        }
                        // Add to similar list if it's not the current hostel
                        if (hostel.getId() != null && !hostel.getId().equals(hostelId)) {
                            similarHostelList.add(hostel);
                        }
                    }
                }
                View label = findViewById(R.id.tvSimilarLabel);
                if (label != null) label.setVisibility(similarHostelList.isEmpty() ? View.GONE : View.VISIBLE);
                similarAdapter.updateList(similarHostelList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchPotentialRoommates() {
        if (hostelName == null) return;
        // Search for bookings for THIS hostel that are Approved
        mDatabase.child("bookings").orderByChild("hostel").equalTo(hostelName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> emails = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking b = ds.getValue(Booking.class);
                    // Filter: Approved bookings by other users
                    if (b != null && "Approved".equals(b.status) && !b.email.equals(userSession.getEmail())) {
                        emails.add(b.email);
                    }
                }
                loadRoommateProfiles(emails);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadRoommateProfiles(List<String> emails) {
        potentialRoommates.clear();
        View label = findViewById(R.id.tvRoommatesLabel);

        if (emails.isEmpty()) {
            if (label != null) label.setVisibility(View.GONE);
            roommateAdapter.updateList(potentialRoommates);
            return;
        }

        final int totalToLoad = emails.size();
        final int[] loadedCount = {0};

        for (String email : emails) {
            String key = email.replace(".", ",");
            mDatabase.child("users").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    // Only show students who explicitly enabled roommate matching
                    if (user != null && user.lookingForRoommate) {
                        potentialRoommates.add(user);
                    }
                    loadedCount[0]++;
                    if (loadedCount[0] == totalToLoad) {
                        if (label != null) label.setVisibility(potentialRoommates.isEmpty() ? View.GONE : View.VISIBLE);
                        roommateAdapter.updateList(potentialRoommates);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadedCount[0]++;
                }
            });
        }
    }

    private void contactOwner() {
        // ALWAYS use Admin phone for contact as requested
        String adminPhone = "0769262481"; 

        try {
            // Prefer WhatsApp
            String formattedPhone = adminPhone.startsWith("0") ? "254" + adminPhone.substring(1) : adminPhone;
            String url = "https://api.whatsapp.com/send?phone=" + formattedPhone + "&text=Hello Albert, I am interested in " + hostelName;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            // Fallback to direct Phone Call dialer
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + adminPhone));
            startActivity(intent);
        }
    }
}
