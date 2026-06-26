package com.example.medinear.models;

public class Reminder {
    private String id;
    private String medicineName;
    private String dosage;
    private String time;
    private boolean isActive;

    public Reminder() {
        // Default constructor
    }

    public Reminder(String id, String medicineName, String dosage, String time, boolean isActive) {
        this.id = id;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.time = time;
        this.isActive = isActive;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}