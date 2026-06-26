package com.example.medinear.activities;

import android.app.TimePickerDialog;
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
import com.example.medinear.utils.NotificationHelper;
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
    private ReminderStorageHelper reminderStorage;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_reminder);

        // Initialize storage helper
        reminderStorage = new ReminderStorageHelper(this);

        // Initialize views
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

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList, this::deleteReminder);
        recyclerView.setAdapter(adapter);

        // Setup click listeners
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnAddReminder.setOnClickListener(v -> addReminder());

        // Load reminders
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

        // Check for duplicate reminder
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

        // Create new reminder with unique ID
        Reminder reminder = new Reminder(
                UUID.randomUUID().toString(),
                medicineName,
                dosage,
                selectedTime,
                true
        );

        // ✅ Save to storage FIRST
        reminderStorage.addReminder(reminder);

        // ✅ Then add to list and update UI
        reminderList.add(reminder);
        adapter.notifyDataSetChanged();

        // ✅ Update empty state
        updateEmptyState();

        // Send notification
        NotificationHelper.sendMedicineReminderNotification(
                this, medicineName, dosage, selectedTime);

        Toast.makeText(this,
                "Reminder set for " + medicineName + " at " + selectedTime,
                Toast.LENGTH_LONG).show();

        // Clear inputs
        etMedicineName.setText("");
        etDosage.setText("");
        tvSelectedTime.setText("No time selected");
        selectedTime = "";

        progressBar.setVisibility(View.GONE);
        btnAddReminder.setEnabled(true);
    }

    private void loadReminders() {
        progressBar.setVisibility(View.VISIBLE);

        // ✅ Load reminders from storage
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
                    // ✅ Remove from storage
                    reminderStorage.deleteReminder(reminder);

                    // ✅ Remove from list and update UI
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
        // ✅ Refresh reminders when returning to this activity
        loadReminders();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}