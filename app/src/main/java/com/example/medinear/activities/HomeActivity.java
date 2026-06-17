package com.example.medinear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.medinear.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = user.getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvWelcome.setText("Hello, " + name + " 👋");
                    }
                });

        // Cards
        CardView cardNearby = findViewById(R.id.cardNearby);
        CardView cardCamera = findViewById(R.id.cardCamera);
        CardView cardFavourites = findViewById(R.id.cardFavourites);
        CardView cardReminder = findViewById(R.id.cardReminder);
        CardView cardProfile = findViewById(R.id.cardProfile);

        Button btnLogout = findViewById(R.id.btnLogout);

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
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}