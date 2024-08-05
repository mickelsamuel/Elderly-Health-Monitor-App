package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        // If user is logged in, navigate to the appropriate activity based on the user's role
        if (isLoggedIn) {
            String role = prefs.getString("role", "");
            String firstName = prefs.getString("firstName", "");
            String lastName = prefs.getString("lastName", "");
            String userId = prefs.getString("userId", "");
            navigateToActivity(role, firstName, lastName, userId);
        } else {
            // If user is not logged in, start the LoginActivity
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        // Finish MainActivity
        finish();
    }

    /**
     * Navigate to the appropriate activity based on the user's role
     * @param role The role of the user (caretaker or user)
     * @param firstName The first name of the user
     * @param lastName The last name of the user
     * @param userId The user ID
     */
    private void navigateToActivity(String role, String firstName, String lastName, String userId) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String phoneNumber = prefs.getString("phoneNumber", "");

        if ("caretaker".equals(role)) {
            startCaretakerActivity(firstName, lastName, userId, phoneNumber);
        } else if ("user".equals(role)) {
            startUserActivity(firstName, lastName, userId);
        } else {
            // If role is invalid, start the LoginActivity
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        // Finish MainActivity
        finish();
    }

    /**
     * Start the CaretakerMonitorActivity
     * @param firstName The first name of the caretaker
     * @param lastName The last name of the caretaker
     * @param userId The user ID of the caretaker
     * @param phoneNumber The phone number of the caretaker
     */
    private void startCaretakerActivity(String firstName, String lastName, String userId, String phoneNumber) {
        Intent intent = new Intent(this, CaretakerMonitorActivity.class);
        intent.putExtra("caretakerName", firstName + " " + lastName);
        intent.putExtra("caretakerId", userId);
        intent.putExtra("caretakerPhoneNumber", phoneNumber);
        startActivity(intent);
        finish();
    }

    /**
     * Start the MonitorActivity for a user
     * @param firstName The first name of the user
     * @param lastName The last name of the user
     * @param userId The user ID of the user
     */
    private void startUserActivity(String firstName, String lastName, String userId) {
        Intent intent = new Intent(this, MonitorActivity.class);
        intent.putExtra("userName", firstName + " " + lastName);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }
}
