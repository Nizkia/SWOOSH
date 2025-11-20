package com.example.swooshv2;

import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class RunnerRegister extends AppCompatActivity {

    private EditText registerName, registerMatric, registerPhone, registerEmail, registerAccNo, registerPassword, registerConfirmPassword;
    private Spinner spinnerFaculty, spinnerVehicleType, spinnerBank;
    private Button registerButton;
    private TextView loginRedirectText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "RunnerRegister";
    private static final String DEFAULT_LOCATION = "2.3126104773183456, 102.31836015746765";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        registerName = findViewById(R.id.register_name);
        registerMatric = findViewById(R.id.register_matric_no);
        registerPhone = findViewById(R.id.register_phone);
        registerEmail = findViewById(R.id.register_email);
        registerAccNo = findViewById(R.id.register_account_no);
        registerPassword = findViewById(R.id.register_password);
        registerConfirmPassword = findViewById(R.id.register_reenter_password);
        spinnerFaculty = findViewById(R.id.register_faculty);
        spinnerVehicleType = findViewById(R.id.register_vehicle_type);
        spinnerBank = findViewById(R.id.register_bank);
        registerButton = findViewById(R.id.register_button);
        loginRedirectText = findViewById(R.id.login_redirect);

        // Set spinner values
        setupSpinners();

        // Register button listener
        registerButton.setOnClickListener(v -> registerRunner());

        // Login redirect listener
        loginRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(RunnerRegister.this, RunnerLogin.class);
            startActivity(intent);
        });
    }

    private void setupSpinners() {
        // Faculty spinner
        ArrayAdapter<CharSequence> facultyAdapter = ArrayAdapter.createFromResource(
                this, R.array.faculty_choices, android.R.layout.simple_spinner_item);
        facultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFaculty.setAdapter(facultyAdapter);

        // Vehicle type spinner
        ArrayAdapter<CharSequence> vehicleAdapter = ArrayAdapter.createFromResource(
                this, R.array.vehicle_type_options, android.R.layout.simple_spinner_item);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(vehicleAdapter);

        // Bank spinner
        ArrayAdapter<CharSequence> bankAdapter = ArrayAdapter.createFromResource(
                this, R.array.bank_options, android.R.layout.simple_spinner_item);
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(bankAdapter);
    }

    private void registerRunner() {
        String name = registerName.getText().toString().trim();
        String matricNo = registerMatric.getText().toString().trim();
        String phone = registerPhone.getText().toString().trim();
        String email = registerEmail.getText().toString().trim();
        String accNo = registerAccNo.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String confirmPassword = registerConfirmPassword.getText().toString().trim();
        String faculty = spinnerFaculty.getSelectedItem().toString();
        String vehicleType = spinnerVehicleType.getSelectedItem().toString();
        String bank = spinnerBank.getSelectedItem().toString();

        // Input validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(matricNo) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(accNo) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate RunnerID and register runner
        db.collection("Runners").orderBy("RunnerID", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    String runnerID;
                    if (!querySnapshot.isEmpty()) {
                        String lastID = querySnapshot.getDocuments().get(0).getId();
                        int newID = Integer.parseInt(lastID.substring(1)) + 1;
                        runnerID = "R" + newID;
                    } else {
                        runnerID = "R200";
                    }

                    // Store data in Firestore
                    storeRunnerData(runnerID, name, matricNo, faculty, phone, email, vehicleType, accNo, bank, password);
                });
    }

    private void storeRunnerData(String runnerID, String name, String matricNo, String faculty, String phone, String email, String vehicleType, String accNo, String bank, String password) {
        Map<String, Object> runnerData = new HashMap<>();
        runnerData.put("RunnerID", runnerID);
        runnerData.put("RName", name);
        runnerData.put("RMatricNo", matricNo);
        runnerData.put("RFaculty", faculty);
        runnerData.put("RPhoneNo", phone);
        runnerData.put("REmail", email);
        runnerData.put("RVehicleType", vehicleType);
        runnerData.put("RAccNo", accNo);
        runnerData.put("RBank", bank);
        runnerData.put("RPassword", password);
        // Parse DEFAULT_LOCATION into GeoPoint
        String[] locationParts = DEFAULT_LOCATION.split(",");
        double latitude = Double.parseDouble(locationParts[0].trim());
        double longitude = Double.parseDouble(locationParts[1].trim());
        GeoPoint defaultGeoPoint = new GeoPoint(latitude, longitude);

        runnerData.put("RLocation", defaultGeoPoint);
        runnerData.put("RisAvailable", false);
        runnerData.put("CurrentTasks", 0);

        db.collection("Runners").document(runnerID).set(runnerData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Runner registered successfully! RunnerID: " + runnerID, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RunnerRegister.this, RunnerLogin.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to register runner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving runner data", e);
                });
    }
}

