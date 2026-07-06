package com.example.medinear.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageStorageHelper {

    private static final String TAG = "ImageStorageHelper";
    private final Context context;

    public ImageStorageHelper(Context context) {
        this.context = context;
    }

    /**
     * Save image to device storage (Android 10+ uses MediaStore)
     */
    public String saveImage(Bitmap bitmap) {
        String imagePath = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Use MediaStore
            imagePath = saveImageWithMediaStore(bitmap);
        } else {
            // Android 9 and below - Use external storage
            imagePath = saveImageToExternalStorage(bitmap);
        }

        return imagePath;
    }

    /**
     * Save image using MediaStore (Android 10+)
     */
    private String saveImageWithMediaStore(Bitmap bitmap) {
        String fileName = generateFileName();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MediNear");

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (uri != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                    outputStream.flush();
                    return uri.toString();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error saving image with MediaStore", e);
            }
        }
        return null;
    }

    /**
     * Save image to external storage (Android 9 and below)
     */
    private String saveImageToExternalStorage(Bitmap bitmap) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "External storage not writable");
            return null;
        }

        File picturesDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MediNear");

        if (!picturesDir.exists()) {
            if (!picturesDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return null;
            }
        }

        String fileName = generateFileName();
        File imageFile = new File(picturesDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image to external storage", e);
            return null;
        }
    }

    /**
     * Generate unique filename with timestamp
     */
    private String generateFileName() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "IMG_" + timestamp + ".jpg";
    }

    /**
     * Check if external storage is writable
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Load image from path
     */
    public Bitmap loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            if (imagePath.startsWith("content://")) {
                // Load from URI (Android 10+)
                return MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imagePath));
            } else {
                // Load from file path
                return BitmapFactory.decodeFile(imagePath);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading image", e);
            return null;
        }
    }

    /**
     * Delete image
     */
    public boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        try {
            if (imagePath.startsWith("content://")) {
                // Delete using MediaStore
                return context.getContentResolver().delete(Uri.parse(imagePath), null, null) > 0;
            } else {
                // Delete file
                File file = new File(imagePath);
                return file.exists() && file.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image", e);
            return false;
        }
    }
}