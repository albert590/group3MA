package com.example.group3ma;

public class Review {
    public String reviewId;
    public String hostelId;
    public String userId;
    public String userName;
    public String comment;
    public float rating;
    public long timestamp;

    public Review() {}

    public Review(String reviewId, String hostelId, String userId, String userName, String comment, float rating) {
        this.reviewId = reviewId;
        this.hostelId = hostelId;
        this.userId = userId;
        this.userName = userName;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = System.currentTimeMillis();
    }
}
