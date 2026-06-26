package com.example.medinear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.medinear.models.Reminder;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReminderStorageHelper {

    private SharedPreferences prefs;
    private Gson gson;
    private static final String PREF_NAME = "MediNearReminders";
    private static final String KEY_REMINDERS = "reminders";

    public ReminderStorageHelper(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveReminders(List<Reminder> reminders) {
        String json = gson.toJson(reminders);
        prefs.edit().putString(KEY_REMINDERS, json).apply();
    }

    public List<Reminder> getReminders() {
        String json = prefs.getString(KEY_REMINDERS, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<List<Reminder>>(){}.getType();
            List<Reminder> reminders = gson.fromJson(json, type);
            return reminders != null ? reminders : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void addReminder(Reminder reminder) {
        List<Reminder> reminders = getReminders();
        reminders.add(reminder);
        saveReminders(reminders);
    }

    public void deleteReminder(Reminder reminder) {
        List<Reminder> reminders = getReminders();
        // Find and remove by ID
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminder.getId())) {
                reminders.remove(i);
                break;
            }
        }
        saveReminders(reminders);
    }

    public void deleteReminderById(String id) {
        List<Reminder> reminders = getReminders();
        reminders.removeIf(r -> r.getId().equals(id));
        saveReminders(reminders);
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}