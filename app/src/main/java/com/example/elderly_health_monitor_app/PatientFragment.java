package com.example.elderly_health_monitor_app;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.Objects;

public class PatientFragment extends Fragment {

    private TextInputLayout tilFirstName, tilLastName, tilPhoneNumber, tilMedicalCard, tilPassword, tilDob, tilAge, tilGender;
    private TextInputEditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextMedicalCard, editTextPassword, editTextDob, editTextAge;
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
        tilDob = view.findViewById(R.id.tilDob);
        tilAge = view.findViewById(R.id.tilAge);
        tilGender = view.findViewById(R.id.tilGender);

        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextPhoneNumber = view.findViewById(R.id.editTextPhoneNumber);
        editTextMedicalCard = view.findViewById(R.id.editTextMedicalCard);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextDob = view.findViewById(R.id.editTextDob);
        editTextAge = view.findViewById(R.id.editTextAge);
        editTextGender = view.findViewById(R.id.editTextGender);

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
                requireContext(),
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

        editTextAge.setText(String.valueOf(age));
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
            createUserAccount();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextFirstName.getText()).toString().trim())) {
            tilFirstName.setError("First name is required");
            isValid = false;
        } else {
            tilFirstName.setError(null);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextLastName.getText()).toString().trim())) {
            tilLastName.setError("Last name is required");
            isValid = false;
        } else {
            tilLastName.setError(null);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextPhoneNumber.getText()).toString().trim())) {
            tilPhoneNumber.setError("Phone number is required");
            isValid = false;
        } else {
            tilPhoneNumber.setError(null);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextMedicalCard.getText()).toString().trim())) {
            tilMedicalCard.setError("Medical card is required");
            isValid = false;
        } else {
            tilMedicalCard.setError(null);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextPassword.getText()).toString().trim())) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextDob.getText()).toString().trim())) {
            tilDob.setError("Date of birth is required");
            isValid = false;
        } else {
            tilDob.setError(null);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextAge.getText()).toString().trim())) {
            tilAge.setError("Age is required");
            isValid = false;
        } else {
            tilAge.setError(null);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextGender.getText()).toString().trim())) {
            tilGender.setError("Gender is required");
            isValid = false;
        } else {
            tilGender.setError(null);
        }

        return isValid;
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

        String userId = usersRef.push().getKey();

        User user = new User(userId, firstName, lastName, phoneNumber, medicalCard, password, "user", dob, Integer.parseInt(age), gender);

        if (userId != null) {
            usersRef.child(userId).setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getActivity(), "User account created successfully", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    })
                    .addOnFailureListener(e -> showError("Failed to create user account"));
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
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

        public User() {
        }

        public User(String id, String firstName, String lastName, String phoneNumber, String medicalCard, String password, String role, String dob, int age, String gender) {
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
        }
    }
}