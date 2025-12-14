package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        
        // Handle Window Insets for Edge-to-Edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Book Now button
        Button btnBookNow = findViewById(R.id.btnBookNow);

        // Set Click Listener to navigate to Booking Form
        btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, bookingform.class);
            // You can pass extras here if needed, e.g., intent.putExtra("HOSTEL_NAME", "Hall Name");
            startActivity(intent);
        });
    }
}