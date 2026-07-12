package com.example.group3ma;

public class MaintenanceReport {
    public String reportId;
    public String userId;
    public String hostelId;
    public String category; // Water, Electricity, Broken Door, etc.
    public String description;
    public String status; // Pending, In Progress, Resolved
    public long timestamp;

    public MaintenanceReport() {}

    public MaintenanceReport(String reportId, String userId, String hostelId, String category, String description) {
        this.reportId = reportId;
        this.userId = userId;
        this.hostelId = hostelId;
        this.category = category;
        this.description = description;
        this.status = "Pending";
        this.timestamp = System.currentTimeMillis();
    }
}
