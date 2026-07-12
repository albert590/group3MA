package com.example.group3ma;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoommateMatchingActivity extends AppCompatActivity {

    private Spinner spinnerBudget;
    private RadioGroup rgStudyHabits;
    private Switch switchSmoking;
    private RecyclerView rvMatches;
    private TextView tvMatchesTitle;
    private DatabaseReference mDatabase;
    private UserSession session;
    private String currentUserKey;
    private User currentUserObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roommate_matching);

        mDatabase = FirebaseDatabase.getInstance("https://group3ma-f0430-default-rtdb.firebaseio.com/").getReference();
        session = new UserSession(this);
        String email = session.getEmail();
        if (email == null) {
            finish();
            return;
        }
        currentUserKey = email.replace(".", ",");

        spinnerBudget = findViewById(R.id.spinnerBudget);
        rgStudyHabits = findViewById(R.id.rgStudyHabits);
        switchSmoking = findViewById(R.id.switchSmoking);
        rvMatches = findViewById(R.id.rvMatches);
        tvMatchesTitle = findViewById(R.id.tvMatchesTitle);

        rvMatches.setLayoutManager(new LinearLayoutManager(this));

        setupSpinner();
        loadCurrentPreferences();

        findViewById(R.id.btnSavePreferences).setOnClickListener(v -> savePreferences());
    }

    private void setupSpinner() {
        String[] budgets = {"Under 5,000", "5,000 - 8,000", "8,000 - 12,000", "Above 12,000"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, budgets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBudget.setAdapter(adapter);
    }

    private void loadCurrentPreferences() {
        mDatabase.child("users").child(currentUserKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserObj = snapshot.getValue(User.class);
                if (currentUserObj != null) {
                    // Pre-fill if preferences exist
                    if (currentUserObj.budgetRange != null && !currentUserObj.budgetRange.isEmpty()) {
                        for (int i = 0; i < spinnerBudget.getCount(); i++) {
                            if (spinnerBudget.getItemAtPosition(i).toString().equals(currentUserObj.budgetRange)) {
                                spinnerBudget.setSelection(i);
                                break;
                            }
                        }
                    }
                    if ("Non-Smoking".equals(currentUserObj.smokingPreference)) {
                        switchSmoking.setChecked(true);
                    }
                    if ("Early Bird".equals(currentUserObj.studyHabits)) rgStudyHabits.check(R.id.rbEarlyBird);
                    else if ("Night Owl".equals(currentUserObj.studyHabits)) rgStudyHabits.check(R.id.rbNightOwl);
                    else rgStudyHabits.check(R.id.rbFlexible);

                    if (currentUserObj.lookingForRoommate) {
                        findMatches(currentUserObj);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void savePreferences() {
        String budget = spinnerBudget.getSelectedItem().toString();
        String smoking = switchSmoking.isChecked() ? "Non-Smoking" : "Any";
        String habits = "Flexible";
        int checkedId = rgStudyHabits.getCheckedRadioButtonId();
        if (checkedId == R.id.rbEarlyBird) habits = "Early Bird";
        else if (checkedId == R.id.rbNightOwl) habits = "Night Owl";

        mDatabase.child("users").child(currentUserKey).child("budgetRange").setValue(budget);
        mDatabase.child("users").child(currentUserKey).child("smokingPreference").setValue(smoking);
        mDatabase.child("users").child(currentUserKey).child("studyHabits").setValue(habits);
        mDatabase.child("users").child(currentUserKey).child("lookingForRoommate").setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Preferences Saved!", Toast.LENGTH_SHORT).show();
                        loadCurrentPreferences();
                    }
                });
    }

    private void findMatches(User currentUser) {
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> matches = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User potentialMatch = userSnapshot.getValue(User.class);
                    if (potentialMatch != null && !userSnapshot.getKey().equals(currentUserKey) 
                            && potentialMatch.lookingForRoommate 
                            && potentialMatch.gender != null && potentialMatch.gender.equalsIgnoreCase(currentUser.gender)) {
                        
                        matches.add(potentialMatch);
                    }
                }
                
                if (!matches.isEmpty()) {
                    tvMatchesTitle.setVisibility(View.VISIBLE);
                    tvMatchesTitle.setText("Top Matches for You");
                    rvMatches.setAdapter(new RoommateAdapter(matches, currentUser));
                } else {
                    tvMatchesTitle.setVisibility(View.VISIBLE);
                    tvMatchesTitle.setText("No matches found yet. Try adjusting preferences.");
                    rvMatches.setAdapter(null);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
