package com.example.group3ma;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HostelDetailsActivity extends AppCompatActivity {

    protected ImageView ivHostelBanner;
    protected TextView tvHostelName, tvPrice, tvAvailability, tvCapacity, tvDescription, tvVerifiedBadge;
    protected RatingBar hostelRating;
    protected RecyclerView rvReviews;
    protected Button btnViewOnMap, btnVirtualTour, btnAddReview, btnShareHostel, btnContactOwner;
    protected ExtendedFloatingActionButton btnBookNow;
    protected FloatingActionButton fabFavorite;
    protected ChipGroup cgAmenitiesDetails;
    protected Toolbar toolbar;
    
    protected DatabaseReference mDatabase;
    protected String hostelId, hostelName, hostelPrice, ownerPhone; // Added ownerPhone
    protected List<Review> reviewList;
    protected ReviewAdapter reviewAdapter;
    protected UserSession userSession;
    protected boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostel_details);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userSession = new UserSession(this);

        hostelId = getIntent().getStringExtra("HOSTEL_ID");
        hostelName = getIntent().getStringExtra("HOSTEL_NAME");

        if (hostelId == null || hostelId.isEmpty()) {
            Toast.makeText(this, "Error: Hostel ID is missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerViews();
        fetchHostelDetails();
        fetchReviews();

        btnBookNow.setOnClickListener(v -> {
            if (!userSession.isLoggedIn()) {
                Toast.makeText(this, "Please login to book a hostel", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, loginpage.class));
                return;
            }
            
            Intent intent = new Intent(this, bookingform.class);
            intent.putExtra("HOSTEL_ID", hostelId);
            intent.putExtra("HOSTEL_NAME", hostelName);
            intent.putExtra("HOSTEL_PRICE", hostelPrice != null ? hostelPrice : "Contact for Price");
            startActivity(intent);
        });

        btnAddReview.setOnClickListener(v -> showAddReviewDialog());
        btnShareHostel.setOnClickListener(v -> shareHostelDetails());
    }

    protected void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivHostelBanner = findViewById(R.id.ivHostelBanner);
        tvHostelName = findViewById(R.id.tvHostelNameDetails);
        tvPrice = findViewById(R.id.tvPriceDetails);
        tvAvailability = findViewById(R.id.tvAvailabilityBadge);
        tvCapacity = findViewById(R.id.tvCapacityValue);
        tvDescription = findViewById(R.id.tvDescriptionDetails);
        tvVerifiedBadge = findViewById(R.id.tvVerifiedBadge);
        hostelRating = findViewById(R.id.hostelRating);
        rvReviews = findViewById(R.id.rvReviews);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnVirtualTour = findViewById(R.id.btnVirtualTour);
        btnAddReview = findViewById(R.id.btnAddReview);
        btnShareHostel = findViewById(R.id.btnShareHostel);
        btnContactOwner = findViewById(R.id.btnContactOwner);
        cgAmenitiesDetails = findViewById(R.id.cgAmenitiesDetails);
        fabFavorite = findViewById(R.id.fabFavorite);

        tvHostelName.setText(hostelName);
    }

    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    protected void setupRecyclerViews() {
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }

    protected void fetchHostelDetails() {
        if (hostelId == null) return;

        mDatabase.child("hostels").child(hostelId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Hostel hostel = snapshot.getValue(Hostel.class);
                if (hostel != null) {
                    hostelPrice = hostel.getPrice();
                    
                    // Ensure price is affordable if it's the old 15k value
                    if (hostelPrice != null && hostelPrice.contains("15,000")) {
                        hostelPrice = "Shared: 5,000 | Single: 10,000";
                    }
                    
                    ownerPhone = hostel.getOwnerPhone(); 
                    tvPrice.setText(hostelPrice);
                    
                    // Update Book Now button to include price
                    if (btnBookNow != null && hostelPrice != null) {
                        btnBookNow.setText("Book This Hostel (" + hostelPrice + ")");
                    }
                    tvDescription.setText(hostel.getDescription());
                    tvCapacity.setText(String.valueOf(hostel.getCapacity()));
                    
                    // Show "4 beds remaining" if database says 0 or is empty, but respect real occupancy
                    int beds = hostel.getAvailableBeds();
                    if (beds == 0 && hostel.getCapacity() == 0) beds = 4; // Default for new/empty entries
                    
                    tvAvailability.setText(beds + " beds remaining");
                    hostelRating.setRating(hostel.getAverageRating());
                    
                    if (hostel.isVerified()) {
                        tvVerifiedBadge.setVisibility(View.VISIBLE);
                    }

                    if (hostel.getAmenities() != null) {
                        cgAmenitiesDetails.removeAllViews();
                        for (String amenity : hostel.getAmenities()) {
                            Chip chip = new Chip(HostelDetailsActivity.this);
                            chip.setText(amenity);
                            chip.setChipBackgroundColorResource(R.color.app_background);
                            chip.setTextColor(getResources().getColor(R.color.white));
                            chip.setChipStrokeColorResource(R.color.text_secondary);
                            chip.setChipStrokeWidth(1.0f);
                            cgAmenitiesDetails.addView(chip);
                        }
                    }

                    Glide.with(HostelDetailsActivity.this)
                            .load(R.drawable.zzz)
                            .into(ivHostelBanner);

                    btnVirtualTour.setOnClickListener(v -> {
                        if (hostel.getVirtualTourUrls() != null && !hostel.getVirtualTourUrls().isEmpty()) {
                            String url = hostel.getVirtualTourUrls().get(0);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        } else {
                            Toast.makeText(HostelDetailsActivity.this, "No virtual tour available.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    protected void fetchReviews() {
        if (hostelId == null) return;
        mDatabase.child("reviews").child(hostelId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Review review = data.getValue(Review.class);
                    if (review != null) reviewList.add(review);
                }
                reviewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    protected void shareHostelDetails() {
        String shareBody = "Check out " + hostelName + " on Hostel Application!\nPrice: " + hostelPrice;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hostel Recommendation");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    protected void showAddReviewDialog() {
        if (!userSession.isLoggedIn()) {
            Toast.makeText(this, "Login to add a review", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_write_review, null);
        RatingBar dialogRating = view.findViewById(R.id.dialogRating);
        EditText dialogComment = view.findViewById(R.id.dialogComment);

        builder.setView(view).setTitle("Write a Review")
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = dialogRating.getRating();
                    String comment = dialogComment.getText().toString().trim();
                    if (rating > 0) submitReview(rating, comment);
                })
                .setNegativeButton("Cancel", null).show();
    }

    protected void submitReview(float rating, String comment) {
        String reviewId = mDatabase.child("reviews").child(hostelId).push().getKey();
        String userId = userSession.getEmail().replace(".", ",");
        Review review = new Review(reviewId, hostelId, userId, userSession.getFullName(), comment, rating);
        if (reviewId != null) {
            mDatabase.child("reviews").child(hostelId).child(reviewId).setValue(review)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                        updateHostelRating();
                    });
        }
    }

    protected void updateHostelRating() {
        mDatabase.child("reviews").child(hostelId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float total = 0; int count = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Review r = data.getValue(Review.class);
                    if (r != null) { total += r.rating; count++; }
                }
                if (count > 0) {
                    mDatabase.child("hostels").child(hostelId).child("averageRating").setValue(total / count);
                    mDatabase.child("hostels").child(hostelId).child("reviewCount").setValue(count);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
