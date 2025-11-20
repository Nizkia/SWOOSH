package com.example.swooshv2;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RunnerProfile extends AppCompatActivity {

    private TextView tvRName, tvREmail, tvRunnerID;
    private EditText etRPhoneNo, etRVehicleNo, etRVehicleType;
    private Button btnSaveChanges, btnDeactivateAccount;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String runnerID; // RunnerID passed from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_profile);

        // Initialize UI elements
        tvRName = findViewById(R.id.tvRName);
        tvREmail = findViewById(R.id.tvREmail);
        tvRunnerID = findViewById(R.id.tvRunnerID);
        etRPhoneNo = findViewById(R.id.etRPhoneNo);
        etRVehicleNo = findViewById(R.id.etRVehicleNo);
        etRVehicleType = findViewById(R.id.etRVehicleType);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnDeactivateAccount = findViewById(R.id.btnDeactivateAccount);

        // Get the RunnerID passed via intent
        runnerID = getIntent().getStringExtra("RunnerID");

        if (runnerID == null || runnerID.isEmpty()) {
            Toast.makeText(this, "Runner ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load Runner details
        loadRunnerDetails();

        // Save Changes
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        // Deactivate Account
        btnDeactivateAccount.setOnClickListener(v -> deactivateAccount());
    }

    private void loadRunnerDetails() {
        // Query Firestore where RunnerID equals the passed runnerID
        db.collection("Runners")
                .whereEqualTo("RunnerID", runnerID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Retrieve the first document that matches the query
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            // Populate the fields with data from Firestore
                            tvRName.setText(document.getString("RName"));
                            tvREmail.setText(document.getString("REmail"));
                            tvRunnerID.setText(document.getString("RunnerID")); // RunnerID
                            etRPhoneNo.setText(document.getString("RPhoneNo"));
                            etRVehicleNo.setText(document.getString("RVehicleNo"));
                            etRVehicleType.setText(document.getString("RVehicleType"));
                        } else {
                            Toast.makeText(this, "Runner not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveChanges() {
        String phoneNo = etRPhoneNo.getText().toString().trim();
        String vehicleNo = etRVehicleNo.getText().toString().trim();
        String vehicleType = etRVehicleType.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNo) || TextUtils.isEmpty(vehicleNo) || TextUtils.isEmpty(vehicleType)) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query Firestore to update the document where RunnerID matches
        db.collection("Runners")
                .whereEqualTo("RunnerID", runnerID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String documentId = task.getResult().getDocuments().get(0).getId();

                        db.collection("Runners").document(documentId)
                                .update("RPhoneNo", phoneNo, "RVehicleNo", vehicleNo, "RVehicleType", vehicleType)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Error: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Runner not found for updating", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deactivateAccount() {
        // Query Firestore to find the document and delete it
        db.collection("Runners")
                .whereEqualTo("RunnerID", runnerID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String documentId = task.getResult().getDocuments().get(0).getId();

                        db.collection("Runners").document(documentId)
                                .delete()
                                .addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        Toast.makeText(this, "Account deactivated", Toast.LENGTH_SHORT).show();
                                        finish(); // Close the activity
                                    } else {
                                        Toast.makeText(this, "Error: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Runner not found for deactivation", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
