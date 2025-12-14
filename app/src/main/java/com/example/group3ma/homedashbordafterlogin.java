package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class homedashbordafterlogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homedashbordafterlogin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnAvailableHostel = findViewById(R.id.btnAvailableHostel);
        Button btnMyBooking = findViewById(R.id.btnMyBooking);
        Button btnPaymentStatus = findViewById(R.id.btnPaymentStatus);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnSupport = findViewById(R.id.btnSupport);
        Button btnNext = findViewById(R.id.btnNext);

        btnAvailableHostel.setOnClickListener(v -> {
            Intent intent = new Intent(homedashbordafterlogin.this, hostellistpage.class);
            startActivity(intent);
        });

        btnMyBooking.setOnClickListener(v -> {
            // TODO: Navigate to My Booking Page
            Toast.makeText(this, "My Booking Clicked", Toast.LENGTH_SHORT).show();
        });

        btnPaymentStatus.setOnClickListener(v -> {
            // TODO: Navigate to Payment Status Page
            Toast.makeText(this, "Payment Status Clicked", Toast.LENGTH_SHORT).show();
        });

        btnProfile.setOnClickListener(v -> {
            // TODO: Navigate to Profile Page
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
        });

        btnSupport.setOnClickListener(v -> {
            // TODO: Navigate to Support Page
            Toast.makeText(this, "Support Clicked", Toast.LENGTH_SHORT).show();
        });

        btnNext.setOnClickListener(v -> {
            // TODO: Navigate to Next Page
            Toast.makeText(this, "Next Clicked", Toast.LENGTH_SHORT).show();
        });
    }
}