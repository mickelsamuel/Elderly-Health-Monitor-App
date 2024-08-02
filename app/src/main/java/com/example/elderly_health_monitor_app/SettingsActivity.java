package com.example.elderly_health_monitor_app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekBarFontSize;
    private TextView textSample, textFontSizeLabel, textEditAccountInfo;
    private TextInputEditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextEmergencyContact, editTextMedicalCard, editTextUserId, inputCurrentPassword, inputNewPassword, inputConfirmNewPassword;
    private MaterialButton buttonSaveChanges, buttonLogout, buttonChangePassword, buttonDeleteAccount;
    private DatabaseReference databaseRef;
    private String currentUserId;
    private float currentFontSize = 18;

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the intent and retrieve the passed details
        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("userId");
        Log.d(TAG, "onCreate: Received userId from intent: " + currentUserId);

        // If currentUserId is null or empty, try to get it from SharedPreferences
        if (currentUserId == null || currentUserId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            currentUserId = prefs.getString("current_user_id", "");
            Log.d(TAG, "onCreate: Retrieved userId from SharedPreferences: " + currentUserId);
        }

        // Store userId in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_user_id", currentUserId);
        editor.apply();

        if (currentUserId != null && !currentUserId.isEmpty()) {
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            initializeViews();
            setupFontSizeAdjustment();
            setupSaveChanges();
            setupLogout();
            setupChangePassword();
            setupDeleteAccount();
            loadUserDetails();
        } else {
            Log.e(TAG, "No user ID provided");
            Toast.makeText(this, "No user ID provided", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }

    private void initializeViews() {
        seekBarFontSize = findViewById(R.id.seekBar_font_size);
        textSample = findViewById(R.id.text_sample);
        textFontSizeLabel = findViewById(R.id.text_font_size_label);
        textEditAccountInfo = findViewById(R.id.text_edit_account_info);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextEmergencyContact = findViewById(R.id.editTextEmergencyContact);
        editTextMedicalCard = findViewById(R.id.editTextMedicalCard);
        editTextUserId = findViewById(R.id.editTextUserId);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);

        Log.d(TAG, "initializeViews: Using userId: " + currentUserId);

        TextInputLayout tilUserId = findViewById(R.id.tilUserId);
        tilUserId.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyUserIdToClipboard();
            }
        });
    }

    private void loadUserDetails() {
        Log.d(TAG, "loadUserDetails: Fetching user details for userId: " + currentUserId);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String emergencyContact = snapshot.child("emergencyContact").getValue(String.class);
                    String medicalCard = snapshot.child("medicalCard").getValue(String.class);

                    // Set the text and hint for each field
                    setField(editTextFirstName, firstName);
                    setField(editTextLastName, lastName);
                    setField(editTextPhoneNumber, phoneNumber);
                    setField(editTextEmergencyContact, emergencyContact);
                    setField(editTextMedicalCard, medicalCard);
                    setField(editTextUserId, currentUserId);

                    // Set text color to ensure visibility
                    int textColor = getResources().getColor(android.R.color.black);
                    editTextFirstName.setTextColor(textColor);
                    editTextLastName.setTextColor(textColor);
                    editTextPhoneNumber.setTextColor(textColor);
                    editTextEmergencyContact.setTextColor(textColor);
                    editTextMedicalCard.setTextColor(textColor);
                    editTextUserId.setTextColor(textColor);

                    Log.d(TAG, "loadUserDetails: User data loaded successfully");
                } else {
                    Log.e(TAG, "No data found for user: " + currentUserId);
                    Toast.makeText(SettingsActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                    // Optionally, navigate back or handle the case
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load user details", error.toException());
                Toast.makeText(SettingsActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFontSizeAdjustment() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        currentFontSize = prefs.getFloat("font_size", 18);
        seekBarFontSize.setProgress((int) currentFontSize);

        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentFontSize = progress;
                applyFontSize();

                // Save the font size preference
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat("font_size", currentFontSize);
                editor.apply();

                // Update font size in other activities
                updateFontSizeInActivities(currentFontSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        applyFontSize();
    }

    private void applyFontSize() {
        textSample.setTextSize(currentFontSize);
        textFontSizeLabel.setTextSize(currentFontSize);
        textEditAccountInfo.setTextSize(currentFontSize);
        editTextFirstName.setTextSize(currentFontSize);
        editTextLastName.setTextSize(currentFontSize);
        editTextPhoneNumber.setTextSize(currentFontSize);
        editTextEmergencyContact.setTextSize(currentFontSize);
        editTextMedicalCard.setTextSize(currentFontSize);
        editTextUserId.setTextSize(currentFontSize);
        buttonSaveChanges.setTextSize(currentFontSize);
        buttonLogout.setTextSize(currentFontSize);
        buttonChangePassword.setTextSize(currentFontSize);
        buttonDeleteAccount.setTextSize(currentFontSize);
    }

    private void updateFontSizeInActivities(float fontSize) {
        Intent intent = new Intent("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE");
        intent.putExtra("font_size", fontSize);
        sendBroadcast(intent);
    }

    private void setupSaveChanges() {
        buttonSaveChanges.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String emergencyContact = editTextEmergencyContact.getText().toString().trim();

            if (phoneNumber.isEmpty() || !phoneNumber.matches("[0-9]+") || phoneNumber.length() != 10) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (emergencyContact.isEmpty() || !emergencyContact.matches("[0-9]+") || emergencyContact.length() != 10) {
                Toast.makeText(this, "Please enter a valid emergency contact number", Toast.LENGTH_SHORT).show();
                return;
            }

            saveChanges(phoneNumber, emergencyContact);
        });
    }

    private void saveChanges(String phoneNumber, String emergencyContact) {
        // Update the current user data
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", phoneNumber);
        updates.put("emergencyContact", emergencyContact);

        databaseRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(SettingsActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
            // Reflect changes in MonitorActivity
            Intent monitorIntent = new Intent(SettingsActivity.this, MonitorActivity.class);
            monitorIntent.putExtra("userId", currentUserId);
            startActivity(monitorIntent);
            finish();
        }).addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to update details", Toast.LENGTH_SHORT).show());
    }

    private void setupLogout() {
        buttonLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        clearLoginPreferences();
        navigateToLogin();
    }

    private void setupChangePassword() {
        buttonChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        inputCurrentPassword = viewInflated.findViewById(R.id.input_current_password);
        inputNewPassword = viewInflated.findViewById(R.id.input_new_password);
        inputConfirmNewPassword = viewInflated.findViewById(R.id.input_confirm_new_password);

        builder.setView(viewInflated);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPassword = inputCurrentPassword.getText().toString();
            String newPassword = inputNewPassword.getText().toString();
            String confirmNewPassword = inputConfirmNewPassword.getText().toString();

            if (newPassword.equals(confirmNewPassword)) {
                changePassword(currentPassword, newPassword);
            } else {
                Toast.makeText(SettingsActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "No authenticated user found.");
            Toast.makeText(SettingsActivity.this, "No authenticated user found. Please log in again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Re-authenticate the user manually by checking the current password
        databaseRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String storedPassword = dataSnapshot.getValue(String.class);
                if (storedPassword != null && storedPassword.equals(currentPassword)) {
                    // Update the password in the database
                    databaseRef.child("password").setValue(newPassword).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password updated successfully");
                            Toast.makeText(SettingsActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Failed to update password", task.getException());
                            Toast.makeText(SettingsActivity.this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Authentication failed. Please check your current password.");
                    Toast.makeText(SettingsActivity.this, "Authentication failed. Please check your current password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read password", databaseError.toException());
                Toast.makeText(SettingsActivity.this, "Failed to read password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDeleteAccount() {
        buttonDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmationDialog());
    }

    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> showDeleteAccountPasswordDialog())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Password");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_password, null);
        inputCurrentPassword = viewInflated.findViewById(R.id.input_current_password);

        builder.setView(viewInflated);

        builder.setPositiveButton("Delete Account", (dialog, which) -> {
            String currentPassword = inputCurrentPassword.getText().toString();
            deleteAccount(currentPassword);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteAccount(String currentPassword) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "No authenticated user found.");
            Toast.makeText(SettingsActivity.this, "No authenticated user found. Please log in again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Re-authenticate the user manually by checking the current password
        databaseRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String storedPassword = dataSnapshot.getValue(String.class);
                if (storedPassword != null && storedPassword.equals(currentPassword)) {
                    // Delete user data from the database
                    databaseRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Account deleted successfully");
                            Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            clearLoginPreferences();
                            navigateToLogin(); // Navigate to login page
                        } else {
                            Log.e(TAG, "Failed to delete account", task.getException());
                            Toast.makeText(SettingsActivity.this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Authentication failed. Please check your current password.");
                    Toast.makeText(SettingsActivity.this, "Authentication failed. Please check your current password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read password", databaseError.toException());
                Toast.makeText(SettingsActivity.this, "Failed to read password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearLoginPreferences() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    private void navigateToLogin() {
        Log.d(TAG, "Navigating to LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Log.d(TAG, "Finished navigating to LoginActivity");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setField(TextInputEditText field, String value) {
        if (value != null && !value.isEmpty()) {
            field.setText(value);
        }
    }

    private void copyUserIdToClipboard() {
        String userId = editTextUserId.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("User ID", userId);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "User ID copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
