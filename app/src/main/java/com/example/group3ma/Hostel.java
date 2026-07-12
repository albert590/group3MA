package com.example.group3ma;

import java.util.ArrayList;
import java.util.List;

public class Hostel {
    private String id;
    private String name;
    private int capacity; 
    private int availableBeds; 
    private int totalRooms;
    private int availableRooms;
    private String price;
    private String description;
    private int imageResId;
    private double latitude;
    private double longitude;
    private String owner;
    private String ownerPhone; // Added owner phone
    private boolean isVerified;
    
    private List<String> virtualTourUrls;
    private List<String> amenities;
    
    private float averageRating;
    private int reviewCount;

    public Hostel() {
        this.virtualTourUrls = new ArrayList<>();
        this.amenities = new ArrayList<>();
    }

    public Hostel(String id, String name, int capacity, String price, String description, int imageResId, double latitude, double longitude, String owner) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.availableBeds = capacity;
        this.price = price;
        this.description = description;
        this.imageResId = imageResId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.owner = owner;
        this.isVerified = false;
        this.virtualTourUrls = new ArrayList<>();
        this.amenities = new ArrayList<>();
        this.averageRating = 0.0f;
        this.reviewCount = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { 
        if (name != null) {
            return name.replace("MMUST ", "").replace("MMUST", "").trim();
        }
        return name; 
    }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getDescription() { 
        if (description != null) {
            return description.replace("MMUST students", "college or university students")
                              .replace("MMUST", "University");
        }
        return description; 
    }
    public void setDescription(String description) { this.description = description; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public List<String> getVirtualTourUrls() { return virtualTourUrls; }
    public void setVirtualTourUrls(List<String> virtualTourUrls) { this.virtualTourUrls = virtualTourUrls; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
