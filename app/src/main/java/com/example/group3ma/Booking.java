package com.example.group3ma;

public class Booking {
    public String bookingId;
    public String email;
    public String phone;
    public String hostel;
    public String roomType;
    public String receiptId;
    public String status;
    public boolean notificationSent; // To avoid duplicate SMS/Email

    public Booking() {
        // Default constructor required for calls to DataSnapshot.getValue(Booking.class)
    }

    public Booking(String bookingId, String email, String phone, String hostel, String roomType, String receiptId, String status) {
        this.bookingId = bookingId;
        this.email = email;
        this.phone = phone;
        this.hostel = hostel;
        this.roomType = roomType;
        this.receiptId = receiptId;
        this.status = status;
        this.notificationSent = false;
    }
}