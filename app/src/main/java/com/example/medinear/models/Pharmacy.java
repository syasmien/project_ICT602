package com.example.medinear.models;

import java.io.Serializable;

public class Pharmacy implements Serializable {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String openingHours;
    private double latitude;
    private double longitude;
    private float rating;
    private float distance;
    private boolean isOpenNow;
    private boolean isFavourite;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }

    public boolean isOpenNow() { return isOpenNow; }
    public void setOpenNow(boolean openNow) { isOpenNow = openNow; }

    public boolean isFavourite() { return isFavourite; }
    public void setFavourite(boolean favourite) { isFavourite = favourite; }
}