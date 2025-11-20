package com.example.swooshv2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalculateRevenue extends AppCompatActivity {

    private static final String TAG = "CalculateRevenue";
    private RecyclerView recyclerView;
    private RevenueAdapter adapter;
    private List<Revenue> revenueList;
    private TextView totalRevenue;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_revenue);

        Log.d(TAG, "onCreate: Initializing CalculateRevenue activity.");

        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerViewRevenue);
        totalRevenue = findViewById(R.id.tvTotalRevenue);
        filterSpinner = findViewById(R.id.spinnerFilter);

        Log.d(TAG, "onCreate: RecyclerView, TextView, and Spinner initialized.");

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        revenueList = new ArrayList<>();
        adapter = new RevenueAdapter(revenueList);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "onCreate: RecyclerView and adapter setup complete.");

        // Set up Spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        Log.d(TAG, "onCreate: Spinner adapter setup complete.");

        // Load data from Firestore
        loadRevenueData();

        // Setup filter logic
        setupFilterSpinner();
    }

    private void loadRevenueData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "loadRevenueData: Fetching data from Firestore.");

        db.collection("Pickups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "loadRevenueData: Data fetch successful.");
                        revenueList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pickupID = document.getString("PickupID");
                            String runnerID = document.getString("RunnerID");
                            String userEmail = document.getString("UEmail");
                            String paymentID = document.getString("PaymentID"); // Retrieve PaymentID from Pickups

                            if (paymentID != null) {
                                fetchPaymentDetails(paymentID, pickupID, runnerID, userEmail);
                            } else {
                                Log.w(TAG, "loadRevenueData: Missing PaymentID for PickupID: " + pickupID);
                                revenueList.add(new Revenue("Unknown Date", 0.0, pickupID, runnerID, userEmail));
                            }
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } else {
                        Log.e(TAG, "loadRevenueData: Error fetching data.", task.getException());
                        Toast.makeText(this, "Error fetching data: " + task.getException(), Toast.LENGTH_SHORT).show();
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

                        // Add the retrieved data to the revenue list
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



    private void setupFilterSpinner() {
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filterOption = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "setupFilterSpinner: Selected filter option: " + filterOption);

                if (filterOption.equals("Weekly")) {
                    filterByWeekly();
                } else if (filterOption.equals("Monthly")) {
                    filterByMonthly();
                } else {
                    showAllData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "setupFilterSpinner: No filter option selected.");
            }
        });
    }

    private void showAllData() {
        Log.d(TAG, "showAllData: Displaying all data.");
        adapter.updateData(revenueList);
        calculateFilteredTotal(revenueList);
    }

    private void filterByWeekly() {
        Log.d(TAG, "filterByWeekly: Filtering data for the current week.");
        List<Revenue> filteredList = filterByTime(Calendar.WEEK_OF_YEAR);
        adapter.updateData(filteredList);
        calculateFilteredTotal(filteredList);
    }

    private void filterByMonthly() {
        Log.d(TAG, "filterByMonthly: Filtering data for the current month.");
        List<Revenue> filteredList = filterByTime(Calendar.MONTH);
        adapter.updateData(filteredList);
        calculateFilteredTotal(filteredList);
    }

    private List<Revenue> filterByTime(int timeField) {
        List<Revenue> filteredList = new ArrayList<>();
        Calendar currentCalendar = Calendar.getInstance();

        for (Revenue revenue : revenueList) {
            try {
                if ("Unknown Date".equals(revenue.getDate()) || "Invalid Date".equals(revenue.getDate())) {
                    Log.w(TAG, "filterByTime: Skipping invalid date for revenue: " + revenue);
                    continue;
                }

                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(revenue.getDate());
                Calendar revenueCalendar = Calendar.getInstance();
                revenueCalendar.setTime(date);

                if (currentCalendar.get(Calendar.YEAR) == revenueCalendar.get(Calendar.YEAR) &&
                        currentCalendar.get(timeField) == revenueCalendar.get(timeField)) {
                    filteredList.add(revenue);
                }
            } catch (ParseException e) {
                Log.e(TAG, "filterByTime: Error parsing date for revenue: " + revenue, e);
            }
        }

        Log.d(TAG, "filterByTime: Filtered data count: " + filteredList.size());
        return filteredList;
    }


    private void calculateFilteredTotal(List<Revenue> filteredList) {
        Log.d(TAG, "calculateFilteredTotal: Calculating total for filtered data.");
        double total = 0.0;
        for (Revenue revenue : filteredList) {
            total += revenue.getAmount();
        }
        totalRevenue.setText(String.format("Total Revenue: RM%.2f", total));
        Log.d(TAG, "calculateFilteredTotal: Total revenue calculated: RM" + total);
    }

    private String convertTimestampToDate(Object timestamp) {
        if (timestamp == null) {
            Log.w(TAG, "convertTimestampToDate: Timestamp is null.");
            return "Unknown Date";
        }

        try {
            if (timestamp instanceof Timestamp) {
                // Firestore's Timestamp object
                Date date = ((Timestamp) timestamp).toDate();
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                Log.d(TAG, "convertTimestampToDate: Converted Firestore Timestamp to date: " + formattedDate);
                return formattedDate;
            } else if (timestamp instanceof Number) {
                // Fallback for Long or similar types
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
        Log.d(TAG, "calculateTotalRevenue: Calculating total revenue for all data.");
        double total = 0.0;
        for (Revenue revenue : revenueList) {
            total += revenue.getAmount();
        }
        totalRevenue.setText(String.format("Total Revenue: RM%.2f", total));
        Log.d(TAG, "calculateTotalRevenue: Total revenue calculated: RM" + total);
    }
}
