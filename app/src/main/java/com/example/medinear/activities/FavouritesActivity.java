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
import com.example.medinear.adapters.FavouriteAdapter;
import com.example.medinear.models.Medicine;
import com.example.medinear.utils.LocalStorageHelper;
import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty, tvCount;
    private Button btnClearAll;
    private View emptyState;

    private LocalStorageHelper storageHelper;
    private FavouriteAdapter adapter;
    private List<Medicine> favouriteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        storageHelper = new LocalStorageHelper(this);

        initViews();
        loadFavourites();

        btnClearAll.setOnClickListener(v -> {
            storageHelper.clearAllData();
            loadFavourites();
            Toast.makeText(this, "All favourites cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCount = findViewById(R.id.tvCount);
        btnClearAll = findViewById(R.id.btnClearAll);
        emptyState = findViewById(R.id.emptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavouriteAdapter(favouriteList);
        recyclerView.setAdapter(adapter);
    }

    private void loadFavourites() {
        favouriteList.clear();
        favouriteList.addAll(storageHelper.getMedicines());
        adapter.notifyDataSetChanged();

        boolean isEmpty = favouriteList.isEmpty();

        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        btnClearAll.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (tvCount != null) {
            tvCount.setText(favouriteList.size() + " items");
        }
    }

    public void goToCamera(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavourites();
    }
}