package com.example.group3ma;

public class User {
    public String fullName;
    public String admissionNumber;
    public String email;
    public String phoneNumber;
    public String gender;
    public String yearOfStudy;
    public String password;
    public long lastPasswordUpdate; // Timestamp of last password update

    // Smart Roommate Matching Preferences
    public String budgetRange;
    public String studyHabits;
    public String smokingPreference;
    public boolean lookingForRoommate;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String fullName, String admissionNumber, String email, String phoneNumber, String gender, String yearOfStudy, String password) {
        this.fullName = fullName;
        this.admissionNumber = admissionNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.yearOfStudy = yearOfStudy;
        this.password = password;
        this.lastPasswordUpdate = System.currentTimeMillis();
        this.lookingForRoommate = false;
        this.budgetRange = "";
        this.studyHabits = "";
        this.smokingPreference = "";
    }
}