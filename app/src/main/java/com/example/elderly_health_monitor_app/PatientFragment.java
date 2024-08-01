package com.example.elderly_health_monitor_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;
import java.util.Objects;

public class PatientFragment extends Fragment {

    private TextInputLayout tilFirstName, tilLastName, tilPhoneNumber, tilMedicalCard, tilPassword, tilConfirmPassword, tilDob, tilAge, tilGender, tilEmergencyContact;
    private TextInputEditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextMedicalCard, editTextPassword, editTextConfirmPassword, editTextDob, editTextAge, editTextEmergencyContact;
    private AutoCompleteTextView editTextGender;
    private MaterialButton buttonCreateUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient, container, false);

        initializeViews(view);
        setupFirebase();
        setupGenderDropdown();
        setupDatePicker();
        setupCreateUserButton();
        setupToolbar(view);

        return view;
    }

    private void initializeViews(View view) {
        tilFirstName = view.findViewById(R.id.tilFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        tilPhoneNumber = view.findViewById(R.id.tilPhoneNumber);
        tilMedicalCard = view.findViewById(R.id.tilMedicalCard);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        tilDob = view.findViewById(R.id.tilDob);
        tilAge = view.findViewById(R.id.tilAge);
        tilGender = view.findViewById(R.id.tilGender);
        tilEmergencyContact = view.findViewById(R.id.tilEmergencyContact);

        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextPhoneNumber = view.findViewById(R.id.editTextPhoneNumber);
        editTextMedicalCard = view.findViewById(R.id.editTextMedicalCard);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        editTextDob = view.findViewById(R.id.editTextDob);
        editTextAge = view.findViewById(R.id.editTextAge);
        editTextGender = view.findViewById(R.id.editTextGender);
        editTextEmergencyContact = view.findViewById(R.id.editTextEmergencyContact);

        buttonCreateUser = view.findViewById(R.id.buttonCreateUser);
    }

    private void setupFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");
    }

    private void setupGenderDropdown() {
        String[] genderOptions = {"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_item, genderOptions);
        editTextGender.setAdapter(genderAdapter);
    }

    private void setupDatePicker() {
        editTextDob.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(), // Use getActivity() for context
                R.style.CustomDatePickerDialog, // Apply the custom style here
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    editTextDob.setText(date);
                    calculateAge(selectedYear, selectedMonth, selectedDay);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        if (age < 20) {
            editTextDob.setText("");
            editTextAge.setText("");
            Toast.makeText(getActivity(), "Age must be at least 20 years", Toast.LENGTH_SHORT).show();
        } else {
            editTextAge.setText(String.valueOf(age));
        }
    }

    private void setupCreateUserButton() {
        buttonCreateUser.setOnClickListener(v -> validateAndCreateUser());
    }

    private void setupToolbar(View view) {
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
    }

    private void validateAndCreateUser() {
        if (validateInputs()) {
            checkUniqueFields();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate first name
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextFirstName.getText()).toString().trim())) {
            tilFirstName.setError("First name is required");
            isValid = false;
        } else {
            tilFirstName.setError(null);
        }

        // Validate last name
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextLastName.getText()).toString().trim())) {
            tilLastName.setError("Last name is required");
            isValid = false;
        } else {
            tilLastName.setError(null);
        }

        // Validate phone number
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextPhoneNumber.getText()).toString().trim())) {
            tilPhoneNumber.setError("Phone number is required");
            isValid = false;
        } else {
            tilPhoneNumber.setError(null);
        }

        // Validate medical card
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextMedicalCard.getText()).toString().trim())) {
            tilMedicalCard.setError("Medical card is required");
            isValid = false;
        } else {
            tilMedicalCard.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextPassword.getText()).toString().trim())) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // Validate confirm password
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextConfirmPassword.getText()).toString().trim())) {
            tilConfirmPassword.setError("Confirm password is required");
            isValid = false;
        } else if (!editTextPassword.getText().toString().trim().equals(editTextConfirmPassword.getText().toString().trim())) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        // Validate date of birth
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextDob.getText()).toString().trim())) {
            tilDob.setError("Date of birth is required");
            isValid = false;
        } else {
            tilDob.setError(null);
        }

        // Validate gender
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextGender.getText()).toString().trim())) {
            tilGender.setError("Gender is required");
            isValid = false;
        } else {
            tilGender.setError(null);
        }

        // Validate emergency contact
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextEmergencyContact.getText()).toString().trim())) {
            tilEmergencyContact.setError("Emergency contact is required");
            isValid = false;
        } else {
            tilEmergencyContact.setError(null);
        }

        return isValid;
    }

    private void checkUniqueFields() {
        final String phoneNumber = Objects.requireNonNull(editTextPhoneNumber.getText()).toString().trim();
        final String medicalCard = Objects.requireNonNull(editTextMedicalCard.getText()).toString().trim();

        usersRef.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    tilPhoneNumber.setError("Phone number already registered");
                } else {
                    usersRef.orderByChild("medicalCard").equalTo(medicalCard).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                tilMedicalCard.setError("Medical card already registered");
                            } else {
                                createUserAccount();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            showError("Database error");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Database error");
            }
        });
    }

    private void createUserAccount() {
        String firstName = Objects.requireNonNull(editTextFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(editTextLastName.getText()).toString().trim();
        String phoneNumber = Objects.requireNonNull(editTextPhoneNumber.getText()).toString().trim();
        String medicalCard = Objects.requireNonNull(editTextMedicalCard.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        String dob = Objects.requireNonNull(editTextDob.getText()).toString().trim();
        String age = Objects.requireNonNull(editTextAge.getText()).toString().trim();
        String gender = Objects.requireNonNull(editTextGender.getText()).toString().trim();
        String emergencyContact = Objects.requireNonNull(editTextEmergencyContact.getText()).toString().trim();

        String userId = usersRef.push().getKey();

        User user = new User(userId, firstName, lastName, phoneNumber, medicalCard, password, "user", dob, Integer.parseInt(age), gender, emergencyContact);

        if (userId != null) {
            usersRef.child(userId).setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        saveLoginState("user", firstName, lastName, userId);
                        Toast.makeText(getActivity(), "User account created successfully", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity("user", firstName, lastName, userId);
                    })
                    .addOnFailureListener(e -> showError("Failed to create user account"));
        }
    }

    private void saveLoginState(String role, String firstName, String lastName, String userId) {
        SharedPreferences prefs = getActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", role);
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("userId", userId);
        editor.apply();
    }

    private void navigateToMainActivity(String role, String firstName, String lastName, String userId) {
        Intent intent;
        if ("caretaker".equals(role)) {
            intent = new Intent(getActivity(), CaretakerMonitorActivity.class);
            intent.putExtra("caretakerName", firstName + " " + lastName);
            intent.putExtra("caretakerId", userId);
        } else {
            intent = new Intent(getActivity(), MonitorActivity.class);
            intent.putExtra("userName", firstName + " " + lastName);
            intent.putExtra("userId", userId);
        }
        startActivity(intent);
        getActivity().finish();
    }

    private void showError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public static class User {
        public String id;
        public String firstName;
        public String lastName;
        public String phoneNumber;
        public String medicalCard;
        public String password;
        public String role;
        public String dob;
        public int age;
        public String gender;
        public String emergencyContact;

        public User() {
        }

        public User(String id, String firstName, String lastName, String phoneNumber, String medicalCard, String password, String role, String dob, int age, String gender, String emergencyContact) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.medicalCard = medicalCard;
            this.password = password;
            this.role = role;
            this.dob = dob;
            this.age = age;
            this.gender = gender;
            this.emergencyContact = emergencyContact;
        }
    }
}
