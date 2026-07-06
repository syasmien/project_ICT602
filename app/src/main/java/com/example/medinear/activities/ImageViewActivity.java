package com.example.medinear.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medinear.R;
import com.example.medinear.models.Medicine;
import com.example.medinear.utils.ImageStorageHelper;
import com.example.medinear.utils.LocalStorageHelper;

import java.util.List;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView fullImageView;
    private LinearLayout captionContainer; // NEW
    private TextView tvMedicineName, tvMedicineNotes, tvMedicineTimestamp; // NEW
    private LocalStorageHelper storageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        storageHelper = new LocalStorageHelper(this);
        fullImageView = findViewById(R.id.fullImageView);

        // NEW - caption views
        captionContainer = findViewById(R.id.captionContainer);
        tvMedicineName = findViewById(R.id.tvMedicineName);
        tvMedicineNotes = findViewById(R.id.tvMedicineNotes);
        tvMedicineTimestamp = findViewById(R.id.tvMedicineTimestamp);

        String imagePath = getIntent().getStringExtra("imagePath");

        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "No image to display", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageStorageHelper imageStorageHelper = new ImageStorageHelper(this);
        Bitmap bitmap = imageStorageHelper.loadImage(imagePath);
        if (bitmap != null) {
            fullImageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateCaption(imagePath); // NEW

        fullImageView.setOnClickListener(v -> toggleCaption()); // CHANGED - was finish(); tap now toggles caption instead
    }

    // NEW - finds the Medicine record whose imagePath matches this photo and fills in the caption
    private void populateCaption(String imagePath) {
        List<Medicine> medicines = storageHelper.getMedicines();
        Medicine match = null;

        for (Medicine medicine : medicines) {
            if (imagePath.equals(medicine.getImagePath())) {
                match = medicine;
                break;
            }
        }

        if (match != null) {
            tvMedicineName.setText(match.getName());

            String notes = match.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                tvMedicineNotes.setText(notes);
                tvMedicineNotes.setVisibility(View.VISIBLE);
            } else {
                tvMedicineNotes.setVisibility(View.GONE);
            }

            String timestamp = match.getTimestamp();
            if (timestamp != null && !timestamp.isEmpty()) {
                tvMedicineTimestamp.setText("Saved on " + timestamp);
                tvMedicineTimestamp.setVisibility(View.VISIBLE);
            } else {
                tvMedicineTimestamp.setVisibility(View.GONE);
            }
        } else {
            // No matching record found — hide the caption entirely
            captionContainer.setVisibility(View.GONE);
        }
    }

    // NEW - lets the user tap the photo to hide/show the caption instead of immediately closing
    private void toggleCaption() {
        captionContainer.setVisibility(
                captionContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // CHANGED - back button now closes the screen since tap no longer does
    }
}