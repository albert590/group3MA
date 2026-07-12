package com.example.group3ma;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class mybooking extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView tvHostelBookedValue, tvBookingStatusValue, tvNoBookings;
    private CardView cvBookingDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mybooking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();

        tvHostelBookedValue = findViewById(R.id.tvHostelBookedValue);
        tvBookingStatusValue = findViewById(R.id.tvBookingStatusValue);
        tvNoBookings = findViewById(R.id.tvNoBookings);
        cvBookingDetails = findViewById(R.id.cvBookingDetails);

        // Get current user email from UserSession
        UserSession session = new UserSession(this);
        String email = session.getEmail();

        if (email != null && !email.isEmpty()) {
            loadBookingStatus(email);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            tvNoBookings.setVisibility(View.VISIBLE);
            cvBookingDetails.setVisibility(View.GONE);
        }
    }

    private void loadBookingStatus(String email) {
        // Query the separate 'bookings' node for this user's email
        mDatabase.child("bookings").orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot lastBooking = null;
                    // Get the last one in the list (most recent usually)
                    for (DataSnapshot child : snapshot.getChildren()) {
                        lastBooking = child;
                    }

                    if (lastBooking != null) {
                        Booking booking = lastBooking.getValue(Booking.class);
                        if (booking != null) {
                            cvBookingDetails.setVisibility(View.VISIBLE);
                            tvNoBookings.setVisibility(View.GONE);

                            tvHostelBookedValue.setText(booking.hostel);
                            tvBookingStatusValue.setText(booking.status);

                            if ("Approved".equalsIgnoreCase(booking.status) || "Paid".equalsIgnoreCase(booking.status)) {
                                tvBookingStatusValue.setTextColor(ContextCompat.getColor(mybooking.this, R.color.status_success));
                            } else if ("Declined".equalsIgnoreCase(booking.status)) {
                                tvBookingStatusValue.setTextColor(ContextCompat.getColor(mybooking.this, R.color.status_error));
                            } else {
                                tvBookingStatusValue.setTextColor(ContextCompat.getColor(mybooking.this, R.color.status_pending));
                            }
                            return; // Found and displayed, exit
                        }
                    }
                }
                // If snapshot doesn't exist or booking is null, show no booking info
                cvBookingDetails.setVisibility(View.GONE);
                tvNoBookings.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mybooking.this, "Error loading booking: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                cvBookingDetails.setVisibility(View.GONE);
                tvNoBookings.setVisibility(View.VISIBLE);
            }
        });
    }
}