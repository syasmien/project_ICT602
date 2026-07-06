package com.example.medinear.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.medinear.R;
import com.example.medinear.models.Pharmacy;

public class PharmacyDetailActivity extends AppCompatActivity {

    private TextView tvName, tvDistance, tvAddress, tvPhone, tvHours;
    private Button btnNavigate, btnCall;
    private Pharmacy pharmacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_detail);

        // Initialize views
        tvName = findViewById(R.id.tvName);
        tvDistance = findViewById(R.id.tvDistance);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvHours = findViewById(R.id.tvHours);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnCall = findViewById(R.id.btnCall);

        // Get pharmacy data
        pharmacy = (Pharmacy) getIntent().getSerializableExtra("pharmacy");
        if (pharmacy == null) {
            Toast.makeText(this, "Pharmacy data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display data
        displayPharmacyDetails();

        // Set click listeners
        btnNavigate.setOnClickListener(v -> navigateToPharmacy());
        btnCall.setOnClickListener(v -> callPharmacy());
    }

    private void displayPharmacyDetails() {
        tvName.setText(pharmacy.getName());

        String distanceText = String.format("%.1f km away", pharmacy.getDistance());
        tvDistance.setText(distanceText);

        tvAddress.setText(pharmacy.getAddress());

        // Phone
        if (pharmacy.getPhone() != null && !pharmacy.getPhone().isEmpty()) {
            tvPhone.setText(pharmacy.getPhone());
        } else {
            tvPhone.setText("Phone number not available");
        }

        // Hours
        if (pharmacy.getOpeningHours() != null && !pharmacy.getOpeningHours().isEmpty()) {
            tvHours.setText(pharmacy.getOpeningHours());
        } else if (pharmacy.isOpenNow()) {
            tvHours.setText("🟢 Open now");
            tvHours.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            tvHours.setText("🔴 Closed");
            tvHours.setTextColor(ContextCompat.getColor(this, R.color.red));
        }
    }

    private void navigateToPharmacy() {
        // Open Google Maps with navigation
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
                pharmacy.getLatitude() + "," + pharmacy.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback to web-based Google Maps
            Uri webUri = Uri.parse("https://maps.google.com/maps?q=" +
                    pharmacy.getLatitude() + "," + pharmacy.getLongitude());
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }
    }

    private void callPharmacy() {
        if (pharmacy.getPhone() != null && !pharmacy.getPhone().isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + pharmacy.getPhone()));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }
}