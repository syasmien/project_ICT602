package com.example.medinear.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.medinear.R;
import com.example.medinear.activities.MedicineReminderActivity;
import com.example.medinear.activities.NearbyPharmacyActivity;

public class NotificationHelper {

    private static final String CHANNEL_REMINDER  = "medinear_reminder";
    private static final String CHANNEL_PHARMACY  = "medinear_pharmacy";
    private static int notifId = 1;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                context.getSystemService(NotificationManager.class);

            // Medicine reminder channel
            NotificationChannel reminderChannel = new NotificationChannel(
                CHANNEL_REMINDER,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH);
            reminderChannel.setDescription("Reminders to take your medicine on time");

            // Pharmacy alert channel
            NotificationChannel pharmacyChannel = new NotificationChannel(
                CHANNEL_PHARMACY,
                "Pharmacy Alerts",
                NotificationManager.IMPORTANCE_DEFAULT);
            pharmacyChannel.setDescription("Alerts for nearby pharmacy availability");

            if (manager != null) {
                manager.createNotificationChannel(reminderChannel);
                manager.createNotificationChannel(pharmacyChannel);
            }
        }
    }

    // Feature 4: Medicine reminder notification
    public static void sendMedicineReminderNotification(Context context,
                                                         String medicineName,
                                                         String dosage,
                                                         String time) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MedicineReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context, CHANNEL_REMINDER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("💊 Medicine Reminder Set!")
                .setContentText("Take " + medicineName + " (" + dosage + ") at " + time)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText("Reminder set!\n\nMedicine: " + medicineName
                        + "\nDosage: " + dosage
                        + "\nTime: " + time
                        + "\n\nStay healthy with MediNear! 💚"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notifId++, builder.build());
    }

    // Pharmacy nearby alert notification
    public static void sendNearbyPharmacyNotification(Context context,
                                                       String pharmacyName,
                                                       float distanceKm) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, NearbyPharmacyActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context, CHANNEL_PHARMACY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🏥 Pharmacy Nearby!")
                .setContentText(pharmacyName + " is only "
                    + String.format("%.1f", distanceKm) + " km away")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notifId++, builder.build());
    }
}
