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

        if (isLoggedIn) {
            String role = prefs.getString("role", "");
            String firstName = prefs.getString("firstName", "");
            String lastName = prefs.getString("lastName", "");
            String userId = prefs.getString("userId", "");
            navigateToActivity(role, firstName, lastName, userId);
        } else {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        finish();
    }

    private void navigateToActivity(String role, String firstName, String lastName, String userId) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String phoneNumber = prefs.getString("phoneNumber", "");

        if ("caretaker".equals(role)) {
            startCaretakerActivity(firstName, lastName, userId, phoneNumber);
        } else if ("user".equals(role)) {
            startUserActivity(firstName, lastName, userId);
        } else {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        finish();
    }

    private void startCaretakerActivity(String firstName, String lastName, String userId, String phoneNumber) {
        Intent intent = new Intent(this, CaretakerMonitorActivity.class);
        intent.putExtra("caretakerName", firstName + " " + lastName);
        intent.putExtra("caretakerId", userId);
        intent.putExtra("caretakerPhoneNumber", phoneNumber);
        startActivity(intent);
        finish();
    }

    private void startUserActivity(String firstName, String lastName, String userId) {
        Intent intent = new Intent(this, MonitorActivity.class);
        intent.putExtra("userName", firstName + " " + lastName);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }
}
