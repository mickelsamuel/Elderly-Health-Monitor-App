package com.example.elderly_health_monitor_app;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public class HealthMonitorAlerts {

    private static final String TAG = "HealthMonitorAlerts";
    private Context context;
    private CaretakerMonitorActivity caretakerMonitorActivity;

    public HealthMonitorAlerts(Context context, CaretakerMonitorActivity caretakerMonitorActivity) {
        this.context = context;
        this.caretakerMonitorActivity = caretakerMonitorActivity;
    }

    // Method to check and trigger alerts for heart rate conditions
    public void checkHeartRateAlerts(ArrayList<DataSnapshot> dataSnapshots, long sevenDaysAgo, long now) {
        Long tachyStartTime = null, bradyStartTime = null, irregularStartTime = null;

        for (DataSnapshot snapshot : dataSnapshots) {
            Long heartVal = snapshot.child("heartVal").getValue(Long.class);
            Long heartTime = snapshot.child("heartTime").getValue(Long.class);

            if (heartVal != null && heartTime != null) {
                if (heartTime >= sevenDaysAgo && heartTime <= now) {

                    // Check for Tachycardia (Heart rate > 100 bpm)
                    if (heartVal > 100) {
                        if (tachyStartTime == null) {
                            tachyStartTime = heartTime;
                        } else if (heartTime - tachyStartTime > 15 * 60 * 1000) {
                            sendNotification("Tachycardia Alert", "Heart rate above 100 bpm for more than 15 minutes.", snapshot);
                        }
                    } else {
                        tachyStartTime = null;
                    }

                    // Check for Bradycardia (Heart rate < 60 bpm)
                    if (heartVal < 60) {
                        if (bradyStartTime == null) {
                            bradyStartTime = heartTime;
                        } else if (heartTime - bradyStartTime > 15 * 60 * 1000) {
                            sendNotification("Bradycardia Alert", "Heart rate below 60 bpm for more than 15 minutes.", snapshot);
                        }
                    } else {
                        bradyStartTime = null;
                    }

                    // Check for Irregular Heartbeat (Significant change in heart rate)
                    if (irregularStartTime != null) {
                        long lastHeartRate = irregularStartTime;
                        if (Math.abs(heartVal - lastHeartRate) > 30) {
                            if (irregularStartTime == null) {
                                irregularStartTime = heartTime;
                            } else if (heartTime - irregularStartTime > 15 * 60 * 1000) {
                                sendNotification("Irregular Heartbeat Alert", "Significant variation in heart rate detected.", snapshot);
                            }
                        } else {
                            irregularStartTime = null;
                        }
                    }
                }
            }
        }
    }

    // Method to check and trigger alerts for temperature conditions
    public void checkTemperatureAlerts(ArrayList<DataSnapshot> dataSnapshots, long sevenDaysAgo, long now) {
        Long feverStartTime = null, hypoStartTime = null;

        for (DataSnapshot snapshot : dataSnapshots) {
            Double temperatureVal = snapshot.child("temperatureVal").getValue(Double.class);
            Long temperatureTime = snapshot.child("temperatureTime").getValue(Long.class);

            if (temperatureVal != null && temperatureTime != null) {
                if (temperatureTime >= sevenDaysAgo && temperatureTime <= now) {

                    // Check for Fever (Temperature > 38°C)
                    if (temperatureVal > 38.0) {
                        if (feverStartTime == null) {
                            feverStartTime = temperatureTime;
                        } else if (temperatureTime - feverStartTime > 15 * 60 * 1000) {
                            sendNotification("Fever Alert", "Temperature above 38°C for more than 15 minutes.", snapshot);
                        }
                    } else {
                        feverStartTime = null;
                    }

                    // Check for Hypothermia (Temperature < 35°C)
                    if (temperatureVal < 35.0) {
                        if (hypoStartTime == null) {
                            hypoStartTime = temperatureTime;
                        } else if (temperatureTime - hypoStartTime > 15 * 60 * 1000) {
                            sendNotification("Hypothermia Alert", "Temperature below 35°C for more than 15 minutes.", snapshot);
                        }
                    } else {
                        hypoStartTime = null;
                    }
                }
            }
        }
    }

    // Method to send notifications based on alert conditions
    private void sendNotification(String title, String message, DataSnapshot snapshot) {
        String patientId = snapshot.child("patientId").getValue(String.class);
        String patientName = snapshot.child("patientName").getValue(String.class);

        if (caretakerMonitorActivity != null) {
            caretakerMonitorActivity.showNotification(title, message, patientId, patientName);
            caretakerMonitorActivity.showNotificationDialog(title, message, patientId, patientName);
        } else {
            Log.e(TAG, "CaretakerMonitorActivity is not initialized.");
        }

        // Example logging, replace with actual notification logic if needed
        Log.d(TAG, "Sending notification: " + title + " - " + message + " for Patient: " + patientName + " (ID: " + patientId + ")");
    }
}
//EOF