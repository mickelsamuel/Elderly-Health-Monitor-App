package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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
    private Button callForHelpButton, changeStatusButton;
    private ImageButton settingsButton;
    private CardView heartRateCard, temperatureCard, accelerometerCard;

    private static final String TAG = "MonitorActivity";

    // Firebase references
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference heartRateRef, temperatureRef, userRef, accelerometerRef;

    private String userId;
    private Patient patient;

    // Constants for SMS permission request code
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    // Thresholds for the indicators
    private static final float TEMPERATURE_YELLOW_LOW_THRESHOLD = 36.1f;
    private static final float TEMPERATURE_RED_LOW_THRESHOLD = 35.0f;
    private static final float TEMPERATURE_YELLOW_HIGH_THRESHOLD = 37.5f;
    private static final float TEMPERATURE_RED_HIGH_THRESHOLD = 38.3f;

    private static final int HEART_RATE_YELLOW_HIGH_THRESHOLD = 130;
    private static final int HEART_RATE_RED_HIGH_THRESHOLD = 150;
    private static final int HEART_RATE_YELLOW_LOW_THRESHOLD = 55;
    private static final int HEART_RATE_RED_LOW_THRESHOLD = 50;

    private static final double ACCELEROMETER_YELLOW_THRESHOLD = 2.0;
    private static final double ACCELEROMETER_RED_THRESHOLD = 3.0;

    // Notification channel constants
    private static final String CHANNEL_ID = "health_monitor_notifications";
    private static final int NOTIFICATION_ID_YELLOW = 1;
    private static final int NOTIFICATION_ID_RED = 2;

    // SharedPreferences constants for alert suppression
    private static final String PREFS_NAME = "ElderlyHealthMonitorPrefs";
    private static final String PREFS_KEY_ALERT_SUPPRESSION_END_TIME = "alertSuppressionEndTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

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
        changeStatusButton = findViewById(R.id.changeStatusButton);

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

        userRef = firebaseDatabase.getReference("users").child(userId);
        setupListeners();

        // Register broadcast receiver for font size updates
        IntentFilter filter = new IntentFilter("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE");
        registerReceiver(new FontSizeUpdateReceiver(), filter);

        // Set up Firebase listeners for real-time updates
        setupFirebaseListeners();

        // Initialize patient object
        patient = new Patient();

        // Request SMS permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }

        // Create notification channel
        createNotificationChannel();

        // Check if alerts are currently suppressed
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long alertSuppressionEndTime = preferences.getLong(PREFS_KEY_ALERT_SUPPRESSION_END_TIME, 0);
        if (System.currentTimeMillis() < alertSuppressionEndTime) {
            // Alerts are suppressed, set up suppression end time check
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                // Re-enable alerts after suppression period ends
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().remove(PREFS_KEY_ALERT_SUPPRESSION_END_TIME).apply();
            }, alertSuppressionEndTime - System.currentTimeMillis());
        }
    }

    /**
     * Set up listeners for UI elements
     */
    private void setupListeners() {
        callForHelpButton.setOnClickListener(v -> showOptionDialog());
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MonitorActivity.this, SettingsActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
        heartRateCard.setOnClickListener(v -> startActivity(new Intent(MonitorActivity.this, HeartRateActivity.class)));
        temperatureCard.setOnClickListener(v -> startActivity(new Intent(MonitorActivity.this, TemperatureActivity.class)));
        accelerometerCard.setOnClickListener(v -> startActivity(new Intent(MonitorActivity.this, AccelerometerActivity.class)));
        changeStatusButton.setOnClickListener(v -> showChangeStatusDialog());
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
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    patient.setFirstName(firstName);
                    patient.setLastName(lastName);
                    userNameText.setText(String.format("Hello, %s %s \n(%s)\n", firstName, lastName, userId));

                    // Check for caretaker details
                    if (dataSnapshot.child("caretakerName").exists() && dataSnapshot.child("caretakerID").exists()) {
                        String caretakerName = dataSnapshot.child("caretakerName").getValue(String.class);
                        String caretakerID = dataSnapshot.child("caretakerID").getValue(String.class);
                        statusSummary.setText(String.format("Your caretaker is %s (%s)\n\n", caretakerName, caretakerID));
                    } else {
                        statusSummary.setText("You do not have a caretaker registered yet.\n");
                    }
                } else {
                    Toast.makeText(MonitorActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    patient.setFirstName(firstName);
                    patient.setLastName(lastName);
                    userNameText.setText(String.format("Hello, %s %s \n(%s)\n", firstName, lastName, userId));

                    // Check for caretaker details
                    if (dataSnapshot.child("caretakerName").exists() && dataSnapshot.child("caretakerID").exists()) {
                        String caretakerName = dataSnapshot.child("caretakerName").getValue(String.class);
                        String caretakerID = dataSnapshot.child("caretakerID").getValue(String.class);
                        statusSummary.setText(String.format("Your caretaker is %s (%s)\n\n", caretakerName, caretakerID));
                    } else {
                        statusSummary.setText("You do not have a caretaker registered yet.\n\n");
                    }
                } else {
                    Toast.makeText(MonitorActivity.this, "No user data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MonitorActivity.this, "Failed to refresh user data", Toast.LENGTH_SHORT).show();
            }
        });

        heartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> heartRateData = (Map<String, Object>) snapshot.getValue();
                    if (heartRateData != null && userId.equals(heartRateData.get("id"))) {
                        patient.setHeartRate(((Long) heartRateData.get("heartVal")).intValue());
                        heartRateReading.setText(String.valueOf(patient.getHeartRate()) + " bpm");
                        updateIndicators();
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
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> temperatureData = (Map<String, Object>) snapshot.getValue();
                    if (temperatureData != null && userId.equals(temperatureData.get("id"))) {
                        patient.setTemperature(((Double) temperatureData.get("temperatureVal")).floatValue());
                        temperatureReading.setText(String.valueOf(patient.getTemperature()) + "°C");
                        updateIndicators();
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
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> accelerometerData = (Map<String, Object>) snapshot.getValue();
                    if (accelerometerData != null && userId.equals(accelerometerData.get("id"))) {
                        patient.setAccelerometerX((Double) accelerometerData.get("accelerometerXVal"));
                        patient.setAccelerometerY((Double) accelerometerData.get("accelerometerYVal"));
                        patient.setAccelerometerZ((Double) accelerometerData.get("accelerometerZVal"));
                        accelerometerXReading.setText(String.format("%.2fg", patient.getAccelerometerX()));
                        accelerometerYReading.setText(String.format("%.2fg", patient.getAccelerometerY()));
                        accelerometerZReading.setText(String.format("%.2fg", patient.getAccelerometerZ()));
                        updateIndicators();
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
        TextView accelerometerTitle = findViewById(R.id.accelerometerTitle);
        TextView accelerometerXTitle = findViewById(R.id.accelerometerXTitle);
        TextView accelerometerYTitle = findViewById(R.id.accelerometerYTitle);
        TextView accelerometerZTitle = findViewById(R.id.accelerometerZTitle);

        if (heartRateTitle != null) heartRateTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (temperatureTitle != null) temperatureTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (accelerometerTitle != null) accelerometerTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (accelerometerXTitle != null) accelerometerXTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (accelerometerYTitle != null) accelerometerYTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        if (accelerometerZTitle != null) accelerometerZTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        // Update font size for the button
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
     * Send an SMS to the given phone number
     * @param phoneNumber The phone number to send the SMS to
     * @param message The message to send
     */
    private void sendSms(String phoneNumber, String message) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update indicators based on sensor data and send notifications
     */
    private void updateIndicators() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long alertSuppressionEndTime = preferences.getLong(PREFS_KEY_ALERT_SUPPRESSION_END_TIME, 0);
        if (System.currentTimeMillis() < alertSuppressionEndTime) {
            // Alerts are currently suppressed, do not update indicators or send notifications
            return;
        }

        // Temperature indicator
        if (patient.getTemperature() >= TEMPERATURE_RED_HIGH_THRESHOLD || patient.getTemperature() < TEMPERATURE_RED_LOW_THRESHOLD) {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_red);
            showAlertConfirmation("Temperature Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has an abnormal temperature.");
            sendNotification("Critical Temperature Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has a critical temperature. Please open the app. If no action is taken in 30 seconds, it will alert the caretaker or emergency contact.");
        } else if (patient.getTemperature() >= TEMPERATURE_YELLOW_HIGH_THRESHOLD || patient.getTemperature() < TEMPERATURE_YELLOW_LOW_THRESHOLD) {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_yellow);
            sendNotification("Temperature Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has an abnormal temperature.");
        } else {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_green);
        }

        // Heart rate indicator
        if (patient.getHeartRate() >= HEART_RATE_RED_HIGH_THRESHOLD || patient.getHeartRate() < HEART_RATE_RED_LOW_THRESHOLD) {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_red);
            showAlertConfirmation("Heart Rate Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has an abnormal heart rate.");
            sendNotification("Critical Heart Rate Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has a critical heart rate. Please open the app. If no action is taken in 30 seconds, it will alert the caretaker or emergency contact.");
        } else if (patient.getHeartRate() >= HEART_RATE_YELLOW_HIGH_THRESHOLD || patient.getHeartRate() < HEART_RATE_YELLOW_LOW_THRESHOLD) {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_yellow);
            sendNotification("Heart Rate Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has an abnormal heart rate.");
        } else {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_green);
        }

        // Accelerometer indicator
        double maxAcceleration = Math.max(Math.max(patient.getAccelerometerX(), patient.getAccelerometerY()), patient.getAccelerometerZ());
        if (maxAcceleration >= ACCELEROMETER_RED_THRESHOLD) {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_red);
            showAlertConfirmation("Accelerometer Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has a significant movement detected.");
            sendNotification("Critical Accelerometer Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has significant movement detected. Please open the app. If no action is taken in 30 seconds, it will alert the caretaker or emergency contact.");
        } else if (maxAcceleration >= ACCELEROMETER_YELLOW_THRESHOLD) {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_yellow);
            sendNotification("Accelerometer Alert", "Patient " + patient.getFirstName() + " " + patient.getLastName() + " has significant movement detected.");
        } else {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_green);
        }

        // Update the UI readings
        heartRateReading.setText(String.valueOf(patient.getHeartRate()) + " bpm");
        temperatureReading.setText(String.valueOf(patient.getTemperature()) + "°C");
        accelerometerXReading.setText(String.format("%.2fg", patient.getAccelerometerX()));
        accelerometerYReading.setText(String.format("%.2fg", patient.getAccelerometerY()));
        accelerometerZReading.setText(String.format("%.2fg", patient.getAccelerometerZ()));
    }

    private void showAlertConfirmation(String title, String message) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasCaretaker = dataSnapshot.child("caretakerName").exists() && dataSnapshot.child("caretakerID").exists();
                String emergencyContact = dataSnapshot.child("emergencyContact").getValue(String.class);
                String caretakerID = dataSnapshot.child("caretakerID").getValue(String.class);
                String caretakerName = dataSnapshot.child("caretakerName").getValue(String.class);
                String caretakerPhoneNumber = dataSnapshot.child("caretakerPhoneNumber").getValue(String.class);

                AlertDialog.Builder builder = new AlertDialog.Builder(MonitorActivity.this);
                builder.setTitle(title)
                        .setMessage(message + "\n\nDo you want to alert your " + (hasCaretaker ? "caretaker" : "emergency contact") + "?\n\nThis alert will be sent in 30 seconds.")
                        .setCancelable(false)
                        .setPositiveButton("Yes", null)
                        .setNegativeButton("No", null)
                        .setNeutralButton("Disable alerts for", null);

                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // Countdown timer
                new CountDownTimer(30000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        alertDialog.setMessage(message + "\n\nDo you want to alert your " + (hasCaretaker ? "caretaker" : "emergency contact") + "?\n\nThis alert will be sent in " + millisUntilFinished / 1000 + " seconds.");
                    }

                    public void onFinish() {
                        if (alertDialog.isShowing()) {
                            if (hasCaretaker) {
                                sendAlertToCaretaker(title, message, caretakerID, caretakerName, caretakerPhoneNumber);
                            } else if (emergencyContact != null) {
                                sendSms(emergencyContact, "Alert: " + message);
                            } else {
                                Toast.makeText(MonitorActivity.this, "No emergency contact registered!", Toast.LENGTH_LONG).show();
                            }
                            alertDialog.dismiss();
                        }
                    }
                }.start();

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (hasCaretaker) {
                        sendAlertToCaretaker(title, message, caretakerID, caretakerName, caretakerPhoneNumber);
                    } else if (emergencyContact != null) {
                        sendSms(emergencyContact, "Alert: " + message);
                    } else {
                        Toast.makeText(MonitorActivity.this, "No emergency contact registered!", Toast.LENGTH_LONG).show();
                    }
                    alertDialog.dismiss();
                });

                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> alertDialog.dismiss());

                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    AlertDialog.Builder disableAlertBuilder = new AlertDialog.Builder(MonitorActivity.this);
                    disableAlertBuilder.setTitle("Disable alerts for")
                            .setItems(new String[]{"30 minutes", "1 hour", "2 hours"}, (dialog, which) -> {
                                long suppressionTime = 0;
                                if (which == 0) {
                                    suppressionTime = 30 * 60 * 1000; // 30 minutes
                                } else if (which == 1) {
                                    suppressionTime = 60 * 60 * 1000; // 1 hour
                                } else if (which == 2) {
                                    suppressionTime = 2 * 60 * 60 * 1000; // 2 hours
                                }

                                long suppressionEndTime = System.currentTimeMillis() + suppressionTime;
                                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putLong(PREFS_KEY_ALERT_SUPPRESSION_END_TIME, suppressionEndTime);
                                editor.apply();

                                // Dismiss both dialogs
                                alertDialog.dismiss();
                                dialog.dismiss();
                            }).show();
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read user data", databaseError.toException());
            }
        });
    }

    private void sendAlertToCaretaker(String title, String message, String caretakerID, String caretakerName, String caretakerPhoneNumber) {
        Log.d(TAG, "sendAlertToCaretaker: title=" + title + ", message=" + message + ", caretakerID=" + caretakerID + ", caretakerName=" + caretakerName);

        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(caretakerID);
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("patientID", userId);
        notificationData.put("patientName", patient.getFirstName() + " " + patient.getLastName());

        notificationsRef.push().setValue(notificationData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "sendAlertToCaretaker: Caretaker alerted successfully");
                Toast.makeText(MonitorActivity.this, "Caretaker alerted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "sendAlertToCaretaker: Failed to alert caretaker", task.getException());
                Toast.makeText(MonitorActivity.this, "Failed to alert caretaker", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "sendAlertToCaretaker: Error alerting caretaker", e);
        });
    }

    private void showChangeStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MonitorActivity.this);
        builder.setTitle("Change Sensor Status")
                .setItems(new String[]{"Heart Rate - Green", "Heart Rate - Yellow", "Heart Rate - Red",
                        "Temperature - Green", "Temperature - Yellow", "Temperature - Red",
                        "Accelerometer - Green", "Accelerometer - Yellow", "Accelerometer - Red"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            patient.setHeartRate(80); // Green heart rate
                            break;
                        case 1:
                            patient.setHeartRate(140); // Yellow heart rate
                            break;
                        case 2:
                            patient.setHeartRate(160); // Red heart rate
                            break;
                        case 3:
                            patient.setTemperature(36.5f); // Green temperature
                            break;
                        case 4:
                            patient.setTemperature(37.6f); // Yellow temperature
                            break;
                        case 5:
                            patient.setTemperature(38.4f); // Red temperature
                            break;
                        case 6:
                            patient.setAccelerometerX(0.5); // Green accelerometer
                            patient.setAccelerometerY(0.5);
                            patient.setAccelerometerZ(0.5);
                            break;
                        case 7:
                            patient.setAccelerometerX(2.5); // Yellow accelerometer
                            patient.setAccelerometerY(2.5);
                            patient.setAccelerometerZ(2.5);
                            break;
                        case 8:
                            patient.setAccelerometerX(3.5); // Red accelerometer
                            patient.setAccelerometerY(3.5);
                            patient.setAccelerometerZ(3.5);
                            break;
                    }
                    updateIndicators(); // Update indicators and trigger alerts if needed
                }).show();
    }

    private void notifyCaretaker(String alertType) {
        // Logic to notify the caretaker
        Toast.makeText(MonitorActivity.this, "Caretaker alerted for " + alertType, Toast.LENGTH_LONG).show();
        // Implement actual notification logic here
    }

    /**
     * Broadcast receiver for updating font size
     */
    private class FontSizeUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float fontSize = intent.getFloatExtra("font_size", 18);
            updateFontSize(fontSize);
        }
    }

    // Handling the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Create a notification channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Health Monitor Alerts";
            String description = "Notifications for health monitor alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Send a notification
     */
    private void sendNotification(String title, String message) {
        Log.d(TAG, "sendNotification: title=" + title + ", message=" + message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_crisis_alert_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(title.contains("Critical") ? NOTIFICATION_ID_RED : NOTIFICATION_ID_YELLOW, builder.build());
    }
}
//EOF
