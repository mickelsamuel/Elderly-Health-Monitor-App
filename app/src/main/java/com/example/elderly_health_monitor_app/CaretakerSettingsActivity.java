package com.example.elderly_health_monitor_app;

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

public class CaretakerSettingsActivity extends AppCompatActivity {

    // UI components
    private SeekBar seekBarFontSize;
    private TextView textSample, textFontSizeLabel, textEditAccountInfo, userIdText;
    private TextInputEditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextLicense, editTextMedicalCard, inputCurrentPassword, inputNewPassword, inputConfirmNewPassword;
    private MaterialButton buttonSaveChanges, buttonChangePassword, buttonDeleteAccount, buttonLogout;
    private TextInputLayout tilFirstName, tilLastName, tilPhoneNumber, tilLicense, tilMedicalCard, tilUserId;

    // Firebase database reference
    private DatabaseReference databaseRef;
    private String caretakerLicense;
    private float currentFontSize = 18;

    private static final String TAG = "CaretakerSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caretaker_settings);

        // Enable back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get caretaker license from intent or shared preferences
        Intent intent = getIntent();
        caretakerLicense = intent.getStringExtra("caretakerLicense");
        Log.d(TAG, "onCreate: Received caretakerLicense from intent: " + caretakerLicense);

        if (caretakerLicense == null || caretakerLicense.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            caretakerLicense = prefs.getString("caretaker_license", "");
            Log.d(TAG, "onCreate: Retrieved caretakerLicense from SharedPreferences: " + caretakerLicense);
        }

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("caretaker_license", caretakerLicense);
        editor.apply();

