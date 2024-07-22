package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddPatientActivity extends AppCompatActivity {

    private EditText patientNameInput;
    private EditText patientDOBInput;
    private EditText patientIDInput;
    private EditText patientGenderInput;
    private EditText patientAgeInput;
    private EditText patientLastVisitDateInput;
    private Button savePatientButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        patientNameInput = findViewById(R.id.patientNameInput);
        patientDOBInput = findViewById(R.id.patientDOBInput);
        patientIDInput = findViewById(R.id.patientIDInput);
        patientGenderInput = findViewById(R.id.patientGenderInput);
        patientAgeInput = findViewById(R.id.patientAgeInput);
        patientLastVisitDateInput = findViewById(R.id.patientLastVisitDateInput);
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);

        savePatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = patientNameInput.getText().toString();
                String dob = patientDOBInput.getText().toString();
                String patientID = patientIDInput.getText().toString();
                String gender = patientGenderInput.getText().toString();
                int age = Integer.parseInt(patientAgeInput.getText().toString());
                String lastVisitDate = patientLastVisitDateInput.getText().toString();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("name", name);
                resultIntent.putExtra("dob", dob);
                resultIntent.putExtra("patientID", patientID);
                resultIntent.putExtra("gender", gender);
                resultIntent.putExtra("age", age);
                resultIntent.putExtra("lastVisitDate", lastVisitDate);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
