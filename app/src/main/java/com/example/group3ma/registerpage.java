package com.example.group3ma;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class registerpage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registerpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etAdmissionNumber = findViewById(R.id.etAdmissionNumber);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhoneNumber = findViewById(R.id.etPhoneNumber);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        RadioGroup rgGender = findViewById(R.id.rgGender);
        Spinner spYearOfStudy = findViewById(R.id.spYearOfStudy);
        Button btnRegister = findViewById(R.id.btnRegisterAction);
        TextView tvLogin = findViewById(R.id.tvLogin);

        // Populate Year of Study Spinner
        String[] years = {"Select Year of Study", "Year One", "Year Two", "Year Three", "Year Four", "Year Five"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years);
        spYearOfStudy.setAdapter(adapter);

        // Register Button Click Listener
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String admissionNumber = etAdmissionNumber.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            String yearOfStudy = spYearOfStudy.getSelectedItem().toString();

            if (fullName.isEmpty() || admissionNumber.isEmpty() || email.isEmpty() || 
                phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
                selectedGenderId == -1 || yearOfStudy.equals("Select Year of Study")) {
                Toast.makeText(registerpage.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(registerpage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // Save user data to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", email);
                editor.putString("phone", phoneNumber);
                editor.apply();

                Toast.makeText(registerpage.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                // Navigate to login or home page after successful registration
                Intent intent = new Intent(registerpage.this, loginpage.class);
                startActivity(intent);
                finish();
            }
        });

        // Login TextView Click Listener
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(registerpage.this, loginpage.class);
            startActivity(intent);
        });
    }
}