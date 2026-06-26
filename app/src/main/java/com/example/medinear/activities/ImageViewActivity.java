package com.example.medinear.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medinear.R;
import com.example.medinear.utils.LocalStorageHelper;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView fullImageView;
    private LocalStorageHelper storageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        storageHelper = new LocalStorageHelper(this);
        fullImageView = findViewById(R.id.fullImageView);

        String imagePath = getIntent().getStringExtra("imagePath");

        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "No image to display", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Bitmap bitmap = storageHelper.loadImage(imagePath);
        if (bitmap != null) {
            fullImageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            finish();
        }

        fullImageView.setOnClickListener(v -> finish());
    }
}