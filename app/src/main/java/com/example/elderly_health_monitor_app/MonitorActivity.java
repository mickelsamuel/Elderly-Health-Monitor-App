package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

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
    private Button triggerCriticalAlertButton;  // New button for testing
    private ImageButton settingsButton;
    private CardView heartRateCard;
    private CardView temperatureCard;
    private CardView accelerometerCard;

    private static final String TAG = "MonitorActivity";
    private static final AtomicInteger messageId = new AtomicInteger();

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
        triggerCriticalAlertButton = findViewById(R.id.triggerCriticalAlertButton);  // New button

        heartRateCard = findViewById(R.id.heartRateCard);
        temperatureCard = findViewById(R.id.temperatureCard);
        accelerometerCard = findViewById(R.id.accelerometerCard);

        // Retrieve user details from intent
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");

        userNameText.setText("Hello, " + userName);

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

        // Set OnClickListener for the Trigger Critical Alert button
        triggerCriticalAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Manually trigger a critical alert
                updateReadings(110, 35.0f, "X: 0.0, Y: 0.0, Z: 0.0"); // Adjust values to trigger critical alerts
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

        Log.d(TAG, "onCreate: Views initialized");
    }

    @Override
    protected void onResume() {
        super.onResume();
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
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
        if (accelerometer.equals("X: 0.0, Y: 0.0, Z: 0.0")) { // Example condition for fall
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_red);
            showCriticalAlertDialog("Accelerometer");
        } else {
            accelerometerStatus.setBackgroundResource(R.drawable.indicator_green);
        }
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
}
