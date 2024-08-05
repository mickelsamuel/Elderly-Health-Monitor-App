package com.example.elderly_health_monitor_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    // List of patients to display
    private List<Patient> patients;
    // Context for inflating layouts
    private Context context;
    // Listener for handling remove button click events
    private OnRemoveButtonClickListener removeButtonClickListener;

    // Interface for the remove button click listener
    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClick(Patient patient);
    }

    // Constructor to initialize the adapter with context, patient list, and remove button listener
    public PatientAdapter(Context context, List<Patient> patients, OnRemoveButtonClickListener listener) {
        this.context = context;
        this.patients = patients;
        this.removeButtonClickListener = listener;
    }

    // Method to create and return a new ViewHolder for each list item
    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each list item
        View view = LayoutInflater.from(context).inflate(R.layout.patient_list_item, parent, false);
        return new PatientViewHolder(view);
    }

    // Method to bind data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        // Get the current patient
        Patient patient = patients.get(position);
        // Set the patient details in the respective TextViews
        holder.patientNameTextView.setText(patient.getFirstName() + " " + patient.getLastName());
        holder.patientIDTextView.setText(patient.getPatientID());
        holder.patientHeartRateText.setText("Heart Rate: " + patient.getHeartRate());
        holder.patientTemperatureText.setText("Temperature: " + patient.getTemperature() + "°C");
        holder.patientAccelerometerText.setText("Accelerometer: X: " + patient.getAccelerometerX() + ", Y: " + patient.getAccelerometerY() + ", Z: " + patient.getAccelerometerZ());

        // Set the remove button click listener
        holder.removePatientButton.setOnClickListener(v -> removeButtonClickListener.onRemoveButtonClick(patient));
    }

    // Method to get the total number of patients
    @Override
    public int getItemCount() {
        return patients.size();
    }

    // ViewHolder class to hold the views for each list item
    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        // TextViews and Button for displaying patient details and removing the patient
        TextView patientNameTextView;
        TextView patientIDTextView;
        TextView patientHeartRateText;
        TextView patientTemperatureText;
        TextView patientAccelerometerText;
        Button removePatientButton;

        // Constructor to initialize the views
        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            patientNameTextView = itemView.findViewById(R.id.patientNameTextView);
            patientIDTextView = itemView.findViewById(R.id.patientIDTextView);
            patientHeartRateText = itemView.findViewById(R.id.patientHeartRateText);
            patientTemperatureText = itemView.findViewById(R.id.patientTemperatureText);
            patientAccelerometerText = itemView.findViewById(R.id.patientAccelerometerText);
            removePatientButton = itemView.findViewById(R.id.removePatientButton);
        }
    }
}
