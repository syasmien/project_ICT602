package com.example.medinear.utils;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MediNear_FCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 🔥 Prefer DATA payload (best practice)
        Map<String, String> data = remoteMessage.getData();

        if (data != null && !data.isEmpty()) {

            String type = data.get("type");

            if ("reminder".equals(type)) {

                String medicineName = data.get("medicineName");
                String dosage = data.get("dosage");
                String time = data.get("time");

                Log.d(TAG, "Reminder received: " + medicineName);

                NotificationHelper.sendMedicineReminderNotification(
                        this,
                        medicineName != null ? medicineName : "Medicine Reminder",
                        dosage != null ? dosage : "",
                        time != null ? time : ""
                );
            }

            return; // ⛔ prevent double notification
        }

        // 🔔 OPTIONAL: fallback for notification payload only
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d(TAG, "Notification received: " + title);

            NotificationHelper.sendMedicineReminderNotification(
                    this,
                    title != null ? title : "MediNear",
                    body != null ? body : "",
                    ""
            );
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        // TODO: send token to Firestore / backend
    }
}