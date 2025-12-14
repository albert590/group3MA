package com.example.group3ma;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class bookingform extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bookingform);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText etHostelName = findViewById(R.id.etHostelName);
        RadioGroup rgRoomType = findViewById(R.id.rgRoomType);
        EditText etMpesaPhone = findViewById(R.id.etMpesaPhone);
        Button btnSubmitBooking = findViewById(R.id.btnSubmitBooking);

        // Autofill Hostel Name (In a real app, this would come from an Intent extra)
        etHostelName.setText("Hall 3");

        // Fetch registered user details from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String registeredPhone = sharedPreferences.getString("phone", "");
        String registeredEmail = sharedPreferences.getString("email", "");

        // Prompt user with registered phone number if available
        if (!registeredPhone.isEmpty()) {
            etMpesaPhone.setText(registeredPhone);
        }

        btnSubmitBooking.setOnClickListener(v -> {
            int selectedRoomTypeId = rgRoomType.getCheckedRadioButtonId();
            String mpesaPhone = etMpesaPhone.getText().toString().trim();

            if (selectedRoomTypeId == -1) {
                Toast.makeText(this, "Please select a room type", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mpesaPhone.isEmpty()) {
                Toast.makeText(this, "Please enter your MPESA phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // In a real app, you would process the payment and save the booking here.
            
            String emailToSendTo = registeredEmail.isEmpty() ? "your registered email" : registeredEmail;
            String successMessage = "Booking Successful!\nConfirmation sent to " + emailToSendTo;
            Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();
            
            // Navigate back to dashboard or show success screen
            finish();
        });
    }
}