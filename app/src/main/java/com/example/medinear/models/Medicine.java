package com.example.medinear.models;

public class Medicine {
    private String name;
    private String notes;
    private String imagePath;
    private String timestamp;
    private String time; // For reminders

    public Medicine() {}

    public Medicine(String name, String notes, String imagePath, String timestamp) {
        this.name = name;
        this.notes = notes;
        this.imagePath = imagePath;
        this.timestamp = timestamp;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}