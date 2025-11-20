package com.example.swooshv2;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class RunnerSetAv extends AppCompatActivity {

    private Switch switchAvailability;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String runnerID; // RunnerID passed from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_set_av);

        // Initialize UI elements
        switchAvailability = findViewById(R.id.switchAvailability);

        // Get the RunnerID passed via intent
        runnerID = getIntent().getStringExtra("RunnerID");

        if (runnerID == null || runnerID.isEmpty()) {
            Toast.makeText(this, "Runner ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load current availability status
        loadAvailabilityStatus();

        // Set listener for switch toggle
        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateAvailability(isChecked);
        });
    }

    private void loadAvailabilityStatus() {
        db.collection("Runners")
                .whereEqualTo("RunnerID", runnerID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0); // Correct type
                        Boolean isAvailable = document.getBoolean("RisAvailable");
                        if (isAvailable != null) {
                            switchAvailability.setChecked(isAvailable); // Set the switch state
                        }
                    } else {
                        Toast.makeText(this, "Failed to load availability status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAvailability(boolean isAvailable) {
        db.collection("Runners")
                .whereEqualTo("RunnerID", runnerID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String documentId = task.getResult().getDocuments().get(0).getId();

                        db.collection("Runners").document(documentId)
                                .update("RisAvailable", isAvailable)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        String message = isAvailable
                                                ? "You are now available to receive tasks."
                                                : "You are now unavailable.";
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to update availability", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Runner not found for updating availability", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
