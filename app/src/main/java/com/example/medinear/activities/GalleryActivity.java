package com.example.medinear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medinear.R;
import com.example.medinear.adapters.ImageAdapter;
import com.example.medinear.models.Medicine;
import com.example.medinear.utils.LocalStorageHelper;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private GridView gridView;
    private TextView tvEmpty;
    private LocalStorageHelper storageHelper;

    // ✅ Changed to ArrayList
    private ArrayList<String> imagePaths = new ArrayList<>();
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        storageHelper = new LocalStorageHelper(this);

        gridView = findViewById(R.id.gridView);
        tvEmpty = findViewById(R.id.tvEmpty);

        // ✅ Initialize adapter with ArrayList
        imageAdapter = new ImageAdapter(this, imagePaths);
        gridView.setAdapter(imageAdapter);

        loadImages();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < imagePaths.size()) {
                String imagePath = imagePaths.get(position);
                Intent intent = new Intent(GalleryActivity.this, ImageViewActivity.class);
                intent.putExtra("imagePath", imagePath);
                startActivity(intent);
            }
        });
    }

    private void loadImages() {
        List<Medicine> medicines = storageHelper.getMedicines();
        imagePaths.clear();

        for (Medicine medicine : medicines) {
            if (medicine.getImagePath() != null && !medicine.getImagePath().isEmpty()) {
                imagePaths.add(medicine.getImagePath());
            }
        }

        imageAdapter.notifyDataSetChanged();
        tvEmpty.setVisibility(imagePaths.isEmpty() ? View.VISIBLE : View.GONE);
        gridView.setVisibility(imagePaths.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImages();
    }
}