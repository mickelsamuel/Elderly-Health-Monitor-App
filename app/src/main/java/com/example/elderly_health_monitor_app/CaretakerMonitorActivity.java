package com.example.elderly_health_monitor_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaretakerMonitorActivity extends AppCompatActivity {

    private TextView userNameText;
    private TextView statusSummary;
    private Button addPatientButton;
    private Spinner sortSpinner;
    private Button toggleSortOrderButton;
    private ImageButton settingsButton;
    private LinearLayout patientContainer;

    private ArrayList<Patient> patients = new ArrayList<>();
    private boolean isAscending = true;
    private DatabaseReference databaseRef;

    private static final String TAG = "CaretakerMonitorActivity";
    private static final String CHANNEL_ID = "patient_alerts_channel";

    private String caretakerId;
    private String caretakerPhoneNumber;

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
        patientContainer = findViewById(R.id.patientContainer);

        Intent intent = getIntent();
        String caretakerName = intent.getStringExtra("caretakerName");
        caretakerId = intent.getStringExtra("caretakerId");
        caretakerPhoneNumber = intent.getStringExtra("caretakerPhoneNumber");

        Log.d(TAG, "onCreate: Caretaker details - Name: " + caretakerName + ", ID: " + caretakerId + ", Phone: " + caretakerPhoneNumber);

        userNameText.setText("Hello, " + caretakerName + "\n(" + caretakerId + ")");

        // Load caretaker's patients
        loadCaretakerPatients();

        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        addPatientButton.setOnClickListener(v -> {
            Log.d(TAG, "addPatientButton onClick: caretakerId=" + caretakerId + ", caretakerName=" + caretakerName + ", caretakerPhoneNumber=" + caretakerPhoneNumber);
            Intent intent1 = new Intent(CaretakerMonitorActivity.this, AddPatientActivity.class);
            intent1.putExtra("caretakerID", caretakerId);
            intent1.putExtra("caretakerName", caretakerName);
            intent1.putExtra("caretakerPhoneNumber", caretakerPhoneNumber);
            startActivityForResult(intent1, 1);
        });

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortPatients(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        toggleSortOrderButton.setOnClickListener(v -> {
            isAscending = !isAscending;
            int selectedPosition = sortSpinner.getSelectedItemPosition();
            sortPatients(selectedPosition);
        });

        settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(CaretakerMonitorActivity.this, CaretakerSettingsActivity.class);
            settingsIntent.putExtra("caretakerLicense", caretakerId);
            startActivity(settingsIntent);
        });

        FirebaseMessaging.getInstance().subscribeToTopic(caretakerId)
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Subscribed to patient alerts" : "Subscription failed";
                    Log.d(TAG, msg);
                });

        registerReceiver(new FontSizeUpdateReceiver(), new IntentFilter("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE"));

        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
    }

    private void loadCaretakerPatients() {
        DatabaseReference caretakerRef = FirebaseDatabase.getInstance().getReference("users").child(caretakerId);
        caretakerRef.child("patientIDs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> patientIDs = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String patientId = snapshot.getValue(String.class);
                        if (patientId != null) {
                            patientIDs.add(patientId);
                        }
                    }
                }
                Log.d(TAG, "Patient IDs found: " + patientIDs);
                if (!patientIDs.isEmpty()) {
                    loadPatientDetails(patientIDs);
                } else {
                    Log.d(TAG, "No patient IDs found for caretaker: " + caretakerId);
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load caretaker's patient list: " + databaseError.getMessage());
            }
        });
    }

    private void loadPatientDetails(List<String> patientIDs) {
        patients.clear();
        final int[] patientsLoaded = {0};
        final int totalPatients = patientIDs.size();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        //Log.d(TAG, "Fetching patient details from path: " + usersRef.getPath().toString());

        for (String patientId : patientIDs) {
            Log.d(TAG, "Fetching details for patient ID: " + patientId);
            usersRef.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Patient patient = dataSnapshot.getValue(Patient.class);
                    if (patient != null) {
                        patients.add(patient);
                        Log.d(TAG, "Loaded patient: " + patient.getFirstName() + " " + patient.getLastName());
                    } else {
                        Log.d(TAG, "Patient details not found for ID: " + patientId);
                    }
                    patientsLoaded[0]++;
                    if (patientsLoaded[0] == totalPatients) {
                        updateUI();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to load patient details: " + databaseError.getMessage());
                    patientsLoaded[0]++;
                    if (patientsLoaded[0] == totalPatients) {
                        updateUI();
                    }
                }
            });
        }
    }

    private void updateUI() {
        runOnUiThread(() -> {
            Log.d(TAG, "Updating UI with " + patients.size() + " patients");
            patientContainer.removeAllViews();
            for (Patient patient : patients) {
                addPatientCard(patient);
            }
            Log.d(TAG, "UI updated with " + patients.size() + " patients");
        });
    }

    private void updatePatientViews() {
        Log.d(TAG, "Updating patient views");
        patientContainer.removeAllViews();
        for (Patient patient : patients) {
            addPatientCard(patient);
        }
    }

    private void addPatientCard(Patient patient) {
        Log.d(TAG, "Adding card for patient: " + patient.getFirstName() + " " + patient.getLastName());
        View patientCardView = LayoutInflater.from(this).inflate(R.layout.patient_card, patientContainer, false);

        TextView patientNameTextView = patientCardView.findViewById(R.id.patientNameTextView);
        TextView patientIDTextView = patientCardView.findViewById(R.id.patientIDTextView);
        TextView patientHeartRateText = patientCardView.findViewById(R.id.patientHeartRateText);
        TextView patientTemperatureText = patientCardView.findViewById(R.id.patientTemperatureText);
        TextView patientAccelerometerText = patientCardView.findViewById(R.id.patientAccelerometerText);
        Button removePatientButton = patientCardView.findViewById(R.id.removePatientButton);
        Button infoPatientButton = patientCardView.findViewById(R.id.infoPatientButton);

        patientNameTextView.setText(patient.getFirstName() != null ? patient.getFirstName() + " " + patient.getLastName() : "N/A");
        patientIDTextView.setText(patient.getId() != null ? patient.getId() : "N/A");
        patientHeartRateText.setText("Heart Rate: " + (patient.getHeartRate() != 0 ? patient.getHeartRate() : "N/A"));
        patientTemperatureText.setText("Temperature: " + (patient.getTemperature() != 0 ? patient.getTemperature() + "°C" : "N/A"));
        patientAccelerometerText.setText("Accelerometer: X: 0.0, Y: 0.0, Z: 0.0");

        removePatientButton.setOnClickListener(v -> removePatient(patient));
        infoPatientButton.setOnClickListener(v -> showPatientInfo(patient));

        patientContainer.addView(patientCardView);
        Log.d(TAG, "Patient card added for: " + patient.getFirstName() + " " + patient.getLastName());
    }

    private void showPatientInfo(Patient patient) {
        Intent intent = new Intent(this, PatientInfoActivity.class);
        intent.putExtra("patientId", patient.getId());
        startActivity(intent);
    }

    private void sortPatients(int position) {
        Comparator<Patient> comparator;

        switch (position) {
            case 0:
                comparator = Comparator.comparing(Patient::getFirstName);
                break;
            case 1:
                comparator = Comparator.comparing(Patient::getId);
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
                comparator = Comparator.comparing(Patient::getFirstName);
        }

        if (!isAscending) {
            comparator = comparator.reversed();
        }

        Collections.sort(patients, comparator);
        updatePatientViews();
    }

    private void removePatient(Patient patient) {
        if (patient == null) {
            Log.e(TAG, "removePatient: Patient object is null");
            return;
        }

        String patientID = patient.getId();
        if (patientID == null) {
            Log.e(TAG, "removePatient: Patient ID is null");
            return;
        }

        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("users").child(patientID);
        if (patientRef == null) {
            Log.e(TAG, "removePatient: Failed to get DatabaseReference for patient ID: " + patientID);
            return;
        }

        Log.d(TAG, "Removing caretaker information for patient: " + patientID);

        Map<String, Object> updates = new HashMap<>();
        updates.put("caretakerID", null);
        updates.put("caretakerName", null);
        updates.put("caretakerPhoneNumber", null);
        updates.put("lastVisitDate", null);

        Log.d(TAG, "Updating patient fields: " + updates);
        patientRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Successfully removed caretaker information and last visit date from patient: " + patientID);

                databaseRef.child(caretakerId).child("patientIDs").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> patientIDs = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                patientIDs.add(snapshot.getValue(String.class));
                            }
                        }
                        Log.d(TAG, "Retrieved caretaker's patient list: " + dataSnapshot);
                        Log.d(TAG, "Current patient IDs: " + patientIDs);

                        patientIDs.remove(patientID);
                        Log.d(TAG, "Updated patient IDs: " + patientIDs);

                        databaseRef.child(caretakerId).child("patientIDs").setValue(patientIDs).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Patient ID removed from caretaker's list successfully.");
                                patients.remove(patient);
                                updatePatientViews();
                                Toast.makeText(CaretakerMonitorActivity.this, "Patient removed successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "Failed to remove patient ID from caretaker's list: " + task.getException().getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                    }
                });

            } else {
                Log.e(TAG, "Failed to remove caretaker information and last visit date from patient: " + task.getException().getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String firstName = data.getStringExtra("patientFirstName");
            String lastName = data.getStringExtra("patientLastName");
            String dob = data.getStringExtra("dob");
            String patientID = data.getStringExtra("patientID");
            String gender = data.getStringExtra("gender");
            int age = data.getIntExtra("age", 0);
            String lastVisitDate = data.getStringExtra("lastVisitDate");

            Log.d(TAG, "New patient added: " + firstName + " " + lastName + " (" + patientID + ")");

            Patient newPatient = new Patient(firstName, lastName, dob, patientID);
            newPatient.setGender(gender);
            newPatient.setAge(age);
            newPatient.setLastVisitDate(lastVisitDate);
            boolean patientExists = false;
            for (Patient patient : patients) {
                if (patient.getId() != null && patient.getId().equals(patientID)) {
                    patientExists = true;
                    break;
                }
            }
            if (!patientExists) {
                patients.add(newPatient);
                addPatientCard(newPatient);
                addPatientToCaretaker(caretakerId, patientID);
            }
        }
    }

    private void addPatientToCaretaker(String caretakerId, String patientId) {
        DatabaseReference caretakerRef = FirebaseDatabase.getInstance().getReference("users").child(caretakerId);
        caretakerRef.child("patientIDs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> patientIDs = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        patientIDs.add(snapshot.getValue(String.class));
                    }
                }
                if (!patientIDs.contains(patientId)) {
                    patientIDs.add(patientId);
                    caretakerRef.child("patientIDs").setValue(patientIDs).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Patient ID added to caretaker's list successfully.");
                        } else {
                            Log.e(TAG, "Failed to add patient ID to caretaker's list: " + task.getException().getMessage());
                        }
                    });
                } else {
                    Log.d(TAG, "Patient ID already exists in caretaker's list.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
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

        for (int i = 0; i < sortSpinner.getCount(); i++) {
            View item = sortSpinner.getSelectedView();
            if (item != null && item instanceof TextView) {
                ((TextView) item).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            }
        }

        for (int i = 0; i < patientContainer.getChildCount(); i++) {
            View patientCardView = patientContainer.getChildAt(i);

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
