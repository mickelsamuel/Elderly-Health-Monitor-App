package com.example.elderly_health_monitor_app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // UI components
    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextLastName;
    private TextInputEditText editTextPassword;

    // Firebase database references
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;

    private static final String CHANNEL_ID = "push_notifications";
    private ActivityResultLauncher<String> ARL;
    public static PushNotifications pn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Initialize UI components
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPassword = findViewById(R.id.editTextPassword);

        // Initialize Firebase database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");

        createNotificationChannel();

        ARL = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                pn.sendNotification(LoginActivity.this, "NOTIFICATIONS ARE ACTIVE", "You will receive notifications");
            } else {
                Toast.makeText(LoginActivity.this, "Post notification permission not granted", Toast.LENGTH_SHORT).show();
            }
        });

        pn = new PushNotifications(ARL); // Initialize after ARL is set up

        // Request notification permission if not already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ARL.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            pn.sendNotification(LoginActivity.this, "NOTIFICATIONS ARE ACTIVE", "You will receive notifications");
        }
        findViewById(R.id.buttonCreateAccount).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, AccountCreationActivity.class));
        });

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            String role = prefs.getString("role", "");
            String firstName = prefs.getString("firstName", "");
            String lastName = prefs.getString("lastName", "");
            String userId = prefs.getString("userId", "");
            navigateToActivity(role, firstName, lastName, userId);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Global Notifications";
            String description = "Channel for global notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Set to high for visibility
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Handle the login button click
     */
    public void onLoginClick(View view) {
        final String firstName = editTextFirstName.getText().toString().trim();
        final String lastName = editTextLastName.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter first name, last name, and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query the Firebase database to validate user credentials
        usersRef.orderByChild("firstName").equalTo(firstName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean loginSuccess = false;
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String dbLastName = userSnapshot.child("lastName").getValue(String.class);
                            String dbPassword = userSnapshot.child("password").getValue(String.class);
                            if (dbLastName != null && dbLastName.equals(lastName) && dbPassword != null && dbPassword.equals(password)) {
                                handleSuccessfulLogin(userSnapshot);
                                loginSuccess = true;
                                break;
                            }
                        }

                        if (!loginSuccess) {
                            Toast.makeText(LoginActivity.this, "Invalid name or password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Database error", databaseError.toException());
                    }
                });
    }

    /**
     * Handle a successful login
     */
    private void handleSuccessfulLogin(DataSnapshot userSnapshot) {
        String role = userSnapshot.child("role").getValue(String.class);
        String firstName = userSnapshot.child("firstName").getValue(String.class);
        String lastName = userSnapshot.child("lastName").getValue(String.class);
        String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
        String userId = userSnapshot.getKey();

        Log.d(TAG, "handleSuccessfulLogin: role=" + role + ", firstName=" + firstName + ", lastName=" + lastName + ", phoneNumber=" + phoneNumber + ", userId=" + userId);

        // Save login details in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", role);
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("userId", userId);
        editor.putString("phoneNumber", phoneNumber);
        editor.apply();

        // Navigate to the appropriate activity based on the user's role
        if ("caretaker".equals(role)) {
            loadCaretakerPatientsAndStartActivity(firstName, lastName, userId, phoneNumber);
        } else if ("user".equals(role)) {
            startUserActivity(firstName, lastName, userId);
        } else {
            Toast.makeText(this, "Access denied: Invalid role", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load caretaker's patients and start the CaretakerMonitorActivity
     */
    private void loadCaretakerPatientsAndStartActivity(String firstName, String lastName, String userId, String phoneNumber) {
        DatabaseReference caretakerRef = usersRef.child(userId).child("patientIDs");
        caretakerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> patientIDs = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        patientIDs.add(snapshot.getValue(String.class));
                    }
                }
                Log.d(TAG, "Patient IDs found: " + patientIDs);
                startCaretakerActivity(firstName, lastName, userId, phoneNumber, patientIDs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load caretaker's patient list: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Start the CaretakerMonitorActivity
     */
    private void startCaretakerActivity(String firstName, String lastName, String userId, String phoneNumber, ArrayList<String> patientIDs) {
        Log.d(TAG, "startCaretakerActivity: firstName=" + firstName + ", lastName=" + lastName + ", userId=" + userId + ", phoneNumber=" + phoneNumber);
        Intent intent = new Intent(this, CaretakerMonitorActivity.class);
        intent.putExtra("caretakerName", firstName + " " + lastName);
        intent.putExtra("caretakerId", userId);
        intent.putExtra("caretakerPhoneNumber", phoneNumber);
        intent.putStringArrayListExtra("patientIDs", patientIDs);
        startActivity(intent);
        finish();
    }

    /**
     * Clear login preferences
     */
    private void clearLoginPreferences() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Navigate to the LoginActivity
     */
    private void navigateToLogin() {
        clearLoginPreferences();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Navigate to the appropriate activity based on the user's role
     */
    private void navigateToActivity(String role, String firstName, String lastName, String userId) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String phoneNumber = prefs.getString("phoneNumber", "");

        if ("caretaker".equals(role)) {
            loadCaretakerPatientsAndStartActivity(firstName, lastName, userId, phoneNumber);
        } else if ("user".equals(role)) {
            startUserActivity(firstName, lastName, userId);
        } else {
            Toast.makeText(this, "Access denied: Invalid role", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start the MonitorActivity for a user
     */
    private void startUserActivity(String firstName, String lastName, String userId) {
        Intent intent = new Intent(this, MonitorActivity.class);
        intent.putExtra("userName", firstName + " " + lastName);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }
}
