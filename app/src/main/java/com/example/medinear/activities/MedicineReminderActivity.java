package com.example.medinear.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.adapters.ReminderAdapter;
import com.example.medinear.models.Reminder;
import com.example.medinear.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.*;

public class MedicineReminderActivity extends AppCompatActivity {

    private EditText etMedicineName, etDosage;
    private TextView tvSelectedTime;
    private Button btnPickTime, btnAddReminder;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private String selectedTime = "";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_reminder);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etMedicineName = findViewById(R.id.etMedicineName);
        etDosage       = findViewById(R.id.etDosage);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnPickTime    = findViewById(R.id.btnPickTime);
        btnAddReminder = findViewById(R.id.btnAddReminder);
        recyclerView   = findViewById(R.id.recyclerView);
        progressBar    = findViewById(R.id.progressBar);
        tvEmpty        = findViewById(R.id.tvEmpty);

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
            selectedTime = String.format("%02d:%02d", hour, minute);
            tvSelectedTime.setText("⏰ Time: " + selectedTime);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void addReminder() {
        String medicineName = etMedicineName.getText().toString().trim();
        String dosage       = etDosage.getText().toString().trim();

        if (medicineName.isEmpty()) { etMedicineName.setError("Medicine name required"); return; }
        if (dosage.isEmpty())       { etDosage.setError("Dosage required"); return; }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a reminder time", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> reminder = new HashMap<>();
        reminder.put("userId", userId);
        reminder.put("medicineName", medicineName);
        reminder.put("dosage", dosage);
        reminder.put("time", selectedTime);
        reminder.put("isActive", true);
        reminder.put("createdAt", System.currentTimeMillis());

        db.collection("reminders").add(reminder)
            .addOnSuccessListener(ref -> {
                // Send immediate push notification as confirmation
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

                loadReminders();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadReminders() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("reminders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                progressBar.setVisibility(View.GONE);
                reminderList.clear();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Reminder r = new Reminder(
                        doc.getId(),
                        doc.getString("medicineName"),
                        doc.getString("dosage"),
                        doc.getString("time"),
                        Boolean.TRUE.equals(doc.getBoolean("isActive"))
                    );
                    reminderList.add(r);
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(reminderList.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error loading reminders", Toast.LENGTH_SHORT).show();
            });
    }

    private void deleteReminder(Reminder reminder) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Reminder")
            .setMessage("Delete reminder for " + reminder.getMedicineName() + "?")
            .setPositiveButton("Delete", (dialog, which) ->
                db.collection("reminders").document(reminder.getId()).delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                        loadReminders();
                    }))
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
