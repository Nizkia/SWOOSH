package com.example.swooshv2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerRegister extends AppCompatActivity {

    private EditText etUName, etUEmail, etUPassw, etReAPassw, etUPhone, etUMatricNo, etDormAddress;
    private Spinner spFaculty, spUDormitory, spUDormBlock;
    private Button btnRegisterNow;
    private TextView tvBackToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final Map<String, GeoPoint> dormLocMap = new HashMap<>();
    private final Map<String, GeoPoint> guardhouseMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etUName = findViewById(R.id.etUName);
        etUEmail = findViewById(R.id.etAEmail);
        etUPassw = findViewById(R.id.etAPassw);
        etReAPassw = findViewById(R.id.etReAPassw);
        etUPhone = findViewById(R.id.etUPhone);
        etUMatricNo = findViewById(R.id.etUMatricNo);
        etDormAddress = findViewById(R.id.etDormAddress);
        spFaculty = findViewById(R.id.spFaculty);
        spUDormitory = findViewById(R.id.spUDormitory);
        spUDormBlock = findViewById(R.id.spUDormBlock);
        btnRegisterNow = findViewById(R.id.btnRegisterNow);
        tvBackToLogin = findViewById(R.id.tvBacktoLogin);

        setupDormitorySpinner();
        initializeGeoPoints();

        btnRegisterNow.setOnClickListener(v -> registerUser());

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerRegister.this, CustomerLogin.class);
            startActivity(intent);
        });
    }

    private void setupDormitorySpinner() {
        List<String> dormitoryOptions = new ArrayList<>();
        dormitoryOptions.add("Satria");
        dormitoryOptions.add("Lestari");
        dormitoryOptions.add("Al-Jazari");

        ArrayAdapter<String> dormitoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dormitoryOptions);
        dormitoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUDormitory.setAdapter(dormitoryAdapter);

        spUDormitory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDormitory = parent.getItemAtPosition(position).toString();
                updateDormBlockOptions(selectedDormitory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(CustomerRegister.this, "Please select a Dormitory Area", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDormBlockOptions(String dormitory) {
        List<String> dormBlockOptions = new ArrayList<>();

        switch (dormitory) {
            case "Satria":
                dormBlockOptions.add("Lekiu");
                dormBlockOptions.add("Lekir");
                dormBlockOptions.add("Kasturi");
                dormBlockOptions.add("Tuah");
                dormBlockOptions.add("Jebat");
                break;

            case "Lestari":
                dormBlockOptions.add("A1");
                dormBlockOptions.add("A2");
                dormBlockOptions.add("A3");
                dormBlockOptions.add("A4");
                dormBlockOptions.add("B1");
                dormBlockOptions.add("B2");
                break;

            case "Al-Jazari":
                dormBlockOptions.add("1");
                dormBlockOptions.add("2");
                dormBlockOptions.add("3");
                break;

            default:
                Toast.makeText(this, "Invalid dormitory selected", Toast.LENGTH_SHORT).show();
                break;
        }

        ArrayAdapter<String> dormBlockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dormBlockOptions);
        dormBlockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUDormBlock.setAdapter(dormBlockAdapter);
    }

    private void registerUser() {
        String name = etUName.getText().toString().trim();
        String email = etUEmail.getText().toString().trim();
        String password = etUPassw.getText().toString().trim();
        String rePassword = etReAPassw.getText().toString().trim();
        String phone = etUPhone.getText().toString().trim();
        String matricNo = etUMatricNo.getText().toString().trim();
        String dormAddress = etDormAddress.getText().toString().trim();
        String faculty = spFaculty.getSelectedItem().toString();
        String dormitory = spUDormitory.getSelectedItem().toString();
        String dormBlock = spUDormBlock.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(rePassword) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(matricNo) ||
                TextUtils.isEmpty(dormAddress) || TextUtils.isEmpty(faculty) || TextUtils.isEmpty(dormitory) ||
                TextUtils.isEmpty(dormBlock)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 8 characters long and include letters and numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint dormLoc = dormLocMap.get(dormBlock);
        GeoPoint guardhouseLoc = guardhouseMap.get(dormitory);

        if (dormLoc == null || guardhouseLoc == null) {
            Toast.makeText(this, "Invalid dormitory or block selection", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userEmail = auth.getCurrentUser().getEmail();

                if (userEmail != null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("UName", name);
                    user.put("UEmail", userEmail);
                    user.put("UPhoneNo", phone);
                    user.put("UMatricNo", matricNo);
                    user.put("UDormAddress", dormAddress);
                    user.put("UFaculty", faculty);
                    user.put("UDormitory", dormitory);
                    user.put("UDormBlock", dormBlock);
                    user.put("UDormLoc", dormLoc);
                    user.put("UGuardhouseLoc", guardhouseLoc);
                    user.put("UPassword", password);

                    db.collection("Users").document(userEmail).set(user).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(CustomerRegister.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                            clearFields();
                            Intent intent = new Intent(CustomerRegister.this, CustomerLogin.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CustomerRegister.this, "Failed to save user details: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(CustomerRegister.this, "User email is null!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CustomerRegister.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*");
    }

    private void initializeGeoPoints() {
        dormLocMap.put("Lekiu", new GeoPoint(2.3110172891377476, 102.314045284621));
        dormLocMap.put("Lekir", new GeoPoint(2.3107677050831485, 102.31413984264651));
        dormLocMap.put("Kasturi", new GeoPoint(2.310829345721184, 102.31454753840309));
        dormLocMap.put("Tuah", new GeoPoint(2.308764746692125, 102.31484742512374));
        dormLocMap.put("Jebat", new GeoPoint(2.308730942256423, 102.31452602209411));

        dormLocMap.put("A1", new GeoPoint(2.314607208959905, 102.31589259722165));
        dormLocMap.put("A2", new GeoPoint(2.314236994362461, 102.31611039839164));
        dormLocMap.put("A3", new GeoPoint(2.3144725360338745, 102.31613142303398));
        dormLocMap.put("A4", new GeoPoint(2.3146850545686086, 102.31611039839164));
        dormLocMap.put("B1", new GeoPoint(2.315348190148213, 102.316804106777));
        dormLocMap.put("B2", new GeoPoint(2.315324739979753, 102.31700594300541));

        dormLocMap.put("1", new GeoPoint(2.320981135254736, 102.32755252870851));
        dormLocMap.put("2", new GeoPoint(2.321101919707713, 102.32777666710497));
        dormLocMap.put("3", new GeoPoint(2.321132115819345, 102.3281342811757));

        guardhouseMap.put("Satria", new GeoPoint(2.3097575795914516, 102.31544188879347));
        guardhouseMap.put("Lestari", new GeoPoint(2.314584103123155, 102.31661351058278));
        guardhouseMap.put("Al-Jazari", new GeoPoint(2.3214742595905347, 102.32867687471179));
    }

    private void clearFields() {
        etUName.setText("");
        etUEmail.setText("");
        etUPassw.setText("");
        etReAPassw.setText("");
        etUPhone.setText("");
        etUMatricNo.setText("");
        etDormAddress.setText("");
        spFaculty.setSelection(0);
        spUDormitory.setSelection(0);
        spUDormBlock.setSelection(0);
    }
}
