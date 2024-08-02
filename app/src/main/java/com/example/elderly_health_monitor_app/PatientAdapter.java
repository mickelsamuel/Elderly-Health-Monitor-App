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

    private List<Patient> patients;
    private Context context;
    private OnRemoveButtonClickListener removeButtonClickListener;

    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClick(Patient patient);
    }

    public PatientAdapter(Context context, List<Patient> patients, OnRemoveButtonClickListener listener) {
        this.context = context;
        this.patients = patients;
        this.removeButtonClickListener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.patient_list_item, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.patientNameTextView.setText(patient.getName());
        holder.patientIDTextView.setText(patient.getPatientID());
        holder.patientHeartRateText.setText("Heart Rate: " + patient.getHeartRate());
        holder.patientTemperatureText.setText("Temperature: " + patient.getTemperature() + "°C");
        holder.patientAccelerometerText.setText("Accelerometer: X: 0.0, Y: 0.0, Z: 0.0");

        holder.removePatientButton.setOnClickListener(v -> removeButtonClickListener.onRemoveButtonClick(patient));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView patientNameTextView;
        TextView patientIDTextView;
        TextView patientHeartRateText;
        TextView patientTemperatureText;
        TextView patientAccelerometerText;
        Button removePatientButton;

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
