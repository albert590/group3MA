package com.example.group3ma;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class hostellistpage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hostellistpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView rvHostelList = findViewById(R.id.rvHostelList);
        rvHostelList.setLayoutManager(new LinearLayoutManager(this));

        List<Hostel> hostelList = new ArrayList<>();
        // Sample Data
        hostelList.add(new Hostel("Hall 1", 10, "KSH 15,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 2", 5, "KSH 22,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 3", 50, "KSH 20,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 4", 20, "KSH 18,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 5", 30, "KSH 21,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 6", 15, "KSH 19,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 7", 40, "KSH 23,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 8", 25, "KSH 20,500 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 9", 35, "KSH 21,500 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 10", 12, "KSH 17,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 11", 8, "KSH 24,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 12", 60, "KSH 16,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Hall 13", 100, "KSH 25,000 / Semester", R.mipmap.ic_launcher));
        hostelList.add(new Hostel("Mashabiki", 45, "KSH 14,000 / Semester", R.mipmap.ic_launcher));

        HostelAdapter adapter = new HostelAdapter(hostelList);
        rvHostelList.setAdapter(adapter);
    }
}