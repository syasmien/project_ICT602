package com.example.medinear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.medinear.models.Medicine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocalStorageHelper {

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private static final String PREF_NAME = "MediNearData";
    private static final String KEY_MEDICINES = "medicines";

    public LocalStorageHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // ============ IMAGE METHODS ============

    public String saveImage(Bitmap bitmap) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "MED_" + timestamp + ".jpg";

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = context.getFilesDir();
        }

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File imageFile = new File(storageDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        File file = new File(path);
        if (file.exists()) {
            return BitmapFactory.decodeFile(path);
        }
        return null;
    }

    public boolean deleteImage(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        return file.exists() && file.delete();
    }

    public List<String> getAllImagePaths() {
        List<String> paths = new ArrayList<>();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                        paths.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return paths;
    }

    // ============ DATA METHODS ============

    public void saveMedicines(List<Medicine> medicines) {
        String json = gson.toJson(medicines);
        prefs.edit().putString(KEY_MEDICINES, json).apply();
    }

    public List<Medicine> getMedicines() {
        String json = prefs.getString(KEY_MEDICINES, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Medicine>>(){}.getType();
        List<Medicine> medicines = gson.fromJson(json, type);
        return medicines != null ? medicines : new ArrayList<>();
    }

    public void addMedicine(Medicine medicine) {
        List<Medicine> medicines = getMedicines();
        medicines.add(medicine);
        saveMedicines(medicines);
    }

    public void deleteMedicine(Medicine medicine) {
        List<Medicine> medicines = getMedicines();
        if (medicine.getImagePath() != null) {
            deleteImage(medicine.getImagePath());
        }
        medicines.remove(medicine);
        saveMedicines(medicines);
    }

    public void deleteMedicineByName(String name) {
        List<Medicine> medicines = getMedicines();
        medicines.removeIf(m -> m.getName().equalsIgnoreCase(name));
        saveMedicines(medicines);
    }

    public void clearAllData() {
        List<String> imagePaths = getAllImagePaths();
        for (String path : imagePaths) {
            deleteImage(path);
        }
        prefs.edit().clear().apply();
    }
}