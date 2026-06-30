package com.example.medinear.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medinear.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePicture;

    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvSavedPharmacies;
    private TextView tvReminders;

    private LinearLayout rowEditProfile;
    private LinearLayout rowChangePassword;
    private LinearLayout rowLogout;

    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("MediNearPrefs", MODE_PRIVATE);

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        populateUserData();
        setupListeners();
    }

    private void initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvSavedPharmacies = findViewById(R.id.tvSavedPharmacies);
        tvReminders = findViewById(R.id.tvReminders);

        rowEditProfile = findViewById(R.id.rowEditProfile);
        rowChangePassword = findViewById(R.id.rowChangePassword);
        rowLogout = findViewById(R.id.rowLogout);
    }

    private void populateUserData() {
        String userName = currentUser.getDisplayName();

        if (userName == null || userName.isEmpty()) {
            userName = prefs.getString("user_name", "User");
        }

        tvUserName.setText(userName);
        tvUserEmail.setText(currentUser.getEmail());

        int savedCount = prefs.getInt("saved_pharmacies_count", 0);
        int reminderCount = prefs.getInt("reminders_count", 0);

        tvSavedPharmacies.setText(String.valueOf(savedCount));
        tvReminders.setText(String.valueOf(reminderCount));
    }

    private void setupListeners() {
        rowEditProfile.setOnClickListener(v -> showEditNameDialog());

        rowChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        rowLogout.setOnClickListener(v -> confirmLogout());
    }

    private void showEditNameDialog() {

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter your name");
        input.setText(tvUserName.getText().toString());

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {

                    String newName = input.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateDisplayName(newName);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDisplayName(String newName) {

        UserProfileChangeRequest request =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

        currentUser.updateProfile(request)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        prefs.edit().putString("user_name", newName).apply();

                        tvUserName.setText(newName);

                        Toast.makeText(this,
                                "Profile updated",
                                Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(this,
                                "Failed to update profile: " +
                                        (task.getException() != null
                                                ? task.getException().getMessage()
                                                : ""),
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void showChangePasswordDialog() {

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText currentPasswordInput = new EditText(this);
        currentPasswordInput.setHint("Current password");
        currentPasswordInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("New password");
        newPasswordInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

        container.addView(currentPasswordInput);
        container.addView(newPasswordInput);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(container)
                .setPositiveButton("Update", (dialog, which) -> {

                    String currentPassword =
                            currentPasswordInput.getText().toString().trim();

                    String newPassword =
                            newPasswordInput.getText().toString().trim();

                    if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                        Toast.makeText(this,
                                "Both fields are required",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this,
                                "New password must be at least 6 characters",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    reauthenticateAndChangePassword(
                            currentPassword,
                            newPassword
                    );

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reauthenticateAndChangePassword(String currentPassword,
                                                 String newPassword) {

        String email = currentUser.getEmail();

        if (email == null) {
            Toast.makeText(this,
                    "No email associated with account",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential =
                EmailAuthProvider.getCredential(email, currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {

                    if (reauthTask.isSuccessful()) {

                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {

                                    if (updateTask.isSuccessful()) {

                                        Toast.makeText(this,
                                                "Password updated successfully",
                                                Toast.LENGTH_SHORT).show();

                                    } else {

                                        Toast.makeText(this,
                                                "Failed to update password: " +
                                                        (updateTask.getException() != null
                                                                ? updateTask.getException().getMessage()
                                                                : ""),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                });

                    } else {

                        Toast.makeText(this,
                                "Current password is incorrect",
                                Toast.LENGTH_SHORT).show();

                    }

                });
    }

    private void confirmLogout() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {

                    mAuth.signOut();

                    prefs.edit().clear().apply();

                    Intent intent = new Intent(ProfileActivity.this,
                            LoginActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    finish();

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}