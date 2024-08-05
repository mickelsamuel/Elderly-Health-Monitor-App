package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPatientActivity extends AppCompatActivity {

    private EditText patientIDInput;
    private EditText patientLastVisitDateInput;
    private Button savePatientButton;
    private Button backButton;
    private DatabaseReference databaseRef;
    private String caretakerID;
    private String caretakerName;
    private String caretakerPhoneNumber;

    private static final String TAG = "AddPatientActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        // Initialize UI components
        patientIDInput = findViewById(R.id.patientIDInput);
        patientLastVisitDateInput = findViewById(R.id.patientLastVisitDateInput);
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);

        // Get Firebase database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Retrieve caretaker details from intent
        caretakerID = getIntent().getStringExtra("caretakerID");
        caretakerName = getIntent().getStringExtra("caretakerName");
        caretakerPhoneNumber = getIntent().getStringExtra("caretakerPhoneNumber");

        Log.d(TAG, "onCreate: Caretaker details - ID: " + caretakerID + ", Name: " + caretakerName + ", Phone: " + caretakerPhoneNumber);

        // Set OnClickListener to show date picker dialog
        patientLastVisitDateInput.setOnClickListener(v -> showDatePickerDialog());

        // Set OnClickListener for save patient button
        savePatientButton.setOnClickListener(v -> {
            String patientID = patientIDInput.getText().toString();
            String lastVisitDate = patientLastVisitDateInput.getText().toString();

            if (TextUtils.isEmpty(patientID) || TextUtils.isEmpty(lastVisitDate)) {
                Toast.makeText(AddPatientActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchAndValidatePatient(patientID, lastVisitDate);
        });

        // Set OnClickListener for back button
        backButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Register broadcast receiver for font size updates
        registerReceiver(new FontSizeUpdateReceiver(), new IntentFilter("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE"));

        // Set initial font size from shared preferences
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update font size when activity resumes
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
    }

    /**
     * Update the font size of UI components
     * @param fontSize The font size to set
     */
    private void updateFontSize(float fontSize) {
        patientIDInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        patientLastVisitDateInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        savePatientButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        backButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    }

    /**
     * Show a date picker dialog to select the last visit date
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddPatientActivity.this,
                R.style.CustomDatePickerDialog,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    patientLastVisitDateInput.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    /**
     * Fetch and validate patient details from Firebase
     * @param patientID The ID of the patient
     * @param lastVisitDate The last visit date of the patient
     */
    private void fetchAndValidatePatient(final String patientID, final String lastVisitDate) {
        Log.d(TAG, "fetchAndValidatePatient: Patient ID: " + patientID + ", Last Visit Date: " + lastVisitDate + ", Caretaker ID: " + caretakerID + ", Caretaker Name: " + caretakerName + ", Caretaker Phone Number: " + caretakerPhoneNumber);

        databaseRef.child(patientID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && "user".equals(dataSnapshot.child("role").getValue(String.class))) {
                    Log.d(TAG, "User exists and has role 'user'");

                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    if (firstName == null || lastName == null) {
                        Log.e(TAG, "Patient first name or last name is missing in the database");
                        Toast.makeText(AddPatientActivity.this, "Patient first name or last name is missing in the database", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String patientName = firstName + " " + lastName;

                    // Fetch caretaker's phone number
                    databaseRef.child(caretakerID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot caretakerSnapshot) {
                            caretakerPhoneNumber = caretakerSnapshot.child("phoneNumber").getValue(String.class);
                            if (caretakerPhoneNumber == null) {
                                Log.e(TAG, "Caretaker phone number is missing in the database");
                                Toast.makeText(AddPatientActivity.this, "Caretaker phone number is missing in the database", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Map<String, Object> patientUpdates = new HashMap<>();
                            patientUpdates.put("lastVisitDate", lastVisitDate);
                            patientUpdates.put("caretakerID", caretakerID);
                            patientUpdates.put("caretakerName", caretakerName);
                            patientUpdates.put("caretakerPhoneNumber", caretakerPhoneNumber);

                            Log.d(TAG, "Updating patient data: " + patientUpdates);

                            databaseRef.child(patientID).updateChildren(patientUpdates).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Patient details updated successfully.");
                                    Toast.makeText(AddPatientActivity.this, "Patient details updated successfully", Toast.LENGTH_SHORT).show();
                                    addPatientToCaretaker(patientID, firstName, lastName);
                                } else {
                                    Log.e(TAG, "Failed to update patient details: " + task.getException().getMessage());
                                    Toast.makeText(AddPatientActivity.this, "Failed to update patient details", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Database error: " + databaseError.getMessage());
                            Toast.makeText(AddPatientActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Invalid patient ID or user role");
                    Toast.makeText(AddPatientActivity.this, "Invalid patient ID or user role", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(AddPatientActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add patient ID to caretaker's list of patients in Firebase
     * @param patientID The ID of the patient
     * @param firstName The first name of the patient
     * @param lastName The last name of the patient
     */
    private void addPatientToCaretaker(final String patientID, final String firstName, final String lastName) {
        databaseRef.child(caretakerID).child("patientIDs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> patientIDs = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        patientIDs.add(snapshot.getValue(String.class));
                    }
                }
                if (!patientIDs.contains(patientID)) {
                    patientIDs.add(patientID);
                    databaseRef.child(caretakerID).child("patientIDs").setValue(patientIDs).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Patient ID added to caretaker's list successfully.");
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("patientID", patientID);
                            resultIntent.putExtra("patientFirstName", firstName);
                            resultIntent.putExtra("patientLastName", lastName);
                            resultIntent.putExtra("lastVisitDate", patientLastVisitDateInput.getText().toString());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Log.e(TAG, "Failed to add patient ID to caretaker's list: " + task.getException().getMessage());
                            Toast.makeText(AddPatientActivity.this, "Failed to add patient ID to caretaker's list", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.d(TAG, "Patient ID already exists in caretaker's list.");
                    Toast.makeText(AddPatientActivity.this, "Patient already exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * BroadcastReceiver to update font size based on settings
     */
    private class FontSizeUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float fontSize = intent.getFloatExtra("font_size", 18);
            updateFontSize(fontSize);
        }
    }
}
