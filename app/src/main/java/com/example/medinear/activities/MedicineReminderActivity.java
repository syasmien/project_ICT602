package com.example.medinear.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.android.material.textfield.TextInputEditText;

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
    private View tvEmptyContainer;
    private TextView tvEmpty;

    private String selectedTime = "";
    private int selectedHour = -1;
    private int selectedMinute = -1;

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
        tvEmptyContainer = findViewById(R.id.tvEmptyContainer);
        tvEmpty = findViewById(R.id.tvEmpty);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Medicine Reminder");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList, this::deleteReminder, this::startEdit, this::toggleActive);
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

            selectedHour = hour;
            selectedMinute = minute;
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

        scheduleReminderAlarm(reminder, selectedHour, selectedMinute);

        Toast.makeText(this,
                medicineName + " reminder has been set",
                Toast.LENGTH_LONG).show();

        etMedicineName.setText("");
        etDosage.setText("");
        tvSelectedTime.setText("No intake time selected");
        selectedTime = "";
        selectedHour = -1;
        selectedMinute = -1;
        progressBar.setVisibility(View.GONE);
        btnAddReminder.setEnabled(true);
    }

    // ===== EDIT — dialog popup, tak guna form utama =====
    private void startEdit(Reminder reminder) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_reminder, null);

        TextInputEditText etEditName = dialogView.findViewById(R.id.etEditMedicineName);
        TextInputEditText etEditDosage = dialogView.findViewById(R.id.etEditDosage);
        TextView tvEditTime = dialogView.findViewById(R.id.tvEditSelectedTime);
        Button btnEditPickTime = dialogView.findViewById(R.id.btnEditPickTime);
        Button btnSave = dialogView.findViewById(R.id.btnSaveEdit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelEdit);

        etEditName.setText(reminder.getMedicineName());
        etEditDosage.setText(reminder.getDosage());
        tvEditTime.setText("⏰ " + reminder.getTime());

        final int[] editHour = new int[1];
        final int[] editMinute = new int[1];
        String[] parts = reminder.getTime().split(":");
        editHour[0] = Integer.parseInt(parts[0]);
        editMinute[0] = Integer.parseInt(parts[1]);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnEditPickTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                editHour[0] = hour;
                editMinute[0] = minute;
                tvEditTime.setText(String.format("⏰ %02d:%02d", hour, minute));
            }, editHour[0], editMinute[0], true).show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newName = etEditName.getText().toString().trim();
            String newDosage = etEditDosage.getText().toString().trim();

            if (newName.isEmpty()) {
                etEditName.setError("Medicine name required");
                return;
            }
            if (newDosage.isEmpty()) {
                etEditDosage.setError("Dosage required");
                return;
            }

            String newTime = String.format("%02d:%02d", editHour[0], editMinute[0]);

            cancelReminderAlarm(reminder);
            reminder.setMedicineName(newName);
            reminder.setDosage(newDosage);
            reminder.setTime(newTime);
            reminderStorage.updateReminder(reminder);

            if (reminder.isActive()) {
                scheduleReminderAlarm(reminder, editHour[0], editMinute[0]);
            }

            loadReminders();
            Toast.makeText(this, newName + " reminder updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // ===== TOGGLE ACTIVE/INACTIVE — pause/resume alarm tanpa delete data =====
    private void toggleActive(Reminder reminder, boolean isActive) {
        reminder.setActive(isActive);
        reminderStorage.updateReminder(reminder);

        if (isActive) {
            String[] parts = reminder.getTime().split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            scheduleReminderAlarm(reminder, hour, minute);
            Toast.makeText(this, reminder.getMedicineName() + " resumed", Toast.LENGTH_SHORT).show();
        } else {
            cancelReminderAlarm(reminder);
            Toast.makeText(this, reminder.getMedicineName() + " paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleReminderAlarm(Reminder reminder, int hour, int minute) {
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.set(Calendar.HOUR_OF_DAY, hour);
        triggerTime.set(Calendar.MINUTE, minute);
        triggerTime.set(Calendar.SECOND, 0);
        triggerTime.set(Calendar.MILLISECOND, 0);

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
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.getTimeInMillis(),
                    pendingIntent);
        }
    }

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
            tvEmptyContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void deleteReminder(Reminder reminder) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText("Delete Reminder");
        tvMessage.setText("Delete reminder for " + reminder.getMedicineName() + "? This cannot be undone.");
        btnConfirm.setText("Delete");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            reminderStorage.deleteReminder(reminder);
            cancelReminderAlarm(reminder);
            reminderList.remove(reminder);
            adapter.notifyDataSetChanged();
            updateEmptyState();
            Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
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