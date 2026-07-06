package com.example.medinear.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.medinear.models.Medicine;
import com.example.medinear.models.Pharmacy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageHelper {

    private static final String PREF_NAME = "MediNearPrefs";
    private static final String FCM_PREF_NAME = "FCM";
    private static final String KEY_FAVOURITES = "favourites";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_MEDICINES = "medicines"; // ← ADD THIS

    private final SharedPreferences prefs;
    private final SharedPreferences fcmPrefs;
    private final Gson gson;

    public LocalStorageHelper(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.fcmPrefs = context.getSharedPreferences(FCM_PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // ================ FCM TOKEN METHODS ================

    public void saveFcmToken(String token) {
        fcmPrefs.edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    public String getFcmToken() {
        return fcmPrefs.getString(KEY_FCM_TOKEN, null);
    }

    public boolean hasFcmToken() {
        return fcmPrefs.contains(KEY_FCM_TOKEN);
    }

    public void clearFcmToken() {
        fcmPrefs.edit().remove(KEY_FCM_TOKEN).apply();
    }

    // ================ MEDICINE METHODS ================

    /**
     * Add a medicine to the list
     */
    public void addMedicine(Medicine medicine) {
        List<Medicine> medicines = getMedicines();

        // Check if already exists (by name and timestamp)
        for (Medicine m : medicines) {
            if (m.getName().equals(medicine.getName()) &&
                    m.getTimestamp().equals(medicine.getTimestamp())) {
                return; // Already exists
            }
        }

        medicines.add(medicine);
        saveMedicines(medicines);
    }

    /**
     * Get all medicines
     */
    public List<Medicine> getMedicines() {
        String json = prefs.getString(KEY_MEDICINES, "[]");
        Type type = new TypeToken<List<Medicine>>() {}.getType();
        List<Medicine> medicines = gson.fromJson(json, type);
        return medicines != null ? medicines : new ArrayList<>();
    }

    /**
     * Get a specific medicine by name
     */
    public Medicine getMedicineByName(String name) {
        List<Medicine> medicines = getMedicines();
        for (Medicine medicine : medicines) {
            if (medicine.getName().equalsIgnoreCase(name)) {
                return medicine;
            }
        }
        return null;
    }

    /**
     * Get medicine by index
     */
    public Medicine getMedicineAt(int index) {
        List<Medicine> medicines = getMedicines();
        if (index >= 0 && index < medicines.size()) {
            return medicines.get(index);
        }
        return null;
    }

    /**
     * Get count of medicines
     */
    public int getMedicineCount() {
        return getMedicines().size();
    }

    /**
     * Remove a medicine
     */
    public boolean removeMedicine(String medicineName) {
        List<Medicine> medicines = getMedicines();
        boolean removed = medicines.removeIf(m -> m.getName().equalsIgnoreCase(medicineName));
        if (removed) {
            saveMedicines(medicines);
        }
        return removed;
    }

    /**
     * Remove a medicine by index
     */
    public boolean removeMedicineAt(int index) {
        List<Medicine> medicines = getMedicines();
        if (index >= 0 && index < medicines.size()) {
            medicines.remove(index);
            saveMedicines(medicines);
            return true;
        }
        return false;
    }

    /**
     * Clear all medicines
     */
    public void clearMedicines() {
        prefs.edit().remove(KEY_MEDICINES).apply();
    }

    /**
     * Save medicines list to SharedPreferences
     */
    private void saveMedicines(List<Medicine> medicines) {
        String json = gson.toJson(medicines);
        prefs.edit().putString(KEY_MEDICINES, json).apply();
    }

    // ================ FAVOURITES METHODS ================

    public void addFavouritePharmacy(Pharmacy pharmacy) {
        List<Pharmacy> favourites = getFavouritePharmacies();

        for (Pharmacy p : favourites) {
            if (p.getId() != null && p.getId().equals(pharmacy.getId())) {
                return;
            }
        }

        favourites.add(pharmacy);
        saveFavourites(favourites);
    }

    public void removeFavouritePharmacy(String pharmacyId) {
        List<Pharmacy> favourites = getFavouritePharmacies();
        favourites.removeIf(p -> p.getId() != null && p.getId().equals(pharmacyId));
        saveFavourites(favourites);
    }

    public List<Pharmacy> getFavouritePharmacies() {
        String json = prefs.getString(KEY_FAVOURITES, "[]");
        Type type = new TypeToken<List<Pharmacy>>() {}.getType();
        List<Pharmacy> favourites = gson.fromJson(json, type);
        return favourites != null ? favourites : new ArrayList<>();
    }

    public boolean isFavouritePharmacy(String pharmacyId) {
        if (pharmacyId == null) return false;
        List<Pharmacy> favourites = getFavouritePharmacies();
        for (Pharmacy p : favourites) {
            if (p.getId() != null && p.getId().equals(pharmacyId)) {
                return true;
            }
        }
        return false;
    }

    private void saveFavourites(List<Pharmacy> favourites) {
        String json = gson.toJson(favourites);
        prefs.edit().putString(KEY_FAVOURITES, json).apply();
    }

    public void clearFavourites() {
        prefs.edit().remove(KEY_FAVOURITES).apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
        fcmPrefs.edit().clear().apply();
    }
}