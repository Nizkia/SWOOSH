package com.example.swooshv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RunnerComplete extends AppCompatActivity {

    private static final String TAG = "RunnerComplete";
    private LinearLayout llCompletedPickups;
    private FirebaseFirestore db;
    private String runnerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_complete);

        Log.d(TAG, "onCreate: Initializing RunnerComplete activity.");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "onCreate: Firestore initialized.");

        // Initialize Layout
        llCompletedPickups = findViewById(R.id.llCompletedPickups);
        Log.d(TAG, "onCreate: Layout initialized.");

        // Retrieve Runner ID
        runnerID = getIntent().getStringExtra("RunnerID");
        if (runnerID == null || runnerID.isEmpty()) {
            Log.e(TAG, "onCreate: Runner ID is null or empty.");
        } else {
            Log.d(TAG, "onCreate: Runner ID retrieved: " + runnerID);
        }

        // Fetch completed pickups
        fetchCompletedPickups();
    }

    private void fetchCompletedPickups() {
        Log.d(TAG, "fetchCompletedPickups: Fetching completed pickups for RunnerID: " + runnerID);

        if (runnerID == null || runnerID.isEmpty()) {
            Toast.makeText(this, "Runner ID is not available.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "fetchCompletedPickups: Runner ID is not available.");
            return;
        }

        db.collection("Pickups")
                .whereEqualTo("RunnerID", runnerID)
                .whereEqualTo("Status", "Delivered")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "fetchCompletedPickups: Successfully fetched pickups.");
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "fetchCompletedPickups: Pickups found: " + querySnapshot.size());
                        for (DocumentSnapshot document : querySnapshot) {
                            displayPickupDetails(document);
                        }
                    } else {
                        Log.d(TAG, "fetchCompletedPickups: No completed pickups found.");
                        Toast.makeText(this, "No completed pickups found.", Toast.LENGTH_SHORT).show();
                        displayMessage("No completed pickups found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchCompletedPickups: Error fetching completed pickups", e);
                    Toast.makeText(this, "Failed to load completed pickups.", Toast.LENGTH_SHORT).show();
                    displayMessage("Failed to load completed pickups.");
                });
    }

    private void displayPickupDetails(DocumentSnapshot document) {
        Log.d(TAG, "displayPickupDetails: Displaying pickup details for document ID: " + document.getId());

        // Extract pickup details
        String pickupID = document.getString("PickupID");
        String userEmail = document.getString("UEmail");
        Object timePickupCompletedObj = document.get("TimePickupCompleted");

        Log.d(TAG, "displayPickupDetails: PickupID: " + pickupID + ", UserEmail: " + userEmail);

        // Format the timestamp if available
        String timePickupCompleted = timePickupCompletedObj != null
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(((com.google.firebase.Timestamp) timePickupCompletedObj).toDate())
                : "N/A";

        Log.d(TAG, "displayPickupDetails: TimePickupCompleted: " + timePickupCompleted);

        // Create a TextView for each pickup
        TextView tvPickupDetails = new TextView(this);
        tvPickupDetails.setText(String.format(
                "Pickup ID: %s\nUser Email: %s\nCompleted: %s\n\n",
                pickupID != null ? pickupID : "N/A",
                userEmail != null ? userEmail : "N/A",
                timePickupCompleted
        ));
        tvPickupDetails.setTextSize(16f);
        tvPickupDetails.setPadding(8, 8, 8, 8);

        // Add the TextView to the LinearLayout
        llCompletedPickups.addView(tvPickupDetails);
        Log.d(TAG, "displayPickupDetails: Pickup details added to layout.");
    }

    private void displayMessage(String message) {
        Log.d(TAG, "displayMessage: Displaying message: " + message);

        // Create a TextView for the message
        TextView tvMessage = new TextView(this);
        tvMessage.setText(message);
        tvMessage.setTextSize(16f);
        tvMessage.setPadding(8, 8, 8, 8);

        // Add the TextView to the LinearLayout
        llCompletedPickups.addView(tvMessage);
        Log.d(TAG, "displayMessage: Message added to layout.");
    }
}
