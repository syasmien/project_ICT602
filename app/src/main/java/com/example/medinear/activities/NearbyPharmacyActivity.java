package com.example.medinear.activities;

import android.Manifest;
import android.content.Intent;
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
import com.example.medinear.utils.LocalStorageHelper;
import com.example.medinear.utils.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyPharmacyActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SEARCH_RADIUS_METERS = 3000;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerView;
    private PharmacyAdapter adapter;
    private final List<Pharmacy> pharmacyList = new ArrayList<>();
    private Location currentLocation;
    private RequestQueue requestQueue;
    private LocalStorageHelper storageHelper;
    private Map<String, Pharmacy> markerPharmacyMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_pharmacy);

        requestQueue = Volley.newRequestQueue(this);
        storageHelper = new LocalStorageHelper(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PharmacyAdapter(pharmacyList, PharmacyAdapter.Mode.NEARBY);

        adapter.setOnAddFavouriteClickListener(pharmacy -> {
            storageHelper.addFavouritePharmacy(pharmacy);
            Toast.makeText(this, pharmacy.getName() + " added to favourites", Toast.LENGTH_SHORT).show();
        });

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
        markerPharmacyMap.clear();

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

        // Set marker click listener
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (marker.getTitle() != null && !marker.getTitle().equals("You are here")) {
                    Pharmacy pharmacy = markerPharmacyMap.get(marker.getId());
                    if (pharmacy != null) {
                        Intent intent = new Intent(NearbyPharmacyActivity.this, PharmacyDetailActivity.class);
                        intent.putExtra("pharmacy", pharmacy);
                        startActivity(intent);
                        return true;
                    }
                }
                return false;
            }
        });
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
        markerPharmacyMap.clear();

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
                Marker userMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .title("You are here"));
                if (userMarker != null) {
                    markerPharmacyMap.put(userMarker.getId(), null);
                }
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
                pharmacy.setId(obj.optString("place_id", null));
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
                    pharmacy.setDistance(distResult[0] / 1000f);
                }

                pharmacy.setFavourite(storageHelper.isFavouritePharmacy(pharmacy.getId()));

                pharmacyList.add(pharmacy);

                if (googleMap != null) {
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(pharmacy.getName())
                            .snippet(pharmacy.getAddress()));
                    if (marker != null) {
                        markerPharmacyMap.put(marker.getId(), pharmacy);
                    }
                }

                // Fetch additional details like phone number
                if (pharmacy.getId() != null) {
                    fetchPlaceDetails(pharmacy.getId(), pharmacy);
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

    private void fetchPlaceDetails(String placeId, Pharmacy pharmacy) {
        String url = "https://maps.googleapis.com/maps/api/place/details/json"
                + "?place_id=" + placeId
                + "&fields=formatted_phone_number,opening_hours,website"
                + "&key=" + PLACES_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");

                        // Get phone number
                        String phone = result.optString("formatted_phone_number", "");
                        pharmacy.setPhone(phone);

                        // Get opening hours
                        JSONObject hours = result.optJSONObject("opening_hours");
                        if (hours != null) {
                            JSONArray weekdayText = hours.optJSONArray("weekday_text");
                            if (weekdayText != null && weekdayText.length() > 0) {
                                StringBuilder hoursText = new StringBuilder();
                                for (int i = 0; i < weekdayText.length(); i++) {
                                    hoursText.append(weekdayText.getString(i)).append("\n");
                                }
                                pharmacy.setOpeningHours(hoursText.toString());
                            }
                        }

                        // Update the pharmacy in the list
                        for (int i = 0; i < pharmacyList.size(); i++) {
                            if (pharmacyList.get(i).getId().equals(pharmacy.getId())) {
                                pharmacyList.set(i, pharmacy);
                                adapter.notifyItemChanged(i);
                                break;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Silently fail for additional details
                }
        );

        requestQueue.add(request);
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