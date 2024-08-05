package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve shared preferences to check if the user is logged in
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // If the user is logged in, get their role, first name, last name, and user ID
            String role = prefs.getString("role", "");
            String firstName = prefs.getString("firstName", "");
            String lastName = prefs.getString("lastName", "");
            String userId = prefs.getString("userId", "");
            // Navigate to the appropriate activity based on the user's role
            navigateToActivity(role, firstName, lastName, userId);
        } else {
            // If the user is not logged in, navigate to the login activity
            startActivity(new Intent(this, LoginActivity.class));
        }

        // Finish the splash activity so it can't be returned to
        finish();
    }

    // Method to navigate to the appropriate activity based on the user's role
    private void navigateToActivity(String role, String firstName, String lastName, String userId) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String phoneNumber = prefs.getString("phoneNumber", "");

        if ("caretaker".equals(role)) {
            // Navigate to the caretaker activity if the role is caretaker
            startCaretakerActivity(firstName, lastName, userId, phoneNumber);
        } else if ("user".equals(role)) {
            // Navigate to the user activity if the role is user
            startUserActivity(firstName, lastName, userId);
        } else {
            // Default to login activity if the role is not recognized
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    // Method to start the caretaker activity and pass necessary data
    private void startCaretakerActivity(String firstName, String lastName, String userId, String phoneNumber) {
        Intent intent = new Intent(this, CaretakerMonitorActivity.class);
        intent.putExtra("caretakerName", firstName + " " + lastName);
        intent.putExtra("caretakerId", userId);
        intent.putExtra("caretakerPhoneNumber", phoneNumber);
        startActivity(intent);
    }

    // Method to start the user activity and pass necessary data
    private void startUserActivity(String firstName, String lastName, String userId) {
        Intent intent = new Intent(this, MonitorActivity.class);
        intent.putExtra("userName", firstName + " " + lastName);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
}
