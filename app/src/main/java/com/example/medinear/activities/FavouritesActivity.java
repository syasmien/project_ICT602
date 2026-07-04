package com.example.medinear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.adapters.PharmacyAdapter;
import com.example.medinear.models.Pharmacy;
import com.example.medinear.utils.LocalStorageHelper;
import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPharmacies;
    private TextView tvEmpty, tvCount;
    private Button btnClearAll;
    private View emptyState;

    private LocalStorageHelper storageHelper;
    private PharmacyAdapter pharmacyAdapter;
    private List<Pharmacy> favouritePharmacyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        storageHelper = new LocalStorageHelper(this);

        initViews();
        loadFavourites();

        btnClearAll.setOnClickListener(v -> {
            storageHelper.clearFavouritePharmacies();
            loadFavourites();
            Toast.makeText(this, "All favourites cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        recyclerViewPharmacies = findViewById(R.id.recyclerViewPharmacies);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCount = findViewById(R.id.tvCount);
        btnClearAll = findViewById(R.id.btnClearAll);
        emptyState = findViewById(R.id.emptyState);

        recyclerViewPharmacies.setLayoutManager(new LinearLayoutManager(this));
        pharmacyAdapter = new PharmacyAdapter(favouritePharmacyList, PharmacyAdapter.Mode.FAVOURITES);
        pharmacyAdapter.setOnDeleteClickListener(pharmacy -> {
            storageHelper.removeFavouritePharmacy(pharmacy.getId());
            Toast.makeText(this, pharmacy.getName() + " removed from favourites", Toast.LENGTH_SHORT).show();
            loadFavourites();
        });
        recyclerViewPharmacies.setAdapter(pharmacyAdapter);
    }

    private void loadFavourites() {
        favouritePharmacyList.clear();
        favouritePharmacyList.addAll(storageHelper.getFavouritePharmacies());
        pharmacyAdapter.updateData(favouritePharmacyList);

        boolean isEmpty = favouritePharmacyList.isEmpty();

        recyclerViewPharmacies.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        btnClearAll.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (tvCount != null) {
            tvCount.setText(favouritePharmacyList.size() + " items");
        }
    }

    public void goToNearbyPharmacy(View view) {
        Intent intent = new Intent(this, NearbyPharmacyActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavourites();
    }
}