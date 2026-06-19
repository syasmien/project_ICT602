package com.example.medinear.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.medinear.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 200;

    private PreviewView previewView;
    private ImageView ivCapturedPhoto;
    private Button btnCapture, btnRetake, btnSave;
    private EditText etMedicineName, etNotes;
    private ProgressBar progressBar;
    private TextView tvStatus;

    private ImageCapture imageCapture;
    private Uri capturedImageUri = null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mAuth   = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        previewView     = findViewById(R.id.previewView);
        ivCapturedPhoto = findViewById(R.id.ivCapturedPhoto);
        btnCapture      = findViewById(R.id.btnCapture);
        btnRetake       = findViewById(R.id.btnRetake);
        btnSave         = findViewById(R.id.btnSave);
        etMedicineName  = findViewById(R.id.etMedicineName);
        etNotes         = findViewById(R.id.etNotes);
        progressBar     = findViewById(R.id.progressBar);
        tvStatus        = findViewById(R.id.tvStatus);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Capture Medicine");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initially hide retake and save
        btnRetake.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        ivCapturedPhoto.setVisibility(View.GONE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }

        btnCapture.setOnClickListener(v -> capturePhoto());
        btnRetake.setOnClickListener(v -> retakePhoto());
        btnSave.setOnClickListener(v -> saveMedicineRecord());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
            ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Image capture
                imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

                // Use back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Camera error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        // Create output file in MediaStore
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,
            "MediNear_" + new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date()));
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputOptions =
            new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues).build();

        btnCapture.setEnabled(false);
        tvStatus.setText("Capturing...");

        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(this),
            new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                    capturedImageUri = output.getSavedUri();
                    showCapturedPhoto();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    btnCapture.setEnabled(true);
                    tvStatus.setText("Capture failed. Try again.");
                    Toast.makeText(CameraActivity.this,
                        "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showCapturedPhoto() {
        previewView.setVisibility(View.GONE);
        btnCapture.setVisibility(View.GONE);
        ivCapturedPhoto.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);

        ivCapturedPhoto.setImageURI(capturedImageUri);
        tvStatus.setText("Photo captured! Add details and save.");
    }

    private void retakePhoto() {
        capturedImageUri = null;
        previewView.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.VISIBLE);
        btnCapture.setEnabled(true);
        ivCapturedPhoto.setVisibility(View.GONE);
        btnRetake.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        tvStatus.setText("Point camera at medicine packaging");
    }

    private void saveMedicineRecord() {
        String medicineName = etMedicineName.getText().toString().trim();
        String notes        = etNotes.getText().toString().trim();

        if (medicineName.isEmpty()) {
            etMedicineName.setError("Enter medicine name");
            return;
        }
        if (capturedImageUri == null) {
            Toast.makeText(this, "Please capture a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        tvStatus.setText("Uploading...");

        String userId = mAuth.getCurrentUser().getUid();
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Upload photo to Firebase Storage
        StorageReference ref = storage.getReference()
            .child("medicine_photos/" + userId + "/" + timestamp + ".jpg");

        ref.putFile(capturedImageUri)
            .addOnSuccessListener(taskSnapshot ->
                ref.getDownloadUrl().addOnSuccessListener(uri ->
                    saveMedicineToFirestore(userId, medicineName, notes, uri.toString(), timestamp)))
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                tvStatus.setText("Upload failed. Try again.");
                Toast.makeText(this, "Upload error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void saveMedicineToFirestore(String userId, String name,
                                          String notes, String photoUrl, String timestamp) {
        Map<String, Object> record = new HashMap<>();
        record.put("userId", userId);
        record.put("medicineName", name);
        record.put("notes", notes);
        record.put("photoUrl", photoUrl);
        record.put("capturedAt", Long.parseLong(timestamp));

        db.collection("medicine_records").add(record)
            .addOnSuccessListener(ref -> {
                progressBar.setVisibility(View.GONE);
                tvStatus.setText("✅ Saved successfully!");
                Toast.makeText(this, "Medicine record saved!", Toast.LENGTH_SHORT).show();
                // Go back after 1 second
                new android.os.Handler().postDelayed(this::finish, 1500);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                tvStatus.setText("Save failed. Try again.");
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
