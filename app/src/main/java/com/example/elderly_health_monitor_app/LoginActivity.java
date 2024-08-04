package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextLastName;
    private TextInputEditText editTextPassword;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPassword = findViewById(R.id.editTextPassword);

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");

        findViewById(R.id.buttonCreateAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, AccountCreationActivity.class));
            }
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

    public void onLoginClick(View view) {
        final String firstName = editTextFirstName.getText().toString().trim();
        final String lastName = editTextLastName.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter first name, last name, and password", Toast.LENGTH_SHORT).show();
            return;
        }

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

    private void handleSuccessfulLogin(DataSnapshot userSnapshot) {
        String role = userSnapshot.child("role").getValue(String.class);
        String firstName = userSnapshot.child("firstName").getValue(String.class);
        String lastName = userSnapshot.child("lastName").getValue(String.class);
        String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
        String userId = userSnapshot.getKey();

        Log.d(TAG, "handleSuccessfulLogin: role=" + role + ", firstName=" + firstName + ", lastName=" + lastName + ", phoneNumber=" + phoneNumber + ", userId=" + userId);

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", role);
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("userId", userId);
        editor.putString("phoneNumber", phoneNumber);
        editor.apply();

        if ("caretaker".equals(role)) {
            startCaretakerActivity(firstName, lastName, userId, phoneNumber);
        } else if ("user".equals(role)) {
            startUserActivity(firstName, lastName, userId);
        } else {
            Toast.makeText(this, "Access denied: Invalid role", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCaretakerActivity(String firstName, String lastName, String userId, String phoneNumber) {
        Log.d(TAG, "startCaretakerActivity: firstName=" + firstName + ", lastName=" + lastName + ", userId=" + userId + ", phoneNumber=" + phoneNumber);
        Intent intent = new Intent(this, CaretakerMonitorActivity.class);
        intent.putExtra("caretakerName", firstName + " " + lastName);
        intent.putExtra("caretakerId", userId);
        intent.putExtra("caretakerPhoneNumber", phoneNumber);
        startActivity(intent);
        finish();
    }

    private void clearLoginPreferences() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    private void navigateToLogin() {
        clearLoginPreferences();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToActivity(String role, String firstName, String lastName, String userId) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String phoneNumber = prefs.getString("phoneNumber", "");

        if ("caretaker".equals(role)) {
            startCaretakerActivity(firstName, lastName, userId, phoneNumber);
        } else if ("user".equals(role)) {
            startUserActivity(firstName, lastName, userId);
        } else {
            Toast.makeText(this, "Access denied: Invalid role", Toast.LENGTH_SHORT).show();
        }
    }

    private void startUserActivity(String firstName, String lastName, String userId) {
        Intent intent = new Intent(this, MonitorActivity.class);
        intent.putExtra("userName", firstName + " " + lastName);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }
}
