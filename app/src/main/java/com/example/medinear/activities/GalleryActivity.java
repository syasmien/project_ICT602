package com.example.medinear.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        imageAdapter.setOnDeleteClickListener(this::confirmDeleteImage);

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

    // CHANGED - guna custom dialog_confirm.xml, bukan default AlertDialog.Builder
    private void confirmDeleteImage(int position) {
        if (position < 0 || position >= imagePaths.size()) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText("Delete image?");
        tvMessage.setText("This image and its medicine record will be permanently removed.");
        btnConfirm.setText("Delete");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            deleteImageAt(position);
            dialog.dismiss();
        });

        dialog.show();
    }

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
            storageHelper.removeMedicine(match.getName());
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