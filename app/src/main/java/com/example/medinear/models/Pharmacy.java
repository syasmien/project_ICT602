package com.example.medinear.models;

public class Pharmacy {
    private String id;
    private String name;
    private String address;
    private String phone;
    private double latitude;
    private double longitude;
    private float distance;
    private float rating;
    private boolean openNow;
    private String openingHours;
    private String imageUrl;
    private boolean isFavourite; // NEW - drives the star icon, not stored inside the pharmacy JSON itself

    public Pharmacy() {
        // Default constructor
    }

    public Pharmacy(String id, String name, String address, String phone,
                    double latitude, double longitude, float distance,
                    float rating, boolean openNow) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.rating = rating;
        this.openNow = openNow;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public boolean isOpenNow() { return openNow; }
    public void setOpenNow(boolean openNow) { this.openNow = openNow; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFavourite() { return isFavourite; }
    public void setFavourite(boolean favourite) { isFavourite = favourite; }
}