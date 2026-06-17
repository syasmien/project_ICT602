package com.example.medinear.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medinear.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private TextView tvEmail;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etName      = findViewById(R.id.etName);
        etPhone     = findViewById(R.id.etPhone);
        tvEmail     = findViewById(R.id.tvEmail);
        btnSave     = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadProfile();
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();
        tvEmail.setText(mAuth.getCurrentUser().getEmail());

        db.collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                progressBar.setVisibility(View.GONE);
                if (doc.exists()) {
                    etName.setText(doc.getString("name"));
                    etPhone.setText(doc.getString("phone"));
                }
            })
            .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void saveProfile() {
        String name  = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty())  { etName.setError("Name required"); return; }
        if (phone.isEmpty()) { etPhone.setError("Phone required"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .update(updates)
            .addOnSuccessListener(unused -> {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
