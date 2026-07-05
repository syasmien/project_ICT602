package com.example.medinear.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_MEDICINE_NAME = "medicine_name";
    public static final String EXTRA_DOSAGE = "dosage";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME);
        String dosage = intent.getStringExtra(EXTRA_DOSAGE);

        if (medicineName == null) medicineName = "your medicine";
        if (dosage == null) dosage = "";

        // This fires the REAL "time to take medicine" notification, at the actual scheduled time
        NotificationHelper.sendMedicineTimeNotification(context, medicineName, dosage);
    }
}