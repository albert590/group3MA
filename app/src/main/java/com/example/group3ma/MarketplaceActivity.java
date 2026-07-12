package com.example.group3ma;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceActivity extends AppCompatActivity {

    private RecyclerView rvMarketplace;
    private EditText etSearchMarket;
    private DatabaseReference mDatabase;
    private List<MarketplaceItem> itemList;
    private MarketplaceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference().child("marketplace");
        rvMarketplace = findViewById(R.id.rvMarketplace);
        etSearchMarket = findViewById(R.id.etSearchMarket);

        rvMarketplace.setLayoutManager(new GridLayoutManager(this, 2));
        itemList = new ArrayList<>();
        adapter = new MarketplaceAdapter(itemList);
        rvMarketplace.setAdapter(adapter);

        loadMarketplaceItems();

        findViewById(R.id.btnSellItem).setOnClickListener(v -> {
            startActivity(new Intent(MarketplaceActivity.this, SellItemActivity.class));
        });

        etSearchMarket.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadMarketplaceItems() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MarketplaceItem item = ds.getValue(MarketplaceItem.class);
                    if (item != null) itemList.add(item);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterItems(String text) {
        List<MarketplaceItem> filtered = new ArrayList<>();
        for (MarketplaceItem item : itemList) {
            if (item.itemName.toLowerCase().contains(text.toLowerCase())) {
                filtered.add(item);
            }
        }
        rvMarketplace.setAdapter(new MarketplaceAdapter(filtered));
    }
}
