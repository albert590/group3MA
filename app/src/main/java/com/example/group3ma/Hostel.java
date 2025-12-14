package com.example.group3ma;

public class Hostel {
    private String name;
    private int capacity;
    private String price;
    private int imageResId;

    public Hostel(String name, int capacity, String price, int imageResId) {
        this.name = name;
        this.capacity = capacity;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }
}