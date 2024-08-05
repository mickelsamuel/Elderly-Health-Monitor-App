package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MonitorActivity extends AppCompatActivity {

    private TextView temperatureReading, accelerometerXReading, accelerometerYReading, accelerometerZReading, heartRateReading, userNameText, statusSummary;
    private View temperatureStatus, accelerometerStatus, heartRateStatus;
    private Button callForHelpButton;
    private ImageButton settingsButton;
    private CardView heartRateCard, temperatureCard, accelerometerCard;

    private static final String TAG = "MonitorActivity";

    // Firebase references
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference heartRateRef, temperatureRef, userRef, accelerometerRef;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Log.d(TAG, "onCreate: Initializing views");

        // Initialize UI elements
        userNameText = findViewById(R.id.userNameText);
        temperatureReading = findViewById(R.id.temperatureText);
        accelerometerXReading = findViewById(R.id.accelerometerXValue);
        accelerometerYReading = findViewById(R.id.accelerometerYValue);
        accelerometerZReading = findViewById(R.id.accelerometerZValue);
        heartRateReading = findViewById(R.id.heartRateText);
        statusSummary = findViewById(R.id.statusSummary);
        temperatureStatus = findViewById(R.id.temperatureStatus);
        accelerometerStatus = findViewById(R.id.accelerometerStatus);
        heartRateStatus = findViewById(R.id.heartRateStatus);
        callForHelpButton = findViewById(R.id.callForHelpButton);
        settingsButton = findViewById(R.id.settingsButton);

        heartRateCard = findViewById(R.id.heartRateCard);
        temperatureCard = findViewById(R.id.temperatureCard);
        accelerometerCard = findViewById(R.id.accelerometerCard);

        // Initialize Firebase references
        firebaseDatabase = FirebaseDatabase.getInstance();
        heartRateRef = firebaseDatabase.getReference("heartRateValues");
        temperatureRef = firebaseDatabase.getReference("temperatureValues");
        accelerometerRef = firebaseDatabase.getReference("accelerometerValues");

        // Get userId from the intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        Log.d(TAG, "onCreate: Received userId: " + userId);

        userRef = firebaseDatabase.getReference("users").child(userId);
        setupListeners();

// Register broadcast receiver for font size updates
IntentFilter filter = new IntentFilter("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE");
registerReceiver(new FontSizeUpdateReceiver(), filter);


        // Set up Firebase listeners for real-time updates
        setupFirebaseListeners();
    }

    /**
     * Set up listeners for UI elements
     */
    private void setupListeners() {
        callForHelpButton.setOnClickListener(v -> showOptionDialog());
        settingsButton.setOnClickListener(v -> {
            Log.d(TAG, "settingsButton: Passing userId to SettingsActivity: " + userId);
            Intent intent = new Intent(MonitorActivity.this, SettingsActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
        heartRateCard.setOnClickListener(v -> startActivity(new Intent(MonitorActivity.this, HeartRateActivity.class)));
        temperatureCard.setOnClickListener(v -> startActivity(new Intent(MonitorActivity.this, TemperatureActivity.class)));
        accelerometerCard.setOnClickListener(v -> startActivity(new Intent(MonitorActivity.this, AccelerometerActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserDetails();
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
    }

    /**
     * Refresh user details from the database
     */
    private void refreshUserDetails() {
        Log.d(TAG, "refreshUserDetails: Fetching user details for userId: " + userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    userNameText.setText(String.format("Hello, %s %s \n(%s)\n", firstName, lastName, userId));

                    Log.d(TAG, "refreshUserDetails: User details found - " + firstName + " " + lastName);

                    // Check for caretaker details
                    if (dataSnapshot.child("caretakerName").exists() && dataSnapshot.child("caretakerID").exists()) {
                        String caretakerName = dataSnapshot.child("caretakerName").getValue(String.class);
                        String caretakerID = dataSnapshot.child("caretakerID").getValue(String.class);
                        statusSummary.setText(String.format("Your caretaker is %s (%s)\n\n", caretakerName, caretakerID));
                    } else {
                        statusSummary.setText("You do not have a caretaker registered yet.\n\n");
                    }
                } else {
                    Log.e(TAG, "No data found for user");
                    Toast.makeText(MonitorActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to refresh user data", databaseError.toException());
                Toast.makeText(MonitorActivity.this, "Failed to refresh user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set up Firebase listeners for real-time updates
     */
    private void setupFirebaseListeners() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update user details
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    userNameText.setText(String.format("Hello, %s %s \n(%s)\n", firstName, lastName, userId));

                    Log.d(TAG, "setupFirebaseListeners: User details updated - " + firstName + " " + lastName);

                    // Check for caretaker details
                    if (dataSnapshot.child("caretakerName").exists() && dataSnapshot.child("caretakerID").exists()) {
                        String caretakerName = dataSnapshot.child("caretakerName").getValue(String.class);
                        String caretakerID = dataSnapshot.child("caretakerID").getValue(String.class);
                        statusSummary.setText(String.format("Your caretaker is %s (%s)\n\n", caretakerName, caretakerID));
                    } else {
                        statusSummary.setText("You do not have a caretaker registered yet.\n\n");
                    }
                } else {
                    Log.e(TAG, "No data found for user");
                    Toast.makeText(MonitorActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to refresh user data", databaseError.toException());
                Toast.makeText(MonitorActivity.this, "Failed to refresh user data", Toast.LENGTH_SHORT).show();
            }
        });

        heartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update heart rate data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> heartRateData = (Map<String, Object>) snapshot.getValue();
                    if (heartRateData != null && userId.equals(heartRateData.get("id"))) {
                        heartRateReading.setText(String.valueOf(heartRateData.get("heartVal")) + " bpm");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read heart rate data", databaseError.toException());
            }
        });

        temperatureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update temperature data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> temperatureData = (Map<String, Object>) snapshot.getValue();
                    if (temperatureData != null && userId.equals(temperatureData.get("id"))) {
                        temperatureReading.setText(String.valueOf(temperatureData.get("temperatureVal")) + "°C");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read temperature data", databaseError.toException());
            }
        });

        accelerometerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update accelerometer data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> accelerometerData = (Map<String, Object>) snapshot.getValue();
                    if (accelerometerData != null && userId.equals(accelerometerData.get("id"))) {
                        accelerometerXReading.setText(String.valueOf(accelerometerData.get("accelerometerXVal")) + "g");
                        accelerometerYReading.setText(String.valueOf(accelerometerData.get("accelerometerYVal")) + "g");
                        accelerometerZReading.setText(String.valueOf(accelerometerData.get("accelerometerZVal")) + "g");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read accelerometer data", databaseError.toException());
            }
        });
    }

    /**
     * Update font size of various UI elements
     * @param fontSize The new font size to set
     */
    private void updateFontSize(float fontSize) {
        // Update font sizes for text views
        userNameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        statusSummary.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        temperatureReading.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        heartRateReading.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        accelerometerXReading.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        accelerometerYReading.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        accelerometerZReading.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        // Update font size for titles if they exist
        TextView heartRateTitle = findViewById(R.id.heartRateTitle);
        TextView temperatureTitle = findViewById(R.id.temperatureTitle);
        TextView accelerometerXTitle = findViewById(R.id.accelerometerXTitle);
        TextView accelerometerYTitle = findViewById(R.id.accelerometerYTitle);
        TextView accelerometerZTitle = findViewById(R.id.accelerometerZTitle);

        if (heartRateTitle != null) heartRateTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (temperatureTitle != null) temperatureTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (accelerometerXTitle != null) accelerometerXTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (accelerometerYTitle != null) accelerometerYTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (accelerometerZTitle != null) accelerometerZTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        // Update font size for the button
        Button callForHelpButton = findViewById(R.id.callForHelpButton);
        callForHelpButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    }

    /**
     * Show dialog to choose between calling caretaker, emergency contact, or emergency services
     */
    private void showOptionDialog() {
        String[] options = {"Call Caretaker", "Call Emergency Contact", "Call Emergency Services"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Option").setItems(options, (dialog, which) -> {
            if (which == 0) {
                confirmCall("Caretaker");
            } else if (which == 1) {
                confirmCall("Emergency Contact");
            } else if (which == 2) {
                confirmCall("Emergency Services");
            }
        }).show();
    }

    /**
     * Confirm before making a call to the selected contact
     * @param type The type of contact to call (Caretaker, Emergency Contact, or Emergency Services)
     */
    private void confirmCall(String type) {
        if (type.equals("Emergency Services")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MonitorActivity.this);
            builder.setMessage("Are you sure you want to call Emergency Services?")
                    .setPositiveButton("Yes", (dialog, id) -> callNumber("911"))
                    .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                    .show();
            return;
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String phoneNumber;
                if (type.equals("Caretaker")) {
                    if (dataSnapshot.child("caretakerPhoneNumber").exists()) {
                        phoneNumber = dataSnapshot.child("caretakerPhoneNumber").getValue(String.class);
                    } else {
                        Toast.makeText(MonitorActivity.this, "No caretaker number registered!", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else if (type.equals("Emergency Contact")) {
                    if (dataSnapshot.child("emergencyContact").exists()) {
                        phoneNumber = dataSnapshot.child("emergencyContact").getValue(String.class);
                    } else {
                        Toast.makeText(MonitorActivity.this, "No emergency contact number registered!", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    phoneNumber = null; // This case should not occur
                }

                if (phoneNumber != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MonitorActivity.this);
                    builder.setMessage("Are you sure you want to call your " + type + "?")
                            .setPositiveButton("Yes", (dialog, id) -> callNumber(phoneNumber))
                            .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch phone number", databaseError.toException());
                Toast.makeText(MonitorActivity.this, "Failed to fetch phone number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Make a call to the given phone number
     * @param phoneNumber The phone number to call
     */
    private void callNumber(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 101);
        } else {
            startActivity(callIntent);
        }
    }

    /**
     * Broadcast receiver for updating font size
     */
//    private class FontSizeUpdateReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            float fontSize = intent.getFloatExtra("font_size", 18);
//            updateFontSize(fontSize);
//        }
//    }
}
