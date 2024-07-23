package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CaretakerMonitorActivity extends AppCompatActivity {

    private TextView userNameText;
    private TextView statusSummary;
    private Button addPatientButton;
    private Spinner sortSpinner;
    private Button toggleSortOrderButton;

    private ArrayList<Patient> patients = new ArrayList<>();
    private boolean isAscending = true; // Default sorting order

    private static final String TAG = "CaretakerMonitorActivity";
    private static final String CHANNEL_ID = "patient_alerts_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caretaker_monitor);

        userNameText = findViewById(R.id.userNameText);
        statusSummary = findViewById(R.id.statusSummary);
        addPatientButton = findViewById(R.id.addPatientButton);
        sortSpinner = findViewById(R.id.sortSpinner);
        toggleSortOrderButton = findViewById(R.id.toggleSortOrderButton);

        // Retrieve caretaker details from intent
        Intent intent = getIntent();
        String caretakerName = intent.getStringExtra("caretakerName");
        String caretakerLicense = intent.getStringExtra("caretakerLicense");

        Log.d(TAG, "Caretaker details - Name: " + caretakerName + ", License: " + caretakerLicense);

        userNameText.setText("Hello, " + caretakerName + " (" + caretakerLicense + ")");

        // Setup Spinner for sorting
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        addPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaretakerMonitorActivity.this, AddPatientActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // Handle sorting selection
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortPatients(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Toggle sorting order
        toggleSortOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAscending = !isAscending;
                int selectedPosition = sortSpinner.getSelectedItemPosition();
                sortPatients(selectedPosition);
            }
        });

        // Initialize Firebase Messaging
        FirebaseMessaging.getInstance().subscribeToTopic(caretakerLicense)
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Subscribed to patient alerts" : "Subscription failed";
                    Log.d(TAG, msg);
                });

        // Create notification channel
        createNotificationChannel();

        // TODO: Load existing patients from database or other storage
    }

    private void sortPatients(int position) {
        Comparator<Patient> comparator;

        switch (position) {
            case 0: // Sort by name
                comparator = Comparator.comparing(Patient::getName);
                break;
            case 1: // Sort by patient ID
                comparator = Comparator.comparing(Patient::getPatientID);
                break;
            case 2: // Sort by date of birth
                comparator = Comparator.comparing(Patient::getDob);
                break;
            case 4: // Sort by temperature
                comparator = Comparator.comparingDouble(Patient::getTemperature);
                break;
            case 5: // Sort by heart rate
                comparator = Comparator.comparingInt(Patient::getHeartRate);
                break;
            case 6: // Sort by gender
                comparator = Comparator.comparing(Patient::getGender);
                break;
            case 7: // Sort by age
                comparator = Comparator.comparingInt(Patient::getAge);
                break;
            case 8: // Sort by last visit date
                comparator = Comparator.comparing(Patient::getLastVisitDate);
                break;
            default:
                comparator = Comparator.comparing(Patient::getName);
        }

        if (!isAscending) {
            comparator = comparator.reversed();
        }

        Collections.sort(patients, comparator);

        // Update UI with sorted patients
        updatePatientCards();
    }

    private void updatePatientCards() {
        // TODO: Update the UI to display sorted patient cards
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Handle new patient data from AddPatientActivity
            String name = data.getStringExtra("name");
            String dob = data.getStringExtra("dob");
            String patientID = data.getStringExtra("patientID");
            String gender = data.getStringExtra("gender");
            int age = data.getIntExtra("age", 0);
            String lastVisitDate = data.getStringExtra("lastVisitDate");

            Patient newPatient = new Patient(name, dob, patientID);
            newPatient.setGender(gender);
            newPatient.setAge(age);
            newPatient.setLastVisitDate(lastVisitDate);
            patients.add(newPatient);
            updatePatientCards();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Patient Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for patient alerts");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void handleIncomingNotification(String title, String message) {
        Intent intent = new Intent(this, CaretakerMonitorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);

        Uri defaultSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
