package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CaretakerFragment extends Fragment {

    // Declare UI components
    private TextInputLayout tilFirstName, tilLastName, tilPhoneNumber, tilMedicalCard, tilPassword, tilLicense, tilConfirmPassword;
    private TextInputEditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextMedicalCard, editTextPassword, editTextLicense, editTextConfirmPassword;
    private MaterialButton buttonCreateCaretaker;

    // Declare Firebase database references
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;
    private DatabaseReference validCaretakerRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_caretaker, container, false);

        // Initialize UI components
        initializeViews(view);
        // Set up Firebase references
        setupFirebase();
        // Set up the Create Caretaker button
        setupCreateCaretakerButton();
        // Set up the toolbar
        setupToolbar(view);

        return view;
    }

    /**
     * Initialize UI components by finding them from the view
     * @param view The root view of the fragment
     */
    private void initializeViews(View view) {
        tilFirstName = view.findViewById(R.id.tilFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        tilPhoneNumber = view.findViewById(R.id.tilPhoneNumber);
        tilMedicalCard = view.findViewById(R.id.tilMedicalCard);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilLicense = view.findViewById(R.id.tilLicense);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);

        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextPhoneNumber = view.findViewById(R.id.editTextPhoneNumber);
        editTextMedicalCard = view.findViewById(R.id.editTextMedicalCard);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextLicense = view.findViewById(R.id.editTextLicense);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);

        buttonCreateCaretaker = view.findViewById(R.id.buttonCreateCaretaker);
    }

    /**
     * Set up Firebase database references
     */
    private void setupFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");
        validCaretakerRef = firebaseDatabase.getReference("validCaretaker");
    }

    /**
     * Set up the Create Caretaker button with its click listener
     */
    private void setupCreateCaretakerButton() {
        buttonCreateCaretaker.setOnClickListener(v -> validateLicenseAndCreateCaretaker());
    }

    /**
     * Set up the toolbar and its navigation click listener
     * @param view The root view of the fragment
     */
    private void setupToolbar(View view) {
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Validate the caretaker's license and create a caretaker account if valid
     */
    private void validateLicenseAndCreateCaretaker() {
        String license = Objects.requireNonNull(editTextLicense.getText()).toString().trim();

        if (TextUtils.isEmpty(license)) {
            tilLicense.setError("Please enter your license");
            return;
        }

        validCaretakerRef.orderByChild("license").equalTo(license).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (validateInputs()) {
                        checkUniqueFields();
                    }
                } else {
                    tilLicense.setError("Invalid license");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error checking license");
            }
        });
    }

    /**
     * Validate the input fields for caretaker registration
     * @return True if all inputs are valid, false otherwise
     */
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

        // Validate license
        if (TextUtils.isEmpty(Objects.requireNonNull(editTextLicense.getText()).toString().trim())) {
            tilLicense.setError("License is required");
            isValid = false;
        } else {
            tilLicense.setError(null);
        }

        return isValid;
    }

    /**
     * Check if the phone number and medical card are unique, and create caretaker account if they are
     */
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
                                createCaretakerAccount();
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

    /**
     * Create a caretaker account in Firebase
     */
    private void createCaretakerAccount() {
        String firstName = Objects.requireNonNull(editTextFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(editTextLastName.getText()).toString().trim();
        String phoneNumber = Objects.requireNonNull(editTextPhoneNumber.getText()).toString().trim();
        String medicalCard = Objects.requireNonNull(editTextMedicalCard.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        String license = Objects.requireNonNull(editTextLicense.getText()).toString().trim();

        String caretakerId = usersRef.push().getKey();

        // Initialize patientIDs as an empty list
        List<String> patientIDs = new ArrayList<>();

        // Create a User object for the caretaker
        User caretaker = new User(caretakerId, firstName, lastName, phoneNumber, medicalCard, password, "caretaker", license, patientIDs);

        if (caretakerId != null) {
            Log.d("Firebase", "Creating caretaker account with ID: " + caretakerId);
            Log.d("Firebase", "Caretaker data: " + caretaker.toString());
            usersRef.child(caretakerId).setValue(caretaker)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Caretaker account created successfully");
                        saveLoginState("caretaker", firstName, lastName, caretakerId);
                        Toast.makeText(getActivity(), "Caretaker account created successfully", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity("caretaker", firstName, lastName, caretakerId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to create caretaker account", e);
                        showError("Failed to create caretaker account");
                    });
        }
    }

    /**
     * Save the login state in shared preferences
     * @param role The role of the user
     * @param firstName The first name of the user
     * @param lastName The last name of the user
     * @param userId The user ID
     */
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

    /**
     * Navigate to the main activity based on the user's role
     * @param role The role of the user
     * @param firstName The first name of the user
     * @param lastName The last name of the user
     * @param userId The user ID
     */
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

    /**
     * Show an error message as a Toast
     * @param message The error message to display
     */
    private void showError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Inner class representing a User
     */
    public static class User {
        public String id;
        public String firstName;
        public String lastName;
        public String phoneNumber;
        public String medicalCard;
        public String password;
        public String role;
        public String license;
        public List<String> patientIDs;

        public User() {
        }

        public User(String id, String firstName, String lastName, String phoneNumber, String medicalCard, String password, String role, String license, List<String> patientIDs) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.medicalCard = medicalCard;
            this.password = password;
            this.role = role;
            this.license = license;
            this.patientIDs = patientIDs;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id='" + id + '\'' +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", medicalCard='" + medicalCard + '\'' +
                    ", password='" + password + '\'' +
                    ", role='" + role + '\'' +
                    ", license='" + license + '\'' +
                    ", patientIDs=" + patientIDs +
                    '}';
        }
    }

}
