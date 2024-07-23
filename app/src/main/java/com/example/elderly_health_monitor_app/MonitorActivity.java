package com.example.elderly_health_monitor_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.content.SharedPreferences;
import android.util.TypedValue;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitorActivity extends AppCompatActivity {

    private TextView temperatureReading;
    private TextView accelerometerReading;
    private TextView heartRateReading;
    private TextView userNameText;
    private TextView statusSummary;
    private View temperatureStatus;
    private View accelerometerStatus;
    private View heartRateStatus;
    private Button callForHelpButton;
    private Button triggerCriticalHeartRateButton;
    private Button triggerExtremeHeartRateButton;
    private Button triggerCriticalTemperatureButton;
    private Button triggerExtremeTemperatureButton;
    private Button triggerCriticalAccelerometerButton;
    private Button triggerExtremeAccelerometerButton;
    private ImageButton settingsButton;
    private CardView heartRateCard;
    private CardView temperatureCard;
    private CardView accelerometerCard;

    private static final String TAG = "MonitorActivity";
    private static final AtomicInteger messageId = new AtomicInteger();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference heartRateRef;
    private DatabaseReference temperatureRef;
    private DatabaseReference userRef;

    private Handler handler;
    private Runnable heartRateRunnable;
    private Runnable temperatureRunnable;
    private static final int INTERVAL = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Log.d(TAG, "onCreate: Initializing views");

        // Initialize TextViews and Status Views
        userNameText = findViewById(R.id.userNameText);
        temperatureReading = findViewById(R.id.temperatureText);
        accelerometerReading = findViewById(R.id.accelerometerText);
        heartRateReading = findViewById(R.id.heartRateText);
        statusSummary = findViewById(R.id.statusSummary);
        temperatureStatus = findViewById(R.id.temperatureStatus);
        accelerometerStatus = findViewById(R.id.accelerometerStatus);
        heartRateStatus = findViewById(R.id.heartRateStatus);
        callForHelpButton = findViewById(R.id.callForHelpButton);
        settingsButton = findViewById(R.id.settingsButton);
        triggerCriticalHeartRateButton = findViewById(R.id.triggerCriticalHeartRateButton);
        triggerExtremeHeartRateButton = findViewById(R.id.triggerExtremeHeartRateButton);
        triggerCriticalTemperatureButton = findViewById(R.id.triggerCriticalTemperatureButton);
        triggerExtremeTemperatureButton = findViewById(R.id.triggerExtremeTemperatureButton);
        triggerCriticalAccelerometerButton = findViewById(R.id.triggerCriticalAccelerometerButton);
        triggerExtremeAccelerometerButton = findViewById(R.id.triggerExtremeAccelerometerButton);

        heartRateCard = findViewById(R.id.heartRateCard);
        temperatureCard = findViewById(R.id.temperatureCard);
        accelerometerCard = findViewById(R.id.accelerometerCard);

        // Initialize Firebase Database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        heartRateRef = firebaseDatabase.getReference("heartRateValues");
        temperatureRef = firebaseDatabase.getReference("temperatureValues");
        userRef = firebaseDatabase.getReference("users/0"); // Adjust this path as needed

        // Fetch and display user details
        fetchUserDetails();

        // Example patient details
        String patientName = "John Doe"; // Replace with actual patient name
        String patientId = "P12345"; // Replace with actual patient ID
        userNameText.setText("Hello, " + patientName + " (" + patientId + ")\n");

        // Example caretaker details
        String caretakerName = "Jane Smith"; // Replace with actual caretaker name
        String caretakerId = "C67890"; // Replace with actual caretaker ID
        statusSummary.setText("Your caretaker is " + caretakerName + " (" + caretakerId + ")\n");

        // TODO: Implement real sensor data reading
        updateReadings(70, 36.5f, "X: 0.1, Y: 0.2, Z: 9.8");

        // Set OnClickListener for the Call for Help button
        callForHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });

        // Set OnClickListener for the Settings button
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonitorActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListeners for the new buttons
        triggerCriticalHeartRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReadings(40, 36.5f, "X: 0.1, Y: 0.2, Z: 9.8"); // Critical heart rate
            }
        });

        triggerExtremeHeartRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReadings(55, 36.5f, "X: 0.1, Y: 0.2, Z: 9.8"); // Extreme heart rate
            }
        });

        triggerCriticalTemperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReadings(70, 34.0f, "X: 0.1, Y: 0.2, Z: 9.8"); // Critical temperature
            }
        });

        triggerExtremeTemperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReadings(70, 35.5f, "X: 0.1, Y: 0.2, Z: 9.8"); // Extreme temperature
            }
        });

        triggerCriticalAccelerometerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReadings(70, 36.5f, "X: 0.0, Y: 0.0, Z: 0.0"); // Critical accelerometer
            }
        });

        triggerExtremeAccelerometerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReadings(70, 36.5f, "X: 0.5, Y: 0.5, Z: 0.5"); // Extreme accelerometer
            }
        });

        // Set OnClickListener for each CardView to navigate to detail screens
        heartRateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonitorActivity.this, HeartRateActivity.class);
                startActivity(intent);
            }
        });
        temperatureCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonitorActivity.this, TemperatureActivity.class);
                startActivity(intent);
            }
        });

        // Initialize handler and runnables for periodic database updates
        handler = new Handler();

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

        handler.post(heartRateRunnable);
        handler.post(temperatureRunnable);

        Log.d(TAG, "onCreate: Views initialized");
    }

    private void fetchUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    String patientId = dataSnapshot.child("id").getValue(String.class);

                    userNameText.setText("Hello, " + firstName + " " + lastName + " (" + patientId + ")\n");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch user details", databaseError.toException());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(heartRateRunnable); // Stop updates when activity is paused
        handler.removeCallbacks(temperatureRunnable); // Stop updates when activity is paused
    }

    private void updateReadings(int heartRate, float temperature, String accelerometer) {
        // Update heart rate reading
        heartRateReading.setText(heartRate + " bpm");
        if (heartRate >= 60 && heartRate <= 100) {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_green);
        } else if (heartRate >= 50 && heartRate <= 110) {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_yellow);
        } else {
            heartRateStatus.setBackgroundResource(R.drawable.indicator_red);
            showCriticalAlertDialog("Heart Rate");
        }

        // Update temperature reading
        temperatureReading.setText(temperature + "°C");
        if (temperature >= 36 && temperature <= 37) {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_green);
        } else if (temperature >= 35 && temperature <= 38) {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_yellow);
        } else {
            temperatureStatus.setBackgroundResource(R.drawable.indicator_red);
            showCriticalAlertDialog("Temperature");
        }

        // Update accelerometer reading
        accelerometerReading.setText(accelerometer);
        String[] values = accelerometer.split(", ");
        float x = Float.parseFloat(values[0].split(": ")[1]);
        float y = Float.parseFloat(values[1].split(": ")[1]);
        float z = Float.parseFloat(values[2].split(": ")[1]);

        if (isFallDetected(x, y, z)) {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_red);
            showCriticalAlertDialog("Accelerometer");
        } else if (isExtremeMovement(x, y, z)) {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_yellow);
        } else {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_green);
        }
    }

    private boolean isFallDetected(float x, float y, float z) {
        float threshold = 15.0f; // Example threshold for a fall impact
        return (Math.abs(x) > threshold || Math.abs(y) > threshold || Math.abs(z) > threshold) && (x == 0 && y == 0 && z == 0);
    }

    private boolean isExtremeMovement(float x, float y, float z) {
        float threshold = 5.0f; // Example threshold for extreme movement
        return (Math.abs(x) > threshold || Math.abs(y) > threshold || Math.abs(z) > threshold);
    }

    private void showCriticalAlertDialog(String metric) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your " + metric + " is critical. Do you want to send an alert?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send alert to caretaker
                        sendAlertToCaretaker();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set font size for dialog buttons and message
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextSize(fontSize);
        }
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextSize(fontSize);
        }
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextSize(fontSize);
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to call for help?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send alert to caretaker
                        sendAlertToCaretaker();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set font size for dialog buttons and message
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextSize(fontSize);
        }
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextSize(fontSize);
        }
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextSize(fontSize);
        }
    }

    private void sendAlertToCaretaker() {
        String caretakerId = "C67890"; // Replace with actual caretaker ID
        String patientName = "John Doe"; // Replace with actual patient name

        // Create the message
        RemoteMessage message = new RemoteMessage.Builder(caretakerId + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(messageId.incrementAndGet()))
                .addData("title", "Patient Alert")
                .addData("message", patientName + " has sent an alert.")
                .build();

        // Send the message
        FirebaseMessaging.getInstance().send(message);

        Log.d(TAG, "Alert sent to caretaker with ID: " + caretakerId);
    }

    private void updateFontSize(float fontSize) {
        userNameText.setTextSize(fontSize);
        statusSummary.setTextSize(fontSize);
        temperatureReading.setTextSize(fontSize);
        heartRateReading.setTextSize(fontSize);
        accelerometerReading.setTextSize(fontSize);
        ((TextView) findViewById(R.id.temperatureTitle)).setTextSize(fontSize);
        ((TextView) findViewById(R.id.heartRateTitle)).setTextSize(fontSize);
        ((TextView) findViewById(R.id.accelerometerTitle)).setTextSize(fontSize);
        callForHelpButton.setTextSize(fontSize);

        // Update settings button size
        int sizeInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fontSize, getResources().getDisplayMetrics());
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) settingsButton.getLayoutParams();
        params.width = sizeInDp;
        params.height = sizeInDp;
        settingsButton.setLayoutParams(params);
    }

    private void saveHeartRateToDatabase() {
        String userId = "100"; // Replace with actual user ID
        long timestamp = System.currentTimeMillis();
        String heartRateValue = heartRateReading.getText().toString();

        Map<String, Object> heartRateData = new HashMap<>();
        heartRateData.put("id", userId);
        heartRateData.put("heartVal", Integer.parseInt(heartRateValue.replace(" bpm", "").trim()));
        heartRateData.put("heartTime", timestamp);

        heartRateRef.push().setValue(heartRateData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Heart rate data saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save heart rate data", e));
    }

    private void saveTemperatureToDatabase() {
        String userId = "100"; // Replace with actual user ID
        long timestamp = System.currentTimeMillis();
        String temperatureValue = temperatureReading.getText().toString();

        Map<String, Object> temperatureData = new HashMap<>();
        temperatureData.put("id", userId);
        temperatureData.put("temperatureVal", Float.parseFloat(temperatureValue.replace("°C", "").trim()));
        temperatureData.put("temperatureTime", timestamp);

        temperatureRef.push().setValue(temperatureData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Temperature data saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save temperature data", e));
    }
}
