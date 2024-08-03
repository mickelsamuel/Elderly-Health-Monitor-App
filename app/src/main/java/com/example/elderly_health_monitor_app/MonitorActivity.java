package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.TypedValue;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.cardview.widget.CardView;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitorActivity extends AppCompatActivity {

    private TextView temperatureReading, accelerometerXReading, accelerometerYReading, accelerometerZReading, heartRateReading, userNameText, statusSummary;
    private View temperatureStatus, accelerometerStatus, heartRateStatus;
    private Button callForHelpButton;
    private ImageButton settingsButton;
    private CardView heartRateCard, temperatureCard, accelerometerCard;

    private static final String TAG = "MonitorActivity";
    private static final AtomicInteger messageId = new AtomicInteger();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference heartRateRef, temperatureRef, userRef, accelerometerRef;

    private Handler handler;
    private Runnable heartRateRunnable, temperatureRunnable, accelerometerRunnable;
    private static final int INTERVAL = 1000; // 1 second

    private String userId; // Define userId here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Log.d(TAG, "onCreate: Initializing views");

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

        firebaseDatabase = FirebaseDatabase.getInstance();
        heartRateRef = firebaseDatabase.getReference("heartRateValues");
        temperatureRef = firebaseDatabase.getReference("temperatureValues");
        accelerometerRef = firebaseDatabase.getReference("accelerometerValues");

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId"); // Get userId from intent

        Log.d(TAG, "onCreate: Received userId: " + userId);

        userRef = firebaseDatabase.getReference("users").child(userId);
        setupListeners();

        // Register broadcast receiver for font size updates
        IntentFilter filter = new IntentFilter("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(new FontSizeUpdateReceiver(), filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(new FontSizeUpdateReceiver(), filter);
        }

        handler = new Handler();
    }

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
        startSavingData();
    }

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
                        String caretakerPhoneNumber = dataSnapshot.child("caretakerPhoneNumber").getValue(String.class); // Retrieve phone number
                        statusSummary.setText(String.format("Your caretaker is %s (%s)\n\n", caretakerName, caretakerID));

                        // Update call button to use caretaker phone number
                        callForHelpButton.setOnClickListener(v -> showOptionDialog());
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

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacks(heartRateRunnable);
            handler.removeCallbacks(temperatureRunnable);
            handler.removeCallbacks(accelerometerRunnable);
        }
    }

    private void startSavingData() {
        heartRateRunnable = new Runnable() {
            @Override
            public void run() {
                saveHeartRateToDatabase();
                handler.postDelayed(this, INTERVAL);
            }
        };

        temperatureRunnable = new Runnable() {
            @Override
            public void run() {
                saveTemperatureToDatabase();
                handler.postDelayed(this, INTERVAL);
            }
        };

        accelerometerRunnable = new Runnable() {
            @Override
            public void run() {
                saveAccelerometerToDatabase();
                handler.postDelayed(this, INTERVAL);
            }
        };

        handler.post(heartRateRunnable);
        handler.post(temperatureRunnable);
        handler.post(accelerometerRunnable);
    }

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

    private void callNumber(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 101);
        } else {
            startActivity(callIntent);
        }
    }

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

    private void saveHeartRateToDatabase() {
        long timestamp = System.currentTimeMillis();
        String heartRateValue = heartRateReading.getText().toString().replace(" bpm", "").trim();
        Map<String, Object> heartRateData = new HashMap<>();
        heartRateData.put("id", userId); // Ensure userId is added here
        heartRateData.put("heartVal", Integer.parseInt(heartRateValue));
        heartRateData.put("heartTime", timestamp);
        heartRateRef.push().setValue(heartRateData);
    }

    private void saveTemperatureToDatabase() {
        long timestamp = System.currentTimeMillis();
        String temperatureValue = temperatureReading.getText().toString().replace("°C", "").trim();
        Map<String, Object> temperatureData = new HashMap<>();
        temperatureData.put("id", userId); // Ensure userId is added here
        temperatureData.put("temperatureVal", Float.parseFloat(temperatureValue));
        temperatureData.put("temperatureTime", timestamp);
        temperatureRef.push().setValue(temperatureData);
    }

    private void saveAccelerometerToDatabase() {
        long timestamp = System.currentTimeMillis();
        String accelerometerXValue = String.format("%.2f", Float.parseFloat(accelerometerXReading.getText().toString().replace("g", "").trim()));
        String accelerometerYValue = String.format("%.2f", Float.parseFloat(accelerometerYReading.getText().toString().replace("g", "").trim()));
        String accelerometerZValue = String.format("%.2f", Float.parseFloat(accelerometerZReading.getText().toString().replace("g", "").trim()));

        Map<String, Object> accelerometerData = new HashMap<>();
        accelerometerData.put("id", userId); // Ensure userId is added here
        accelerometerData.put("accelerometerXVal", Float.parseFloat(accelerometerXValue));
        accelerometerData.put("accelerometerYVal", Float.parseFloat(accelerometerYValue));
        accelerometerData.put("accelerometerZVal", Float.parseFloat(accelerometerZValue));
        accelerometerData.put("accelerometerTime", timestamp);
        accelerometerRef.push().setValue(accelerometerData);
    }


    private class FontSizeUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float fontSize = intent.getFloatExtra("font_size", 18);
            updateFontSize(fontSize);
        }
    }
}
