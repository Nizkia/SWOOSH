package com.example.swooshv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class RunnerEarns extends AppCompatActivity {

    private static final String TAG = "RunnerEarns";
    private FirebaseFirestore db;
    private LinearLayout llEarningsContainer;
    private TextView tvTotalEarnings;

    private double totalEarnings = 0.0;
    private String runnerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_earns);

        Log.d(TAG, "onCreate: Initializing RunnerEarns activity.");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "onCreate: Firestore initialized.");

        // Initialize Views
        llEarningsContainer = findViewById(R.id.llEarningsContainer);
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings);
        Log.d(TAG, "onCreate: Views initialized.");

        // Retrieve Runner ID
        runnerID = getIntent().getStringExtra("RunnerID");
        if (runnerID == null || runnerID.isEmpty()) {
            Log.e(TAG, "onCreate: Runner ID is null or empty.");
        } else {
            Log.d(TAG, "onCreate: Runner ID retrieved: " + runnerID);
        }

        // Fetch earnings data
        fetchEarnings();
    }

    private void fetchEarnings() {
        Log.d(TAG, "fetchEarnings: Fetching earnings for RunnerID: " + runnerID);

        if (runnerID == null || runnerID.isEmpty()) {
            Toast.makeText(this, "Runner ID is not available.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "fetchEarnings: Runner ID is not available.");
            return;
        }

        db.collection("Pickups")
                .whereEqualTo("RunnerID", runnerID)
                .whereEqualTo("Status", "Delivered")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "fetchEarnings: Successfully fetched pickups.");
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "fetchEarnings: Pickups found: " + querySnapshot.size());
                        int[] processedCount = {0}; // Counter to track completed pickups

                        for (DocumentSnapshot document : querySnapshot) {
                            String paymentID = document.getString("PaymentID"); // Adjust field name if necessary
                            String pickupID = document.getString("PickupID");

                            if (paymentID != null) {
                                fetchPaymentDetails(paymentID, pickupID);
                            } else {
                                Log.w(TAG, "fetchEarnings: No PaymentID found for PickupID: " + pickupID);
                            }

                            // Increment processed count after each pickup is handled
                            processedCount[0]++;
                            if (processedCount[0] == querySnapshot.size()) {
                                calculateTotalEarnings(); // Call the total earnings calculation
                            }
                        }
                    } else {
                        Log.d(TAG, "fetchEarnings: No completed pickups found.");
                        Toast.makeText(this, "No completed pickups found.", Toast.LENGTH_SHORT).show();
                        displayMessage("No completed pickups found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchEarnings: Error fetching completed pickups", e);
                    Toast.makeText(this, "Failed to load pickups.", Toast.LENGTH_SHORT).show();
                    displayMessage("Failed to load pickups.");
                });
    }

    private void calculateTotalEarnings() {
        Log.d(TAG, "calculateTotalEarnings: Calculating total earnings.");

        // Display the total earnings on the TextView
        tvTotalEarnings.setText(String.format(Locale.getDefault(),
                "Total Earnings: MYR %.2f", totalEarnings));

        Log.d(TAG, "calculateTotalEarnings: Total earnings displayed: MYR " + totalEarnings);
    }



    private void fetchPaymentDetails(String paymentID, String pickupID) {
        Log.d(TAG, "fetchPaymentDetails: Fetching payment details for PaymentID: " + paymentID);

        db.collection("Payments")
                .document(paymentID) // Assuming PaymentID is the document ID
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Object amountObject = document.get("Amount");
                        double amount = 0.0;

                        // Safely handle the 'Amount' field
                        if (amountObject instanceof Number) {
                            amount = ((Number) amountObject).doubleValue();
                        } else if (amountObject instanceof String) {
                            try {
                                amount = Double.parseDouble((String) amountObject);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "fetchPaymentDetails: Amount field is not a valid number string", e);
                            }
                        }

                        Log.d(TAG, "fetchPaymentDetails: Amount: " + amount);

                        // Calculate runner's commission (30%)
                        double commission = amount * 0.3;
                        totalEarnings += commission;

                        Log.d(TAG, "fetchPaymentDetails: Commission: " + commission);
                        Log.d(TAG, "fetchPaymentDetails: Total earnings updated to: " + totalEarnings);

                        // Display pickup earnings
                        displayPickupEarnings(pickupID, amount, commission);

                        // Update total earnings display
                        tvTotalEarnings.setText(String.format(Locale.getDefault(),
                                "Total Earnings: MYR %.2f", totalEarnings));
                        Log.d(TAG, "fetchPaymentDetails: Updated total earnings on UI.");
                    } else {
                        Log.d(TAG, "fetchPaymentDetails: No document found for PaymentID: " + paymentID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchPaymentDetails: Error fetching payment details for PaymentID: " + paymentID, e);
                    Toast.makeText(this, "Failed to load payment details.", Toast.LENGTH_SHORT).show();
                });
    }


    private void displayPickupEarnings(String pickupID, double amount, double commission) {
        Log.d(TAG, "displayPickupEarnings: Displaying earnings for PickupID: " + pickupID);

        TextView tvPickupEarnings = new TextView(this);
        tvPickupEarnings.setText(String.format(Locale.getDefault(),
                "Pickup ID: %s\nTotal Amount: MYR %.2f\nYour Earnings: MYR %.2f\n\n",
                pickupID, amount, commission));
        tvPickupEarnings.setTextSize(16f);
        tvPickupEarnings.setPadding(8, 8, 8, 8);

        // Add the TextView to the LinearLayout
        llEarningsContainer.addView(tvPickupEarnings);
        Log.d(TAG, "displayPickupEarnings: Added earnings details to layout.");
    }

    private void displayMessage(String message) {
        Log.d(TAG, "displayMessage: Displaying message: " + message);

        TextView tvMessage = new TextView(this);
        tvMessage.setText(message);
        tvMessage.setTextSize(16f);
        tvMessage.setPadding(8, 8, 8, 8);

        llEarningsContainer.addView(tvMessage);
        Log.d(TAG, "displayMessage: Message added to layout.");
    }
}
