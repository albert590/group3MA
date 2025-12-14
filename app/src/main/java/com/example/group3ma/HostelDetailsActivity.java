package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HostelDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostel_details);

        Button btnBookNow = findViewById(R.id.btnBookNow);

        btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(HostelDetailsActivity.this, bookingform.class);
            // Pass hostel name if needed, e.g. intent.putExtra("HOSTEL_NAME", "Hall Name");
            startActivity(intent);
        });
    }
}