        // If caretaker license is available, initialize the views and load details
        if (caretakerLicense != null && !caretakerLicense.isEmpty()) {
            databaseRef = FirebaseDatabase.getInstance().getReference("users").child(caretakerLicense);
            initializeViews();
            setupFontSizeAdjustment();
            setupSaveChanges();
            setupChangePassword();
            setupDeleteAccount();
            setupLogout();
            loadCaretakerDetails();
        } else {
            Log.e(TAG, "No caretaker license provided");
            Toast.makeText(this, "No caretaker license provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initialize the views by finding them in the layout
     */
    private void initializeViews() {
        seekBarFontSize = findViewById(R.id.seekBar_font_size);
        textSample = findViewById(R.id.text_sample);
        textFontSizeLabel = findViewById(R.id.text_font_size_label);
        textEditAccountInfo = findViewById(R.id.text_edit_account_info);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextLicense = findViewById(R.id.editTextLicense);
        editTextMedicalCard = findViewById(R.id.editTextMedicalCard);
        userIdText = findViewById(R.id.editTextUserId);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        buttonLogout = findViewById(R.id.buttonLogout);

        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilLicense = findViewById(R.id.tilLicense);
        tilMedicalCard = findViewById(R.id.tilMedicalCard);
        tilUserId = findViewById(R.id.tilUserId);

        Log.d(TAG, "initializeViews: Using caretakerLicense: " + caretakerLicense);
    }

    /**
     * Load caretaker details from Firebase
     */
    private void loadCaretakerDetails() {
        Log.d(TAG, "loadCaretakerDetails: Fetching caretaker details for caretakerLicense: " + caretakerLicense);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(TAG, "Data snapshot: " + snapshot.toString());
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String license = snapshot.child("license").getValue(String.class);
                    String medicalCard = snapshot.child("medicalCard").getValue(String.class);
                    String userId = snapshot.child("id").getValue(String.class);

                    setField(editTextFirstName, firstName);
                    setField(editTextLastName, lastName);
                    setField(editTextPhoneNumber, phoneNumber);
                    setField(editTextLicense, license);
                    setField(editTextMedicalCard, medicalCard);
                    setField(userIdText, userId);

                    int textColor = getResources().getColor(android.R.color.black);
                    editTextFirstName.setTextColor(textColor);
                    editTextLastName.setTextColor(textColor);
                    editTextPhoneNumber.setTextColor(textColor);
                    editTextLicense.setTextColor(textColor);
                    editTextMedicalCard.setTextColor(textColor);
                    userIdText.setTextColor(textColor);

                    Log.d(TAG, "loadCaretakerDetails: Caretaker data loaded successfully");
                } else {
                    Log.e(TAG, "No data found for caretaker: " + caretakerLicense);
                    Toast.makeText(CaretakerSettingsActivity.this, "No caretaker data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load caretaker details", error.toException());
                Toast.makeText(CaretakerSettingsActivity.this, "Failed to load caretaker details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set text for a TextInputEditText field
     * @param field The TextInputEditText field
     * @param value The value to set
     */
    private void setField(TextInputEditText field, String value) {
        if (value != null && !value.isEmpty()) {
            field.setText(value);
        }
    }

    /**
     * Set text for a TextView field
     * @param field The TextView field
     * @param value The value to set
     */
    private void setField(TextView field, String value) {
        if (value != null && !value.isEmpty()) {
            field.setText(value);
        }
    }

    /**
     * Set up the font size adjustment seek bar
     */
    private void setupFontSizeAdjustment() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        currentFontSize = prefs.getFloat("font_size", 18);
        seekBarFontSize.setProgress((int) currentFontSize);

        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentFontSize = progress;
                applyFontSize();

                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat("font_size", currentFontSize);
                editor.apply();

                updateFontSizeInActivities(currentFontSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        applyFontSize();
    }

    /**
     * Apply the font size to UI components
     */
    private void applyFontSize() {
        textSample.setTextSize(currentFontSize);
        textFontSizeLabel.setTextSize(currentFontSize);
        textEditAccountInfo.setTextSize(currentFontSize);
        editTextFirstName.setTextSize(currentFontSize);
        editTextLastName.setTextSize(currentFontSize);
        editTextPhoneNumber.setTextSize(currentFontSize);
        editTextLicense.setTextSize(currentFontSize);
        editTextMedicalCard.setTextSize(currentFontSize);
        userIdText.setTextSize(currentFontSize);
        buttonSaveChanges.setTextSize(currentFontSize);
        buttonChangePassword.setTextSize(currentFontSize);
        buttonDeleteAccount.setTextSize(currentFontSize);
        buttonLogout.setTextSize(currentFontSize);
    }

    /**
     * Update the font size in all activities by sending a broadcast
     * @param fontSize The font size to set
     */
    private void updateFontSizeInActivities(float fontSize) {
        Intent intent = new Intent("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE");
        intent.putExtra("font_size", fontSize);
        sendBroadcast(intent);
    }

    /**
     * Set up the save changes button
     */
    private void setupSaveChanges() {
        buttonSaveChanges.setOnClickListener(v -> {
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String license = editTextLicense.getText().toString().trim();
            String medicalCard = editTextMedicalCard.getText().toString().trim();

            if (!validatePhoneNumber(phoneNumber)) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            saveChanges(firstName, lastName, phoneNumber, license, medicalCard);
        });
    }

    // Validate phone number
    private boolean validatePhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && phoneNumber.matches("\\d{10}");
    }

    /**
     * Save changes to the caretaker's details in Firebase
     * @param firstName The first name of the caretaker
     * @param lastName The last name of the caretaker
     * @param phoneNumber The phone number of the caretaker
     * @param license The license of the caretaker
     * @param medicalCard The medical card of the caretaker
     */
    private void saveChanges(String firstName, String lastName, String phoneNumber, String license, String medicalCard) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phoneNumber", phoneNumber);
        updates.put("license", license);
        updates.put("medicalCard", medicalCard);

        databaseRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(CaretakerSettingsActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
            updateCaretakerPhoneNumberInPatients(phoneNumber);
            finish();
        }).addOnFailureListener(e -> Toast.makeText(CaretakerSettingsActivity.this, "Failed to update details", Toast.LENGTH_SHORT).show());
    }

    /**
     * Update the caretaker's phone number in the patients' records
     * @param newPhoneNumber The new phone number
     */
    private void updateCaretakerPhoneNumberInPatients(String newPhoneNumber) {
        databaseRef.child("patientIDs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                        String patientId = patientSnapshot.getValue(String.class);
                        if (patientId != null && !patientId.isEmpty()) {
                            DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("users").child(patientId);
                            patientRef.child("caretakerPhoneNumber").setValue(newPhoneNumber)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Caretaker phone number updated for patient: " + patientId))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update caretaker phone number for patient: " + patientId, e));
                        }
                    }
                } else {
                    Log.e(TAG, "No patient IDs found for caretaker: " + caretakerLicense);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load patient IDs", error.toException());
            }
        });
    }

    /**
     * Set up the change password button
     */
    private void setupChangePassword() {
        buttonChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    /**
     * Show the change password dialog
     */
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

            if (!validatePassword(newPassword)) {
                Toast.makeText(CaretakerSettingsActivity.this, "New password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            } else if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(CaretakerSettingsActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                changePassword(currentPassword, newPassword);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Change the caretaker's password in Firebase
     * @param currentPassword The current password
     * @param newPassword The new password
     */
    private void changePassword(String currentPassword, String newPassword) {
        if (caretakerLicense == null || caretakerLicense.isEmpty()) {
            Log.e(TAG, "No authenticated caretaker found.");
            Toast.makeText(CaretakerSettingsActivity.this, "No authenticated caretaker found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String storedPassword = dataSnapshot.getValue(String.class);
                if (storedPassword != null && storedPassword.equals(currentPassword)) {
                    if (!validatePassword(newPassword)) {
                        Toast.makeText(CaretakerSettingsActivity.this, "New password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    databaseRef.child("password").setValue(newPassword).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password updated successfully");
                            Toast.makeText(CaretakerSettingsActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Failed to update password", task.getException());
                            Toast.makeText(CaretakerSettingsActivity.this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Authentication failed. Please check your current password.");
                    Toast.makeText(CaretakerSettingsActivity.this, "Authentication failed. Please check your current password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read password", databaseError.toException());
                Toast.makeText(CaretakerSettingsActivity.this, "Failed to read password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validate password
    private boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Set up the delete account button
     */
    private void setupDeleteAccount() {
        buttonDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    /**
     * Show the delete account dialog
     */
    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> showDeleteAccountPasswordDialog());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Show the confirm password dialog before deleting the account
     */
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

    /**
     * Delete the caretaker's account from Firebase
     * @param currentPassword The current password
     */
    private void deleteAccount(String currentPassword) {
        if (caretakerLicense == null || caretakerLicense.isEmpty()) {
            Log.e(TAG, "No authenticated caretaker found.");
            Toast.makeText(CaretakerSettingsActivity.this, "No authenticated caretaker found. Please log in again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        databaseRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String storedPassword = dataSnapshot.getValue(String.class);
                if (storedPassword != null && storedPassword.equals(currentPassword)) {
                    databaseRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Account deleted successfully");
                            Toast.makeText(CaretakerSettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            clearLoginPreferences();
                            navigateToLogin();
                        } else {
                            Log.e(TAG, "Failed to delete account", task.getException());
                            Toast.makeText(CaretakerSettingsActivity.this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Authentication failed. Please check your current password.");
                    Toast.makeText(CaretakerSettingsActivity.this, "Authentication failed. Please check your current password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read password", databaseError.toException());
                Toast.makeText(CaretakerSettingsActivity.this, "Failed to read password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Clear the login preferences
     */
    private void clearLoginPreferences() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Navigate to the login activity
     */
    private void navigateToLogin() {
        Log.d(TAG, "Navigating to LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Log.d(TAG, "Finished navigating to LoginActivity");
    }

    /**
     * Set up the logout button
     */
    private void setupLogout() {
        buttonLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    /**
     * Show the logout confirmation dialog
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Logout the caretaker and navigate to login activity
     */
    private void logout() {
        clearLoginPreferences();
        navigateToLogin();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set text for a TextInputEditText field and update the TextInputLayout hint
     * @param field The TextInputEditText field
     * @param layout The TextInputLayout
     * @param value The value to set
     */
    private void setField(TextInputEditText field, TextInputLayout layout, String value) {
        if (value != null && !value.isEmpty()) {
            field.setText(value);
            layout.setHint(value);
        }
    }
}
