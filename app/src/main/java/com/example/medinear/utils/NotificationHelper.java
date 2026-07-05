package com.example.medinear.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.medinear.R;
import com.example.medinear.activities.MedicineReminderActivity;
import com.example.medinear.activities.NearbyPharmacyActivity;

public class NotificationHelper {

    private static final String CHANNEL_REMINDER = "medinear_reminder";
    private static final String CHANNEL_PHARMACY = "medinear_pharmacy";
    private static final String CHANNEL_GENERAL = "medinear_general";

    private static int notifId = 1;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDER,
                    "Medicine Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            reminderChannel.setDescription("Reminders to take your medicine on time");
            reminderChannel.enableVibration(true);
            reminderChannel.setVibrationPattern(new long[]{0, 500, 200, 500});

            NotificationChannel pharmacyChannel = new NotificationChannel(
                    CHANNEL_PHARMACY,
                    "Pharmacy Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            pharmacyChannel.setDescription("Alerts for nearby pharmacy availability");

            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_LOW);
            generalChannel.setDescription("General app notifications");

            manager.createNotificationChannel(reminderChannel);
            manager.createNotificationChannel(pharmacyChannel);
            manager.createNotificationChannel(generalChannel);
        }
    }

    // NEW - shared permission/enabled check with logging so we can see exactly why a notification doesn't show
    private static boolean canShowNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("NotifDebug", "POST_NOTIFICATIONS not granted — notification blocked");
                return false;
            }
        }

        boolean enabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
        Log.d("NotifDebug", "areNotificationsEnabled: " + enabled);
        return enabled;
    }

    public static void sendMedicineReminderNotification(Context context,
                                                        String medicineName,
                                                        String dosage,
                                                        String time) {
        createNotificationChannel(context);

        if (!canShowNotification(context)) return; // NEW

        Intent intent = new Intent(context, MedicineReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDER)
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
        Log.d("NotifDebug", "notify() called for reminder"); // NEW
    }

    public static void sendNearbyPharmacyNotification(Context context,
                                                      String pharmacyName,
                                                      float distanceKm) {
        createNotificationChannel(context);

        if (!canShowNotification(context)) return; // NEW

        Intent intent = new Intent(context, NearbyPharmacyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_PHARMACY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🏥 Pharmacy Nearby!")
                .setContentText(pharmacyName + " is only " + String.format("%.1f", distanceKm) + " km away")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(pharmacyName + " is only " + String.format("%.1f", distanceKm)
                                + " km away\n\nTap to find nearby pharmacies"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notifId++, builder.build());
    }

    public static void sendSimpleNotification(Context context, String title, String message) {
        createNotificationChannel(context);
        if (!canShowNotification(context)) return; // NEW

        Intent intent = new Intent(context, MedicineReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notifId++, builder.build());
    }

    public static void sendMedicineTimeNotification(Context context,
                                                    String medicineName,
                                                    String dosage) {
        createNotificationChannel(context);
        if (!canShowNotification(context)) return; // NEW

        Intent intent = new Intent(context, MedicineReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ Time to take your medicine!")
                .setContentText(medicineName + " (" + dosage + ")")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("It's time to take your medicine!\n\n"
                                + "Medicine: " + medicineName
                                + "\nDosage: " + dosage
                                + "\n\nDon't forget to stay healthy! 💚"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notifId++, builder.build());
    }

    public static void cancelAllNotifications(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManagerCompat.from(context).cancel(notificationId);
    }
}