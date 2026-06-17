package com.example.medinear.models;

public class Pharmacy {
    private String id, name, address, phone, operatingHours;
    private boolean is24Hours;
    private double lat, lng;
    private float distanceKm;

    public Pharmacy() {}

    public Pharmacy(String id, String name, String address, String phone,
                    String operatingHours, boolean is24Hours,
                    double lat, double lng, float distanceKm) {
        this.id             = id;
        this.name           = name;
        this.address        = address;
        this.phone          = phone;
        this.operatingHours = operatingHours;
        this.is24Hours      = is24Hours;
        this.lat            = lat;
        this.lng            = lng;
        this.distanceKm     = distanceKm;
    }

    public String getId()             { return id; }
    public String getName()           { return name; }
    public String getAddress()        { return address; }
    public String getPhone()          { return phone; }
    public String getOperatingHours() { return operatingHours; }
    public boolean is24Hours()        { return is24Hours; }
    public double getLat()            { return lat; }
    public double getLng()            { return lng; }
    public float getDistanceKm()      { return distanceKm; }
}
