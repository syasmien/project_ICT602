package com.example.medinear.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    private ArrayList<String> imagePaths = new ArrayList<>();
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        storageHelper = new LocalStorageHelper(this);

        gridView = findViewById(R.id.gridView);
        tvEmpty = findViewById(R.id.tvEmpty);

        imageAdapter = new ImageAdapter(this, imagePaths);
        gridView.setAdapter(imageAdapter);

        imageAdapter.setOnDeleteClickListener(this::confirmDeleteImage); // NEW

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

    // NEW - confirmation dialog before deleting
    private void confirmDeleteImage(int position) {
        if (position < 0 || position >= imagePaths.size()) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteImageAt(position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // NEW - removes the matching Medicine (which also deletes the file on disk)
    private void deleteImageAt(int position) {
        if (position < 0 || position >= imagePaths.size()) return;

        String pathToDelete = imagePaths.get(position);

        List<Medicine> medicines = storageHelper.getMedicines();
        Medicine match = null;
        for (Medicine medicine : medicines) {
            if (pathToDelete.equals(medicine.getImagePath())) {
                match = medicine;
                break;
            }
        }

        if (match != null) {
            storageHelper.removeMedicine(match.getName()); // removes from saved list by name
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could not find matching entry", Toast.LENGTH_SHORT).show();
        }

        loadImages();
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