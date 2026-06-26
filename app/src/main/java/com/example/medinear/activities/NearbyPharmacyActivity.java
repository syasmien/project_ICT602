package com.example.medinear.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.adapters.PharmacyAdapter;
import com.example.medinear.models.Pharmacy;
import com.example.medinear.utils.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class NearbyPharmacyActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerView;
    private PharmacyAdapter adapter;
    private List<Pharmacy> pharmacyList = new ArrayList<>();
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_pharmacy);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PharmacyAdapter(pharmacyList);
        recyclerView.setAdapter(adapter);

        // Setup Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Check location permission
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            currentLocation = task.getResult();
                            updateMapWithLocation();
                            loadNearbyPharmacies();
                        } else {
                            Toast.makeText(NearbyPharmacyActivity.this,
                                    "Unable to get location", Toast.LENGTH_SHORT).show();
                            // Load pharmacies with default location
                            loadNearbyPharmacies();
                        }
                    }
                });
    }

    private void updateMapWithLocation() {
        if (googleMap == null || currentLocation == null) return;

        LatLng currentLatLng = new LatLng(
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title("You are here"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void loadNearbyPharmacies() {
        // Sample pharmacy data (replace with actual data from your API or local database)
        pharmacyList.clear();

        // Add some sample pharmacies near the user
        if (currentLocation != null) {
            // Sample pharmacy 1
            Pharmacy pharmacy1 = new Pharmacy();
            pharmacy1.setName("MediCare Pharmacy");
            pharmacy1.setAddress("123 Main Street");
            pharmacy1.setPhone("+1234567890");
            pharmacy1.setLatitude(currentLocation.getLatitude() + 0.01);
            pharmacy1.setLongitude(currentLocation.getLongitude() + 0.01);
            pharmacy1.setDistance(0.5f);
            pharmacy1.setRating(4.5f);
            pharmacy1.setOpenNow(true);
            pharmacyList.add(pharmacy1);

            // Sample pharmacy 2
            Pharmacy pharmacy2 = new Pharmacy();
            pharmacy2.setName("HealthPlus Pharmacy");
            pharmacy2.setAddress("456 Oak Avenue");
            pharmacy2.setPhone("+1234567891");
            pharmacy2.setLatitude(currentLocation.getLatitude() - 0.015);
            pharmacy2.setLongitude(currentLocation.getLongitude() + 0.008);
            pharmacy2.setDistance(1.2f);
            pharmacy2.setRating(4.2f);
            pharmacy2.setOpenNow(true);
            pharmacyList.add(pharmacy2);

            // Sample pharmacy 3
            Pharmacy pharmacy3 = new Pharmacy();
            pharmacy3.setName("City Pharmacy");
            pharmacy3.setAddress("789 Pine Street");
            pharmacy3.setPhone("+1234567892");
            pharmacy3.setLatitude(currentLocation.getLatitude() + 0.02);
            pharmacy3.setLongitude(currentLocation.getLongitude() - 0.01);
            pharmacy3.setDistance(2.0f);
            pharmacy3.setRating(3.8f);
            pharmacy3.setOpenNow(false);
            pharmacyList.add(pharmacy3);

            // Sample pharmacy 4
            Pharmacy pharmacy4 = new Pharmacy();
            pharmacy4.setName("Green Cross Pharmacy");
            pharmacy4.setAddress("321 Elm Boulevard");
            pharmacy4.setPhone("+1234567893");
            pharmacy4.setLatitude(currentLocation.getLatitude() - 0.008);
            pharmacy4.setLongitude(currentLocation.getLongitude() - 0.012);
            pharmacy4.setDistance(0.8f);
            pharmacy4.setRating(4.8f);
            pharmacy4.setOpenNow(true);
            pharmacyList.add(pharmacy4);

            // Add markers to map
            for (Pharmacy pharmacy : pharmacyList) {
                LatLng location = new LatLng(pharmacy.getLatitude(), pharmacy.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(pharmacy.getName())
                        .snippet(pharmacy.getAddress()));
            }
        }

        adapter.notifyDataSetChanged();

        // Send notification about nearby pharmacies
        if (!pharmacyList.isEmpty()) {
            NotificationHelper.sendNearbyPharmacyNotification(
                    this,
                    pharmacyList.get(0).getName(),
                    pharmacyList.get(0).getDistance()
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required for nearby pharmacies",
                        Toast.LENGTH_LONG).show();
                loadNearbyPharmacies(); // Load with default location
            }
        }
    }
}