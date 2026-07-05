package com.example.medinear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

        btnClearAll.setOnClickListener(v -> showConfirmDialog(
                "Clear all favourites?",
                "All saved pharmacies will be removed. This action cannot be undone.",
                "Clear All",
                () -> {
                    storageHelper.clearFavouritePharmacies();
                    loadFavourites();
                    Toast.makeText(this, "All favourites cleared", Toast.LENGTH_SHORT).show();
                }
        ));
    }

    private void initViews() {
        recyclerViewPharmacies = findViewById(R.id.recyclerViewPharmacies);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCount = findViewById(R.id.tvCount);
        btnClearAll = findViewById(R.id.btnClearAll);
        emptyState = findViewById(R.id.emptyState);

        recyclerViewPharmacies.setLayoutManager(new LinearLayoutManager(this));
        pharmacyAdapter = new PharmacyAdapter(favouritePharmacyList, PharmacyAdapter.Mode.FAVOURITES);

        pharmacyAdapter.setOnDeleteClickListener(pharmacy -> showConfirmDialog(
                "Remove from favourites?",
                pharmacy.getName() + " will be removed from your saved list.",
                "Remove",
                () -> {
                    storageHelper.removeFavouritePharmacy(pharmacy.getId());
                    Toast.makeText(this, pharmacy.getName() + " removed from favourites", Toast.LENGTH_SHORT).show();
                    loadFavourites();
                }
        ));

        recyclerViewPharmacies.setAdapter(pharmacyAdapter);
    }

    // NEW - reusable custom confirmation dialog, ganti default AlertDialog style
    private void showConfirmDialog(String title, String message, String confirmText, Runnable onConfirm) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnConfirm.setText(confirmText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            onConfirm.run();
            dialog.dismiss();
        });

        dialog.show();
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