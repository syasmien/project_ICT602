package com.example.medinear.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.adapters.FavouriteAdapter;
import com.example.medinear.models.Favourite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FavouriteAdapter adapter;
    private List<Favourite> favouriteList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        progressBar  = findViewById(R.id.progressBar);
        tvEmpty      = findViewById(R.id.tvEmpty);

        getSupportActionBar().setTitle("Favourite Pharmacies");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavouriteAdapter(favouriteList,
            this::navigateToPharmacy,
            this::confirmDelete);
        recyclerView.setAdapter(adapter);

        loadFavourites();
    }

    private void loadFavourites() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("favourites")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                progressBar.setVisibility(View.GONE);
                favouriteList.clear();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Favourite f = new Favourite(
                        doc.getId(),
                        doc.getString("pharmacyName"),
                        doc.getString("address"),
                        doc.getString("phone"),
                        doc.getDouble("lat") != null ? doc.getDouble("lat") : 0.0,
                        doc.getDouble("lng") != null ? doc.getDouble("lng") : 0.0
                    );
                    favouriteList.add(f);
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(favouriteList.isEmpty() ? View.VISIBLE : View.GONE);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void navigateToPharmacy(Favourite favourite) {
        Uri gmmIntentUri = Uri.parse(
            "google.navigation:q=" + favourite.getLat() + "," + favourite.getLng() + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri browserUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination="
                + favourite.getLat() + "," + favourite.getLng());
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    private void confirmDelete(Favourite favourite) {
        new AlertDialog.Builder(this)
            .setTitle("Remove Favourite")
            .setMessage("Remove " + favourite.getPharmacyName() + " from favourites?")
            .setPositiveButton("Remove", (dialog, which) ->
                db.collection("favourites").document(favourite.getId()).delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Removed from favourites", Toast.LENGTH_SHORT).show();
                        loadFavourites();
                    }))
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
