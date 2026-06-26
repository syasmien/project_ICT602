package com.example.medinear.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.medinear.R;
import com.example.medinear.utils.LocalStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private LocalStorageHelper storageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("MediNearPrefs", MODE_PRIVATE);
        storageHelper = new LocalStorageHelper(this);

        // Check if user is logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews(user);
        setupListeners();
    }

    private void initViews(FirebaseUser user) {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);

        // Get user name from Firebase or SharedPreferences
        String userName = user.getDisplayName();
        if (userName == null || userName.isEmpty()) {
            userName = prefs.getString("user_name", "User");
        }
        tvWelcome.setText("Hello, " + userName + " 👋");
    }

    private void setupListeners() {
        CardView cardNearby = findViewById(R.id.cardNearby);
        CardView cardCamera = findViewById(R.id.cardCamera);
        CardView cardFavourites = findViewById(R.id.cardFavourites);
        CardView cardReminder = findViewById(R.id.cardReminder);
        CardView cardProfile = findViewById(R.id.cardProfile);

        cardNearby.setOnClickListener(v ->
                startActivity(new Intent(this, NearbyPharmacyActivity.class)));

        cardCamera.setOnClickListener(v ->
                startActivity(new Intent(this, CameraActivity.class)));

        cardFavourites.setOnClickListener(v ->
                startActivity(new Intent(this, FavouritesActivity.class)));

        cardReminder.setOnClickListener(v ->
                startActivity(new Intent(this, MedicineReminderActivity.class)));

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            prefs.edit().clear().apply();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is still logged in
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}