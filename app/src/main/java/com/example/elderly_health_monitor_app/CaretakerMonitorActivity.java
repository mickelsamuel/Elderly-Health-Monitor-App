package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private ImageButton settingsButton;

    private ArrayList<Patient> patients = new ArrayList<>();
    private boolean isAscending = true;
    private DatabaseReference databaseRef;

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
        settingsButton = findViewById(R.id.settingsButton);

        // Retrieve caretaker details from intent
        Intent intent = getIntent();
        String caretakerName = intent.getStringExtra("caretakerName");
        String caretakerId = intent.getStringExtra("caretakerId");

        Log.d(TAG, "Caretaker details - Name: " + caretakerName + ", ID: " + caretakerId);

        userNameText.setText("Hello, " + caretakerName + "\n(" + caretakerId + ")");

        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Setup Spinner for sorting
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        addPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaretakerMonitorActivity.this, AddPatientActivity.class);
                intent.putExtra("caretakerID", caretakerId);
                intent.putExtra("caretakerName", caretakerName);
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

        // Handle settings button click
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(CaretakerMonitorActivity.this, CaretakerSettingsActivity.class);
                settingsIntent.putExtra("caretakerLicense", caretakerId);
                startActivity(settingsIntent);
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic(caretakerId)
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Subscribed to patient alerts" : "Subscription failed";
                    Log.d(TAG, msg);
                });

        createNotificationChannel();

        registerReceiver(new FontSizeUpdateReceiver(), new IntentFilter("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE"));

        // Set initial font sizes based on preferences
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);

        // Load patients from Firebase
        loadPatients(caretakerId);
    }

    private void loadPatients(String caretakerId) {
        databaseRef.orderByChild("caretakerID").equalTo(caretakerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                patients.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient != null && "user".equals(patient.getRole())) {
                        patients.add(patient);
                    }
                }
                updatePatientCards();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to load patients: " + databaseError.getMessage());
            }
        });
    }

    private void sortPatients(int position) {
        Comparator<Patient> comparator;

        switch (position) {
            case 0:
                comparator = Comparator.comparing(Patient::getName);
                break;
            case 1:
                comparator = Comparator.comparing(Patient::getPatientID);
                break;
            case 2:
                comparator = Comparator.comparing(Patient::getDob);
                break;
            case 4:
                comparator = Comparator.comparingDouble(Patient::getTemperature);
                break;
            case 5:
                comparator = Comparator.comparingInt(Patient::getHeartRate);
                break;
            case 6:
                comparator = Comparator.comparing(Patient::getGender);
                break;
            case 7:
                comparator = Comparator.comparingInt(Patient::getAge);
                break;
            case 8:
                comparator = Comparator.comparing(Patient::getLastVisitDate);
                break;
            default:
                comparator = Comparator.comparing(Patient::getName);
        }

        if (!isAscending) {
            comparator = comparator.reversed();
        }

        Collections.sort(patients, comparator);
        updatePatientCards();
    }

    private void updatePatientCards() {
        GridLayout patientGridLayout = findViewById(R.id.patientGridLayout); // Assuming you have an ID for your GridLayout
        patientGridLayout.removeAllViews();

        for (Patient patient : patients) {
            View patientCardView = getLayoutInflater().inflate(R.layout.patient_card, null);

            TextView patientNameTextView = patientCardView.findViewById(R.id.patientNameTextView);
            TextView patientIDTextView = patientCardView.findViewById(R.id.patientIDTextView);
            TextView patientHeartRateText = patientCardView.findViewById(R.id.patientHeartRateText);
            TextView patientTemperatureText = patientCardView.findViewById(R.id.patientTemperatureText);
            TextView patientAccelerometerText = patientCardView.findViewById(R.id.patientAccelerometerText);
            Button removePatientButton = patientCardView.findViewById(R.id.removePatientButton);

            patientNameTextView.setText(patient.getName());
            patientIDTextView.setText(patient.getPatientID());
            patientHeartRateText.setText("Heart Rate: " + patient.getHeartRate());
            patientTemperatureText.setText("Temperature: " + patient.getTemperature() + "°C");
            patientAccelerometerText.setText("Accelerometer: X: " + patient.getAccelerometerX() + ", Y: " + patient.getAccelerometerY() + ", Z: " + patient.getAccelerometerZ());

            // Set the text size
            float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
            patientNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            patientIDTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            patientHeartRateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            patientTemperatureText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            patientAccelerometerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

            // Handle remove button click
            removePatientButton.setOnClickListener(v -> {
                removePatient(patient);
            });

            patientGridLayout.addView(patientCardView);
        }
    }

    private void removePatient(Patient patient) {
        String caretakerId = getIntent().getStringExtra("caretakerId");

        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("users").child(patient.getPatientID());

        Log.d(TAG, "Removing caretaker information for patient: " + patient.getPatientID());

        // Remove caretaker information from patient's account in the database
        patientRef.child("caretakerID").removeValue();
        patientRef.child("caretakerName").removeValue();

        // Remove patient from the local list and update the view
        patients.remove(patient);
        updatePatientCards();

        Toast.makeText(this, "Patient removed successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String name = data.getStringExtra("name");
            String dob = data.getStringExtra("dob");
            String patientID = data.getStringExtra("patientID");
            String gender = data.getStringExtra("gender");
            int age = data.getIntExtra("age", 0);
            String lastVisitDate = data.getStringExtra("lastVisitDate");

            Log.d(TAG, "New patient added: " + name + " (" + patientID + ")");

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

    @Override
    protected void onResume() {
        super.onResume();
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
    }

    private void updateFontSize(float fontSize) {
        userNameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        statusSummary.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        addPatientButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        toggleSortOrderButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        // Update Spinner text size
        for (int i = 0; i < sortSpinner.getCount(); i++) {
            View item = sortSpinner.getSelectedView();
            if (item != null && item instanceof TextView) {
                ((TextView) item).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            }
        }

        // Update font size for patient cards
        updatePatientCardsFontSize(fontSize);
    }

    private void updatePatientCardsFontSize(float fontSize) {
        GridLayout patientGridLayout = findViewById(R.id.patientGridLayout); // Assuming you have an ID for your GridLayout

        for (int i = 0; i < patientGridLayout.getChildCount(); i++) {
            View patientCardView = patientGridLayout.getChildAt(i);

            TextView patientNameTextView = patientCardView.findViewById(R.id.patientNameTextView);
            TextView patientIDTextView = patientCardView.findViewById(R.id.patientIDTextView);
            TextView patientHeartRateText = patientCardView.findViewById(R.id.patientHeartRateText);
            TextView patientTemperatureText = patientCardView.findViewById(R.id.patientTemperatureText);
            TextView patientAccelerometerText = patientCardView.findViewById(R.id.patientAccelerometerText);

            patientNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            patientIDTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            patientHeartRateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            patientTemperatureText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            patientAccelerometerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        }
    }

    private class FontSizeUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float fontSize = intent.getFloatExtra("font_size", 18);
            updateFontSize(fontSize);
        }
    }
}
