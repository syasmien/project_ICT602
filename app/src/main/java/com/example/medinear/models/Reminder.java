package com.example.medinear.models;

public class Reminder {
    private String id, medicineName, dosage, time;
    private boolean isActive;

    public Reminder() {}

    public Reminder(String id, String medicineName, String dosage,
                    String time, boolean isActive) {
        this.id           = id;
        this.medicineName = medicineName;
        this.dosage       = dosage;
        this.time         = time;
        this.isActive     = isActive;
    }

    public String getId()           { return id; }
    public String getMedicineName() { return medicineName; }
    public String getDosage()       { return dosage; }
    public String getTime()         { return time; }
    public boolean isActive()       { return isActive; }
}
