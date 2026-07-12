package com.example.group3ma;

public class SystemLog {
    public String email;
    public String userType;
    public String timestamp;
    public String action;

    public SystemLog() {
        // Default constructor required for calls to DataSnapshot.getValue(SystemLog.class)
    }

    public SystemLog(String email, String userType, String timestamp, String action) {
        this.email = email;
        this.userType = userType;
        this.timestamp = timestamp;
        this.action = action;
    }
}
