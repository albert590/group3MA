package com.example.group3ma;

public class MarketplaceItem {
    public String itemId;
    public String sellerId;
    public String itemName;
    public String description;
    public String price;
    public String category;
    public String imageUrl;
    public String contactInfo;
    public long timestamp;

    public MarketplaceItem() {}

    public MarketplaceItem(String itemId, String sellerId, String itemName, String description, String price, String category, String contactInfo) {
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.itemName = itemName;
        this.description = description;
        this.price = price;
        this.category = category;
        this.contactInfo = contactInfo;
        this.timestamp = System.currentTimeMillis();
    }
}
