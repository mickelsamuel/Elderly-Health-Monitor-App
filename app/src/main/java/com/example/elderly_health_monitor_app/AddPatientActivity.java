package com.example.elderly_health_monitor_app;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class AddPatientActivity extends AppCompatActivity {

    private EditText patientIDInput;
    private EditText patientLastVisitDateInput;
    private Button savePatientButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        patientIDInput = findViewById(R.id.patientIDInput);
        patientLastVisitDateInput = findViewById(R.id.patientLastVisitDateInput);
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);

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

                Intent resultIntent = new Intent();
                resultIntent.putExtra("patientID", patientID);
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

    private class FontSizeUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float fontSize = intent.getFloatExtra("font_size", 18);
            updateFontSize(fontSize);
        }
    }
}
