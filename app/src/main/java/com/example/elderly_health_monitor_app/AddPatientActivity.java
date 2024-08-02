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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddPatientActivity extends AppCompatActivity {

    private EditText patientIDInput;
    private EditText patientLastVisitDateInput;
    private Button savePatientButton;
    private Button backButton;
    private DatabaseReference databaseRef;
    private String caretakerID;
    private String caretakerName;
    private String caretakerPhoneNumber; // Add caretaker phone number

    private static final String TAG = "AddPatientActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        patientIDInput = findViewById(R.id.patientIDInput);
        patientLastVisitDateInput = findViewById(R.id.patientLastVisitDateInput);
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);

        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Retrieve caretaker details from the intent
        caretakerID = getIntent().getStringExtra("caretakerID");
        caretakerName = getIntent().getStringExtra("caretakerName");
        caretakerPhoneNumber = getIntent().getStringExtra("caretakerPhoneNumber"); // Retrieve phone number

        Log.d(TAG, "Caretaker ID: " + caretakerID);
        Log.d(TAG, "Caretaker Name: " + caretakerName);
        Log.d(TAG, "Caretaker Phone Number: " + caretakerPhoneNumber);

        patientLastVisitDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        savePatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientID = patientIDInput.getText().toString();
                String lastVisitDate = patientLastVisitDateInput.getText().toString();

                if (TextUtils.isEmpty(patientID)) {
                    Toast.makeText(AddPatientActivity.this, "Patient ID is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(lastVisitDate)) {
                    Toast.makeText(AddPatientActivity.this, "Last Visit Date is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                validateAndAddPatient(patientID, lastVisitDate);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        registerReceiver(new FontSizeUpdateReceiver(), new IntentFilter("com.example.elderly_health_monitor_app.UPDATE_FONT_SIZE"));

        // Set initial font sizes based on preferences
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
    }

    @Override
    protected void onResume() {
        super.onResume();
        float fontSize = getSharedPreferences("settings", MODE_PRIVATE).getFloat("font_size", 18);
        updateFontSize(fontSize);
    }

    private void updateFontSize(float fontSize) {
        patientIDInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        patientLastVisitDateInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        savePatientButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        backButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddPatientActivity.this,
                R.style.CustomDatePickerDialog, // Apply the custom style here
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        patientLastVisitDateInput.setText(selectedDate);
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void validateAndAddPatient(final String patientID, final String lastVisitDate) {
        Log.d(TAG, "validateAndAddPatient: Patient ID: " + patientID + ", Last Visit Date: " + lastVisitDate);

        databaseRef.child(patientID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && "user".equals(dataSnapshot.child("role").getValue(String.class))) {
                    Log.d(TAG, "User exists and has role 'user'");

                    // Add caretaker details to the patient's data
                    Map<String, Object> patientUpdates = new HashMap<>();
                    patientUpdates.put("lastVisitDate", lastVisitDate);
                    patientUpdates.put("caretakerID", caretakerID);
                    patientUpdates.put("caretakerName", caretakerName);
                    patientUpdates.put("caretakerPhoneNumber", caretakerPhoneNumber); // Add phone number

                    Log.d(TAG, "Updating patient data: " + patientUpdates);

                    databaseRef.child(patientID).updateChildren(patientUpdates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Patient details updated successfully.");
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("patientID", patientID);
                            resultIntent.putExtra("lastVisitDate", lastVisitDate);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Log.e(TAG, "Failed to update patient details: " + task.getException().getMessage());
                            Toast.makeText(AddPatientActivity.this, "Failed to add patient", Toast.LENGTH_SHORT).show();
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

    private class FontSizeUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float fontSize = intent.getFloatExtra("font_size", 18);
            updateFontSize(fontSize);
        }
    }
}
