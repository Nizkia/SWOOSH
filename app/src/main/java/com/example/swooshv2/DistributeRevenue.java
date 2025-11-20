package com.example.swooshv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DistributeRevenue extends AppCompatActivity {

    private static final String TAG = "DistributeRevenue";
    private RecyclerView recyclerView;
    private RevenueAdapter adapter;
    private List<Revenue> revenueList;
    private TextView totalRevenue, adminShare, runnersShare;
    private Button calculateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distribute_revenue);

        Log.d(TAG, "onCreate: Initializing DistributeRevenue activity.");

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerViewDistributeRevenue);
        totalRevenue = findViewById(R.id.tvTotalRevenue);
        adminShare = findViewById(R.id.tvAdminShare);
        runnersShare = findViewById(R.id.tvRunnersShare);
        calculateButton = findViewById(R.id.btnCalculateShares);

        Log.d(TAG, "onCreate: RecyclerView and UI components initialized.");

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        revenueList = new ArrayList<>();
        adapter = new RevenueAdapter(revenueList);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "onCreate: RecyclerView setup complete.");

        // Load data
        loadRevenueData();

        // Set up calculate button logic
        calculateButton.setOnClickListener(v -> calculateShares());
    }

    private void loadRevenueData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "loadRevenueData: Fetching data from Firestore.");

        // Clear the list before loading new data
        revenueList.clear();

        db.collection("Pickups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "loadRevenueData: Successfully fetched pickups.");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pickupID = document.getString("PickupID");
                            String runnerID = document.getString("RunnerID");
                            String userEmail = document.getString("UEmail");
                            String paymentID = document.getString("PaymentID");

                            if (paymentID != null) {
                                fetchPaymentDetails(paymentID, pickupID, runnerID, userEmail);
                            } else {
                                Log.w(TAG, "loadRevenueData: Missing PaymentID for PickupID: " + pickupID);
                                revenueList.add(new Revenue("Unknown Date", 0.0, pickupID, runnerID, userEmail));
                            }
                        }
                    } else {
                        Log.e(TAG, "loadRevenueData: Error fetching data.", task.getException());
                        Toast.makeText(this, "Error loading data: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchPaymentDetails(String paymentID, String pickupID, String runnerID, String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "fetchPaymentDetails: Fetching payment details for PaymentID: " + paymentID);

        db.collection("Payments")
                .document(paymentID) // Assuming PaymentID is the document ID
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        double amount = 0.0;
                        Object amountObject = document.get("Amount");
                        if (amountObject instanceof Number) {
                            amount = ((Number) amountObject).doubleValue();
                        } else if (amountObject instanceof String) {
                            try {
                                amount = Double.parseDouble((String) amountObject);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "fetchPaymentDetails: Error parsing amount for PaymentID: " + paymentID, e);
                            }
                        }

                        String date = convertTimestampToDate(document.get("Date"));

                        Log.d(TAG, String.format("fetchPaymentDetails: Retrieved data - PickupID: %s, Amount: %.2f, Date: %s",
                                pickupID, amount, date));

                        // Add revenue data to the list
                        revenueList.add(new Revenue(date, amount, pickupID, runnerID, userEmail));

                        // Notify the adapter about data changes
                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            calculateTotalRevenue();
                        });
                    } else {
                        Log.w(TAG, "fetchPaymentDetails: No document found for PaymentID: " + paymentID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchPaymentDetails: Error fetching payment details for PaymentID: " + paymentID, e);
                });
    }

    private void calculateShares() {
        double total = 0;
        for (Revenue revenue : revenueList) {
            total += revenue.getAmount();
        }

        Log.d(TAG, "calculateShares: Calculating shares based on total revenue: RM" + total);

        // Assume admin takes 70% and runners get 30% as commission
        double adminPercentage = 70.0;
        double runnersPercentage = 30.0;

        double adminAmount = (total * adminPercentage) / 100;
        double runnersAmount = (total * runnersPercentage) / 100;

        adminShare.setText("Admin Share: RM" + String.format("%.2f", adminAmount));
        runnersShare.setText("Runners Share: RM" + String.format("%.2f", runnersAmount));

        Log.d(TAG, "calculateShares: Admin Share: RM" + adminAmount + ", Runners Share: RM" + runnersAmount);
        Toast.makeText(this, "Shares calculated successfully!", Toast.LENGTH_SHORT).show();
    }

    private String convertTimestampToDate(Object timestamp) {
        if (timestamp == null) {
            Log.w(TAG, "convertTimestampToDate: Timestamp is null.");
            return "Unknown Date";
        }

        try {
            if (timestamp instanceof com.google.firebase.Timestamp) {
                Date date = ((com.google.firebase.Timestamp) timestamp).toDate();
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                Log.d(TAG, "convertTimestampToDate: Converted Firestore Timestamp to date: " + formattedDate);
                return formattedDate;
            } else if (timestamp instanceof Number) {
                Date date = new Date(((Number) timestamp).longValue());
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                Log.d(TAG, "convertTimestampToDate: Converted numeric timestamp to date: " + formattedDate);
                return formattedDate;
            } else {
                Log.w(TAG, "convertTimestampToDate: Unsupported timestamp type: " + timestamp.getClass().getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "convertTimestampToDate: Error converting timestamp to date.", e);
        }
        return "Invalid Date";
    }

    private void calculateTotalRevenue() {
        double total = 0;
        for (Revenue revenue : revenueList) {
            total += revenue.getAmount();
        }
        totalRevenue.setText("Total Revenue: RM" + String.format("%.2f", total));
        Log.d(TAG, "calculateTotalRevenue: Total revenue calculated: RM" + total);
    }
}
