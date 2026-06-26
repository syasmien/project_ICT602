package com.example.medinear.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medinear.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private Button btnEditProfile, btnChangePassword, btnBack;

    private FirebaseAuth mAuth;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("MediNearPrefs", MODE_PRIVATE);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews(user);
        setupListeners();
    }

    private void initViews(FirebaseUser user) {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBack);

        String userName = user.getDisplayName();
        if (userName == null || userName.isEmpty()) {
            userName = prefs.getString("user_name", "User");
        }

        tvUserName.setText("Name: " + userName);
        tvUserEmail.setText("Email: " + user.getEmail());
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit profile coming soon", Toast.LENGTH_SHORT).show();
        });

        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change password coming soon", Toast.LENGTH_SHORT).show();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}