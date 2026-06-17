package com.example.medinear.models;

public class Favourite {
    private String id, pharmacyName, address, phone;
    private double lat, lng;

    public Favourite() {}

    public Favourite(String id, String pharmacyName, String address,
                     String phone, double lat, double lng) {
        this.id           = id;
        this.pharmacyName = pharmacyName;
        this.address      = address;
        this.phone        = phone;
        this.lat          = lat;
        this.lng          = lng;
    }

    public String getId()           { return id; }
    public String getPharmacyName() { return pharmacyName; }
    public String getAddress()      { return address; }
    public String getPhone()        { return phone; }
    public double getLat()          { return lat; }
    public double getLng()          { return lng; }
}
