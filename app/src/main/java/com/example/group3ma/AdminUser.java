package com.example.group3ma;

public class AdminUser {
    public String username;
    public String password;
    public String phoneNumber;
    public String role; // role can be "superadmin" or "admin"

    public AdminUser() {
        // Default constructor required for calls to DataSnapshot.getValue(AdminUser.class)
    }

    public AdminUser(String username, String password, String phoneNumber, String role) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}