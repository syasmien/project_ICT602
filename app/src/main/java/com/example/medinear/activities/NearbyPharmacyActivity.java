package com.example.medinear.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.medinear.R;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NearbyPharmacyActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final String API_KEY = "AIzaSyDr64tr-Y3YopYDi7PmbUou96Q0o3wSYlI";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvPharmacyCount;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_pharmacy);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvPharmacyCount = findViewById(R.id.tvPharmacyCount);
        progressBar     = findViewById(R.id.progressBar);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(false);

        // Marker tap → show save dialog
        mMap.setOnMarkerClickListener(marker -> {
            if ("You are here".equals(marker.getTitle())) return false;

            new AlertDialog.Builder(this)
                    .setTitle(marker.getTitle())
                    .setMessage(marker.getSnippet())
                    .setPositiveButton("⭐ Save Favourite", (dialog, which) ->
                            saveFavourite(
                                    marker.getTitle(),
                                    marker.getSnippet(),
                                    marker.getPosition().latitude,
                                    marker.getPosition().longitude
                            ))
                    .setNegativeButton("Close", null)
                    .show();

            return true;
        });

        checkPermission();
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        progressBar.setVisibility(View.VISIBLE);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng user = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(user, 14f));
                        loadPlaces(location);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Cannot get location. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPlaces(Location location) {
        new Thread(() -> {
            try {
                String urlStr =
                        "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                                + "?location=" + location.getLatitude() + "," + location.getLongitude()
                                + "&radius=5000"
                                + "&type=pharmacy"
                                + "&key=" + API_KEY;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

                JSONObject json    = new JSONObject(result.toString());
                JSONArray  results = json.getJSONArray("results");

                runOnUiThread(() -> {
                    mMap.clear();

                    LatLng user = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(user)
                            .title("You are here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    int count = 0;
                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject obj     = results.getJSONObject(i);
                            String name        = obj.getString("name");
                            String address     = obj.optString("vicinity", "No address");
                            JSONObject geo     = obj.getJSONObject("geometry").getJSONObject("location");
                            double lat         = geo.getDouble("lat");
                            double lng         = geo.getDouble("lng");

                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title(name)
                                    .snippet(address)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            count++;
                        } catch (Exception ignored) {}
                    }

                    progressBar.setVisibility(View.GONE);
                    tvPharmacyCount.setText(count + " pharmacies found nearby");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveFavourite(String name, String address, double lat, double lng) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Check duplicate first
        db.collection("favourites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("pharmacyName", name)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Toast.makeText(this, "Already in favourites!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("userId",       userId);
                    data.put("pharmacyName", name);
                    data.put("address",      address != null ? address : "");
                    data.put("phone",        "");
                    data.put("lat",          lat);
                    data.put("lng",          lng);

                    db.collection("favourites").add(data)
                            .addOnSuccessListener(ref ->
                                    Toast.makeText(this, "⭐ Saved to favourites!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }
}