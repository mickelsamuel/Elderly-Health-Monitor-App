package com.example.elderly_health_monitor_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.Objects;

public class CaretakerFragment extends Fragment {

    private TextInputLayout tilFirstName, tilLastName, tilPhoneNumber, tilMedicalCard, tilPassword, tilLicense;
    private TextInputEditText editTextFirstName, editTextLastName, editTextPhoneNumber, editTextMedicalCard, editTextPassword, editTextLicense;
    private MaterialButton buttonCreateCaretaker;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersRef;
    private DatabaseReference validCaretakerRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_caretaker, container, false);

        initializeViews(view);
        setupFirebase();
        setupCreateCaretakerButton();
        setupToolbar(view);

        return view;
    }

    private void initializeViews(View view) {
        tilFirstName = view.findViewById(R.id.tilFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        tilPhoneNumber = view.findViewById(R.id.tilPhoneNumber);
        tilMedicalCard = view.findViewById(R.id.tilMedicalCard);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilLicense = view.findViewById(R.id.tilLicense);

        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextPhoneNumber = view.findViewById(R.id.editTextPhoneNumber);
        editTextMedicalCard = view.findViewById(R.id.editTextMedicalCard);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextLicense = view.findViewById(R.id.editTextLicense);

        buttonCreateCaretaker = view.findViewById(R.id.buttonCreateCaretaker);
    }

    private void setupFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");
        validCaretakerRef = firebaseDatabase.getReference("validCaretaker");
    }

    private void setupCreateCaretakerButton() {
        buttonCreateCaretaker.setOnClickListener(v -> validateLicenseAndCreateCaretaker());
    }

    private void setupToolbar(View view) {
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
    }

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
                        createCaretakerAccount();
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

        if (TextUtils.isEmpty(Objects.requireNonNull(editTextLicense.getText()).toString().trim())) {
            tilLicense.setError("License is required");
            isValid = false;
        } else {
            tilLicense.setError(null);
        }

        return isValid;
    }

    private void createCaretakerAccount() {
        String firstName = Objects.requireNonNull(editTextFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(editTextLastName.getText()).toString().trim();
        String phoneNumber = Objects.requireNonNull(editTextPhoneNumber.getText()).toString().trim();
        String medicalCard = Objects.requireNonNull(editTextMedicalCard.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        String license = Objects.requireNonNull(editTextLicense.getText()).toString().trim();

        String caretakerId = usersRef.push().getKey();

        User caretaker = new User(caretakerId, firstName, lastName, phoneNumber, medicalCard, password, "caretaker", license);

        if (caretakerId != null) {
            usersRef.child(caretakerId).setValue(caretaker)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getActivity(), "Caretaker account created successfully", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    })
                    .addOnFailureListener(e -> showError("Failed to create caretaker account"));
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
        public String license;

        public User() {
        }

        public User(String id, String firstName, String lastName, String phoneNumber, String medicalCard, String password, String role, String license) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.medicalCard = medicalCard;
            this.password = password;
            this.role = role;
            this.license = license;
        }
    }
}