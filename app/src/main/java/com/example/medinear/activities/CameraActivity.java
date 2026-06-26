package com.example.medinear.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.medinear.R;
import com.example.medinear.models.Medicine;
import com.example.medinear.utils.LocalStorageHelper;
import com.example.medinear.utils.NotificationHelper;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private TextView tvStatus;
    private ImageView ivCapturedPhoto;
    private EditText etMedicineName, etNotes;
    private Button btnCapture, btnRetake, btnSave, btnGallery;
    private ProgressBar progressBar;

    private LocalStorageHelper storageHelper;
    private Bitmap capturedBitmap;
    private boolean isPhotoCaptured = false;
    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        storageHelper = new LocalStorageHelper(this);
        cameraExecutor = Executors.newSingleThreadExecutor();

        initViews();
        setupClickListeners();

        if (checkCameraPermission()) {
            startCamera();
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        ivCapturedPhoto = findViewById(R.id.ivCapturedPhoto);
        etMedicineName = findViewById(R.id.etMedicineName);
        etNotes = findViewById(R.id.etNotes);
        btnCapture = findViewById(R.id.btnCapture);
        btnRetake = findViewById(R.id.btnRetake);
        btnSave = findViewById(R.id.btnSave);
        btnGallery = findViewById(R.id.btnGallery);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setupClickListeners() {
        btnCapture.setOnClickListener(v -> capturePhoto());
        btnRetake.setOnClickListener(v -> retakePhoto());
        btnSave.setOnClickListener(v -> saveRecord());
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(CameraActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,
                        preview, imageCapture);

                tvStatus.setText("Camera ready");

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCapture.setEnabled(false);
        tvStatus.setText("Capturing...");

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "MED_" + timestamp + ".jpg";

        android.content.ContentValues contentValues = new android.content.ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build();

        imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri imageUri = outputFileResults.getSavedUri();
                        if (imageUri != null) {
                            try {
                                capturedBitmap = MediaStore.Images.Media.getBitmap(
                                        getContentResolver(), imageUri);
                                showCapturedPhoto();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(CameraActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraActivity.this, "Failed to capture", Toast.LENGTH_SHORT).show();
                        btnCapture.setEnabled(true);
                        tvStatus.setText("Failed to capture");
                    }
                });
    }

    private void showCapturedPhoto() {
        isPhotoCaptured = true;
        ivCapturedPhoto.setImageBitmap(capturedBitmap);
        ivCapturedPhoto.setVisibility(View.VISIBLE);
        previewView.setVisibility(View.GONE);

        btnCapture.setVisibility(View.GONE);
        btnRetake.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);

        tvStatus.setText("Photo captured - Enter details and save");
    }

    private void retakePhoto() {
        isPhotoCaptured = false;
        capturedBitmap = null;

        ivCapturedPhoto.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);
        ivCapturedPhoto.setImageBitmap(null);

        btnCapture.setVisibility(View.VISIBLE);
        btnCapture.setEnabled(true);
        btnRetake.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);

        tvStatus.setText("Ready to capture");
    }

    private void saveRecord() {
        if (!isPhotoCaptured || capturedBitmap == null) {
            Toast.makeText(this, "Please capture a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        String medicineName = etMedicineName.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (medicineName.isEmpty()) {
            etMedicineName.setError("Medicine name required");
            etMedicineName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        String imagePath = storageHelper.saveImage(capturedBitmap);
        if (imagePath == null) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            btnSave.setText("Save Record");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        Medicine medicine = new Medicine(medicineName, notes, imagePath, timestamp);
        storageHelper.addMedicine(medicine);

        progressBar.setVisibility(View.GONE);
        btnSave.setEnabled(true);
        btnSave.setText("Save Record");

        Toast.makeText(this, "✅ Medicine saved!", Toast.LENGTH_LONG).show();

        // Send notification
        NotificationHelper.sendMedicineReminderNotification(
                this,
                medicineName,
                "Saved successfully",
                new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date())
        );

        etMedicineName.setText("");
        etNotes.setText("");
        retakePhoto();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}