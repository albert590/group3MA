package com.example.group3ma;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;

public class UserSession {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_ADMISSION_NUMBER = "admissionNumber";
    private static final String KEY_ROLE = "role";

    public UserSession(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(String email, String phone, String userType, String fullName, String admissionNumber, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_ADMISSION_NUMBER, admissionNumber);
        editor.putString(KEY_ROLE, role);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        // Sign out from Firebase Auth to ensure database rules remain effective
        FirebaseAuth.getInstance().signOut();
        
        // Clear SharedPreferences
        editor.clear();
        editor.commit();
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, null);
    }

    public String getAdmissionNumber() {
        return sharedPreferences.getString(KEY_ADMISSION_NUMBER, null);
    }
    
    public String getUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, null);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, "admin");
    }
}
