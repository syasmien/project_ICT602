package com.example.medinear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medinear.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private TextView tvEmail, tvDisplayName, tvAvatar;
    private Button btnSave, btnLogout;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etName        = findViewById(R.id.etName);
        etPhone       = findViewById(R.id.etPhone);
        tvEmail       = findViewById(R.id.tvEmail);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvAvatar      = findViewById(R.id.tvAvatar);
        btnSave       = findViewById(R.id.btnSave);
        btnLogout     = findViewById(R.id.btnLogout);
        progressBar   = findViewById(R.id.progressBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadProfile();
        btnSave.setOnClickListener(v -> saveProfile());
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void loadProfile() {
        if (mAuth.getCurrentUser() == null) return;

        String email = mAuth.getCurrentUser().getEmail();
        tvEmail.setText(email);

        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    if (doc.exists()) {
                        String name  = doc.getString("name");
                        String phone = doc.getString("phone");

                        etName.setText(name);
                        etPhone.setText(phone);

                        // Update hero section
                        if (name != null && !name.isEmpty()) {
                            tvDisplayName.setText(name);
                            // Show first letter as avatar
                            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile() {
        String name  = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty())  { etName.setError("Name required");   return; }
        if (phone.isEmpty()) { etPhone.setError("Phone required");  return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name",  name);
        updates.put("phone", phone);

        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .set(updates)   // set instead of update — works even if doc doesn't exist
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    // Refresh hero
                    tvDisplayName.setText(name);
                    tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());

                    Toast.makeText(this, "✅ Profile saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Update failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}