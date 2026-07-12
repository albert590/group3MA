package com.example.group3ma;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SellItemActivity extends AppCompatActivity {

    private EditText etItemName, etItemDescription, etItemPrice, etContactInfo;
    private Spinner spinnerCategory;
    private DatabaseReference mDatabase;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_item);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        session = new UserSession(this);

        etItemName = findViewById(R.id.etItemName);
        etItemDescription = findViewById(R.id.etItemDescription);
        etItemPrice = findViewById(R.id.etItemPrice);
        etContactInfo = findViewById(R.id.etContactInfo);
        spinnerCategory = findViewById(R.id.spinnerItemCategory);

        setupCategorySpinner();

        findViewById(R.id.btnPostItem).setOnClickListener(v -> postItem());
    }

    private void setupCategorySpinner() {
        String[] categories = {"Furniture", "Electronics", "Books", "Kitchenware", "Clothing", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void postItem() {
        String name = etItemName.getText().toString().trim();
        String description = etItemDescription.getText().toString().trim();
        String price = etItemPrice.getText().toString().trim();
        String contact = etContactInfo.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || price.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemId = mDatabase.child("marketplace").push().getKey();
        MarketplaceItem item = new MarketplaceItem(itemId, session.getEmail(), name, description, price, category, contact);
        // Default image URL for now
        item.imageUrl = "default"; 

        if (itemId != null) {
            mDatabase.child("marketplace").child(itemId).setValue(item)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SellItemActivity.this, "Item posted successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SellItemActivity.this, "Failed to post item", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
