package com.example.medinear.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medinear.R;
import com.example.medinear.models.Pharmacy;

public class PharmacyDetailActivity extends AppCompatActivity {

    private TextView tvName, tvAddress, tvPhone, tvHours, tvDistance;
    private Button btnNavigate, btnCall;

    private String name, address, phone, hours;
    private double lat, lng, distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_detail);

        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvHours = findViewById(R.id.tvHours);
        tvDistance = findViewById(R.id.tvDistance);

        btnNavigate = findViewById(R.id.btnNavigate);
        btnCall = findViewById(R.id.btnCall);

        getSupportActionBar().setTitle("Pharmacy Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get data from intent
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        address = intent.getStringExtra("address");
        phone = intent.getStringExtra("phone");
        hours = intent.getStringExtra("hours");
        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);
        distance = intent.getDoubleExtra("distance", 0);

        // Set data
        tvName.setText(name);
        tvAddress.setText(address);
        tvPhone.setText(phone);
        tvHours.setText(hours);
        tvDistance.setText(String.format("%.1f km away", distance));

        // Navigate button
        btnNavigate.setOnClickListener(v -> openGoogleMaps());

        // Call button
        btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            startActivity(callIntent);
        });
    }

    private void openGoogleMaps() {
        Uri uri = Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=d");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri browserUri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng
            );
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}