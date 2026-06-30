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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NearbyPharmacyActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String PLACES_API_KEY = "AIzaSyDr64tr-Y3YopYDi7PmbUou96Q0o3wSYlI"; //
    private static final int SEARCH_RADIUS_METERS = 3000;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerView;
    private PharmacyAdapter adapter;
    private final List<Pharmacy> pharmacyList = new ArrayList<>();
    private Location currentLocation;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_pharmacy);

        requestQueue = Volley.newRequestQueue(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PharmacyAdapter(pharmacyList);
        recyclerView.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

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
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        if (currentLocation != null) {
            updateMapWithLocation();
        }
    }

    private void loadNearbyPharmacies() {
        if (currentLocation == null) {
            Toast.makeText(this, "Location not available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                + "&radius=" + SEARCH_RADIUS_METERS
                + "&type=pharmacy"
                + "&key=" + PLACES_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handlePlacesResponse,
                error -> Toast.makeText(NearbyPharmacyActivity.this,
                        "Failed to load pharmacies: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    private void handlePlacesResponse(JSONObject response) {
        pharmacyList.clear();

        String status = response.optString("status", "");
        if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
            Toast.makeText(this, "Places API error: " + status, Toast.LENGTH_SHORT).show();
            return;
        }

        JSONArray results = response.optJSONArray("results");
        if (results == null) {
            adapter.notifyDataSetChanged();
            return;
        }

        if (googleMap != null) {
            googleMap.clear();
            if (currentLocation != null) {
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .title("You are here"));
            }
        }

        for (int i = 0; i < results.length(); i++) {
            try {
                JSONObject obj = results.getJSONObject(i);

                JSONObject geometry = obj.optJSONObject("geometry");
                if (geometry == null) continue;
                JSONObject locationObj = geometry.optJSONObject("location");
                if (locationObj == null) continue;

                double lat = locationObj.optDouble("lat", 0);
                double lng = locationObj.optDouble("lng", 0);

                Pharmacy pharmacy = new Pharmacy();
                pharmacy.setName(obj.optString("name", "Unknown Pharmacy"));
                pharmacy.setAddress(obj.optString("vicinity", ""));
                pharmacy.setLatitude(lat);
                pharmacy.setLongitude(lng);
                pharmacy.setRating((float) obj.optDouble("rating", 0));

                JSONObject openingHours = obj.optJSONObject("opening_hours");
                boolean openNow = openingHours != null && openingHours.optBoolean("open_now", false);
                pharmacy.setOpenNow(openNow);

                if (currentLocation != null) {
                    float[] distResult = new float[1];
                    Location.distanceBetween(
                            currentLocation.getLatitude(), currentLocation.getLongitude(),
                            lat, lng, distResult);
                    pharmacy.setDistance(distResult[0] / 1000f); // meters to km
                }

                pharmacyList.add(pharmacy);

                if (googleMap != null) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(pharmacy.getName())
                            .snippet(pharmacy.getAddress()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        adapter.notifyDataSetChanged();

        if (!pharmacyList.isEmpty()) {
            NotificationHelper.sendNearbyPharmacyNotification(
                    this,
                    pharmacyList.get(0).getName(),
                    pharmacyList.get(0).getDistance()
            );
        } else {
            Toast.makeText(this, "No pharmacies found nearby", Toast.LENGTH_SHORT).show();
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
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}