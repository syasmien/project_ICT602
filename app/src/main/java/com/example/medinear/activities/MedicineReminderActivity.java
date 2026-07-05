package com.example.medinear.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.adapters.ReminderAdapter;
import com.example.medinear.models.Reminder;
import com.example.medinear.utils.ReminderReceiver;
import com.example.medinear.utils.ReminderStorageHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class MedicineReminderActivity extends AppCompatActivity {

    private EditText etMedicineName, etDosage;
    private TextView tvSelectedTime;
    private Button btnPickTime, btnAddReminder;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private String selectedTime = "";
    private int selectedHour = -1;   // NEW
    private int selectedMinute = -1; // NEW

    private ReminderStorageHelper reminderStorage;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_reminder);

        reminderStorage = new ReminderStorageHelper(this);

        etMedicineName = findViewById(R.id.etMedicineName);
        etDosage = findViewById(R.id.etDosage);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnAddReminder = findViewById(R.id.btnAddReminder);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Medicine Reminder");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList, this::deleteReminder);
        recyclerView.setAdapter(adapter);

        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnAddReminder.setOnClickListener(v -> addReminder());

        loadReminders();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(Calendar.HOUR_OF_DAY, hour);
            selectedCal.set(Calendar.MINUTE, minute);
            selectedCal.set(Calendar.SECOND, 0);

            if (selectedCal.before(Calendar.getInstance())) {
                Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedHour = hour;     // NEW
            selectedMinute = minute; // NEW
            selectedTime = String.format("%02d:%02d", hour, minute);
            tvSelectedTime.setText("⏰ Time: " + selectedTime);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void addReminder() {
        String medicineName = etMedicineName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();

        if (medicineName.isEmpty()) {
            etMedicineName.setError("Medicine name required");
            etMedicineName.requestFocus();
            return;
        }

        if (dosage.isEmpty()) {
            etDosage.setError("Dosage required");
            etDosage.requestFocus();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a reminder time", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Reminder existing : reminderList) {
            if (existing.getMedicineName().equalsIgnoreCase(medicineName)
                    && existing.getTime().equals(selectedTime)) {
                Toast.makeText(this, "Reminder already exists for " + medicineName + " at " + selectedTime,
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAddReminder.setEnabled(false);

        Reminder reminder = new Reminder(
                UUID.randomUUID().toString(),
                medicineName,
                dosage,
                selectedTime,
                true
        );

        reminderStorage.addReminder(reminder);

        reminderList.add(reminder);
        adapter.notifyDataSetChanged();

        updateEmptyState();

        // CHANGED - schedule the real notification for the chosen time instead of firing one now
        scheduleReminderAlarm(reminder, selectedHour, selectedMinute);

        // CHANGED - simple confirmation instead of the old detailed push notification
        Toast.makeText(this,
                medicineName + " reminder has been set",
                Toast.LENGTH_LONG).show();

        etMedicineName.setText("");
        etDosage.setText("");
        tvSelectedTime.setText("No time selected");
        selectedTime = "";
        selectedHour = -1;
        selectedMinute = -1;

        progressBar.setVisibility(View.GONE);
        btnAddReminder.setEnabled(true);
    }

    // NEW - schedules a real alarm that fires the "time to take medicine" notification later
    private void scheduleReminderAlarm(Reminder reminder, int hour, int minute) {
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.set(Calendar.HOUR_OF_DAY, hour);
        triggerTime.set(Calendar.MINUTE, minute);
        triggerTime.set(Calendar.SECOND, 0);
        triggerTime.set(Calendar.MILLISECOND, 0);

        // If somehow already passed today, push to tomorrow
        if (triggerTime.before(Calendar.getInstance())) {
            triggerTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_MEDICINE_NAME, reminder.getMedicineName());
        intent.putExtra(ReminderReceiver.EXTRA_DOSAGE, reminder.getDosage());

        int requestCode = reminder.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        boolean canScheduleExact = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canScheduleExact = alarmManager.canScheduleExactAlarms();
        }

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.getTimeInMillis(),
                    pendingIntent);
        } else {
            // Fallback for devices/users that haven't granted exact-alarm permission
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.getTimeInMillis(),
                    pendingIntent);
        }
    }

    // NEW - cancels the scheduled alarm when a reminder is deleted
    private void cancelReminderAlarm(Reminder reminder) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        int requestCode = reminder.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void loadReminders() {
        progressBar.setVisibility(View.VISIBLE);

        reminderList.clear();
        List<Reminder> savedReminders = reminderStorage.getReminders();
        reminderList.addAll(savedReminders);

        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);

        updateEmptyState();
    }

    private void updateEmptyState() {
        if (reminderList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void deleteReminder(Reminder reminder) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Delete reminder for " + reminder.getMedicineName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    reminderStorage.deleteReminder(reminder);
                    cancelReminderAlarm(reminder); // NEW - stop the scheduled notification too

                    reminderList.remove(reminder);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();

                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}