package com.example.swooshv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextView tvParcelInfo, tvCurrentStatus, tvCustomerName, tvNoOfItems, tvPaymentInfo;
    private Button btnUpdateStatus, btnBack;
    private String runnerID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String pickupID;
    private String currentStatus;
    private int noOfItems;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        // Initialize UI elements
        tvParcelInfo = findViewById(R.id.tvParcelInfo);
        tvPaymentInfo = findViewById(R.id.tvPaymentInfo);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvNoOfItems = findViewById(R.id.tvNoOfItems);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        btnBack = findViewById(R.id.btnBack);

        // Get data from Intent
        Intent intent = getIntent();
        runnerID = intent.getStringExtra("RunnerID");
        pickupID = intent.getStringExtra("PickupID");

        // Validate if PickupID is passed
        if (pickupID == null || pickupID.isEmpty()) {
            Log.e("TaskDetailsActivity", "PickupID is missing!");
            Toast.makeText(this, "No task selected.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load all details based on PickupID
        loadTaskDetails(pickupID);

        // Back button logic
        btnBack.setOnClickListener(v -> finish());

        btnUpdateStatus.setOnClickListener(v -> {
            if (pickupID == null || pickupID.isEmpty()) {
                Toast.makeText(this, "Pickup ID is missing!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine the next status based on the current status
            String nextStatus = getNextStatus(currentStatus);

            if ("Delivered".equals(nextStatus)) {
                // If the next status is "Delivered", confirm the delivery with a dialog
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Delivery")
                        .setMessage("Has the item(s) been successfully delivered?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Add TimePickupCompleted field and update status
                            proceedStatusUpdateWithTimestamp(nextStatus);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
            else {
                // Directly update the status
                proceedStatusUpdate(nextStatus);
            }
        });

    }

    private void proceedStatusUpdateWithTimestamp(String nextStatus) {
        // Get the current timestamp
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("Status", nextStatus);
        updateData.put("TimePickupCompleted", com.google.firebase.Timestamp.now()); // Add current timestamp

        // Update the database
        db.collection("Pickups").document(pickupID)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    currentStatus = nextStatus;
                    tvCurrentStatus.setText("Current Status: " + currentStatus);
                    Toast.makeText(this, "Status updated to: " + currentStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status.", Toast.LENGTH_SHORT).show();
                    Log.e("TaskDetailsActivity", "Error updating status", e);
                });
    }


    private void loadTaskDetails(String pickupID) {
        db.collection("Pickups").document(pickupID)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Extract basic task details
                        noOfItems = document.getLong("NoOfItems").intValue();
                        currentStatus = document.getString("Status");

                        // Populate initial details on the UI
                        tvNoOfItems.setText("No. of Items: " + noOfItems);
                        tvCurrentStatus.setText("Current Status: " + currentStatus);
                        if ("Delivered".equals(currentStatus)) {
                            btnUpdateStatus.setEnabled(false);
                            btnUpdateStatus.setText("No Further Updates");
                        }

                        // Determine the type of pickup and fetch relevant details
                        String pickupType = pickupID.substring(2, 3);  // Extract only the 3rd character
                        if ("P".equals(pickupType)) {
                            fetchParcelDetails(pickupID);
                        } else if ("F".equals(pickupType)) {
                            fetchFoodDetails(pickupID);
                        } else {
                            Toast.makeText(this, "Invalid pickup type.", Toast.LENGTH_SHORT).show();
                            Log.e("TaskDetailsActivity", "Unknown pickup type for ID: " + pickupID);
                        }

                        // Fetch related details
                        fetchCustomerDetails(document.getString("UEmail"));
                        fetchPaymentDetails(document.getString("PaymentID"));
                    } else {
                        Toast.makeText(this, "Task not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load task details.", Toast.LENGTH_SHORT).show();
                    Log.e("TaskDetailsActivity", "Error loading task details: ", e);
                });
    }


    private void fetchCustomerDetails(String email) {
        if (email == null || email.isEmpty()) {
            tvCustomerName.setText("Customer details not found.");
            return;
        }

        db.collection("Users").document(email)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String customerName = document.getString("UName");
                        String dormAddress = document.getString("UDormAddress");
                        String phoneNo = document.getString("UPhoneNo");

                        // Populate customer details
                        tvCustomerName.setText("Name: " + customerName + "\nDorm: " + dormAddress + "\nPhone: " + phoneNo);
                    } else {
                        tvCustomerName.setText("Customer details not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("fetchCustomerDetails", "Error fetching customer details: ", e);
                    tvCustomerName.setText("Failed to load customer details.");
                });
    }

    private void fetchParcelDetails(String pickupID) {
        // Step 1: Query Parcel_Pickups for associated ParcelNos
        db.collection("Pickups").document(pickupID).collection("Parcel_Pickups")
                .whereEqualTo("PickupID", pickupID) // Ensure the field name matches
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d("fetchParcelDetails", "Parcel_Pickups query success. Found " + querySnapshot.size() + " parcels.");
                        StringBuilder parcelDetails = new StringBuilder();

                        // Iterate through Parcel_Pickups documents to get ParcelNos
                        for (DocumentSnapshot parcelDoc : querySnapshot) {
                            String parcelNo = parcelDoc.getString("ParcelNo");
                            Log.d("fetchParcelDetails", "Processing ParcelNo: " + parcelNo);

                            if (parcelNo != null) {
                                // Step 2: Query Parcels collection for each ParcelNo
                                db.collection("Parcels").document(parcelNo)
                                        .get()
                                        .addOnSuccessListener(parcelSnapshot -> {
                                            if (parcelSnapshot.exists()) {
                                                // Extract parcel details
                                                Log.d("fetchParcelDetails", "Details found for ParcelNo: " + parcelNo);
                                                String platform = parcelSnapshot.getString("PPlatform");
                                                String placeOfDrop = parcelSnapshot.getString("PPlaceOfDrop");
                                                String timeOfArrival = parcelSnapshot.getString("PTimeOfArrival");
                                                String size = parcelSnapshot.getString("PSize");

                                                // Append details to StringBuilder
                                                parcelDetails.append("Parcel No: ").append(parcelNo).append("\n")
                                                        .append("Platform: ").append(platform).append("\n")
                                                        .append("Place of Drop: ").append(placeOfDrop).append("\n")
                                                        .append("Time of Arrival: ").append(timeOfArrival).append("\n")
                                                        .append("Size: ").append(size).append("\n\n");

                                                // Update the UI
                                                tvParcelInfo.setText(parcelDetails.toString());
                                            } else {
                                                Log.d("fetchParcelDetails", "No details found for ParcelNo: " + parcelNo);
                                                parcelDetails.append("Parcel No: ").append(parcelNo).append(" - Details not found.\n\n");
                                                tvParcelInfo.setText(parcelDetails.toString());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("fetchParcelDetails", "Failed to query Parcels for ParcelNo: " + parcelNo, e);
                                            Toast.makeText(this, "Failed to load parcel details for ParcelNo: " + parcelNo, Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.d("fetchParcelDetails", "ParcelNo is null for a document in Parcel_Pickups.");
                            }
                        }
                    } else {
                        Log.d("fetchParcelDetails", "No parcels found for PickupID: " + pickupID);
                        tvParcelInfo.setText("No parcels found for this pickup.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("fetchParcelDetails", "Failed to query Parcel_Pickups for PickupID: " + pickupID, e);
                    Toast.makeText(this, "Failed to load parcel details.", Toast.LENGTH_SHORT).show();
                });
    }


    private void fetchFoodDetails(String pickupID) {
        // Step 1: Query Food_Pickups for associated ParcelNos
        db.collection("Pickups").document(pickupID).collection("Food_Pickups")
                .whereEqualTo("PickupID", pickupID) // Ensure the field name matches
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d("fetchFoodDetails", "Food_Pickups query success. Found " + querySnapshot.size() + " parcels.");
                        StringBuilder foodDetails = new StringBuilder();

                        // Iterate through Parcel_Pickups documents to get ParcelNos

                        for (DocumentSnapshot foodDoc : querySnapshot) {
                            String parcelNo = foodDoc.getString("FoodNo");
                            Log.d("fetchFoodDetails", "Processing foodNo: " + parcelNo);

                            if (parcelNo != null) {
                                // Step 2: Query Parcels collection for each ParcelNo
                                db.collection("Foods").document(parcelNo)
                                        .get()
                                        .addOnSuccessListener(parcelSnapshot -> {
                                            if (parcelSnapshot.exists()) {
                                                // Extract parcel details
                                                Log.d("fetchFoodDetails", "Details found for FoodNo: " + parcelNo);
                                                String platform = parcelSnapshot.getString("FPlatform");
                                                String placeOfDrop = parcelSnapshot.getString("FPlaceOfDrop");
                                                String timeOfArrival = parcelSnapshot.getString("FTimeOfArrival");
                                                String size = parcelSnapshot.getString("FSize");

                                                // Append details to StringBuilder
                                               foodDetails.append("Food No: ").append(parcelNo).append("\n")
                                                        .append("Platform: ").append(platform).append("\n")
                                                        .append("Place of Drop: ").append(placeOfDrop).append("\n")
                                                        .append("Time of Arrival: ").append(timeOfArrival).append("\n")
                                                        .append("Size: ").append(size).append("\n\n");

                                                // Update the UI
                                                tvParcelInfo.setText(foodDetails.toString());
                                            } else {
                                                Log.d("fetchFoodDetails", "No details found for foodNo: " + parcelNo);
                                                foodDetails.append("Food No: ").append(parcelNo).append(" - Details not found.\n\n");
                                                tvParcelInfo.setText(foodDetails.toString());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("fetchFoodDetails", "Failed to query Foods for FoodNo: " + parcelNo, e);
                                            Toast.makeText(this, "Failed to load Food details for FoodNo: " + parcelNo, Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.d("fetchFoodDetails", "FoodNo is null for a document in Food_Pickups.");
                            }
                        }
                    } else {
                        Log.d("fetchFoodDetails", "No Food found for PickupID: " + pickupID);
                        tvParcelInfo.setText("No Foods found for this pickup.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("fetchFoodDetails", "Failed to query Food_Pickups for PickupID: " + pickupID, e);
                    Toast.makeText(this, "Failed to load Food details.", Toast.LENGTH_SHORT).show();
                });
    }



    private void fetchPaymentDetails(String paymentID) {
        if (paymentID == null || paymentID.isEmpty()) {
            tvPaymentInfo.setText("Payment details not found.");
            return;
        }

        db.collection("Payments").document(paymentID)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Object amountObject = document.get("Amount");
                        double amount = 0.0; // Default value

                        // Safely handle the 'Amount' field
                        if (amountObject instanceof Number) {
                            amount = ((Number) amountObject).doubleValue();
                        } else if (amountObject instanceof String) {
                            try {
                                amount = Double.parseDouble((String) amountObject);
                            } catch (NumberFormatException e) {
                                Log.e("TaskDetailsActivity", "Amount field is not a valid number string", e);
                            }
                        } else {
                            Log.e("TaskDetailsActivity", "Amount field is of an unexpected type: " + (amountObject != null ? amountObject.getClass().getName() : "null"));
                        }

                        // Update UI
                        tvPaymentInfo.setText("Amount Charged: MYR " + amount);
                    } else {
                        tvPaymentInfo.setText("Payment details not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("fetchPaymentDetails", "Error fetching payment details: ", e);
                    tvPaymentInfo.setText("Failed to load payment details.");
                });
    }

    private void proceedStatusUpdate(String nextStatus) {
        db.collection("Pickups").document(pickupID)
                .update("Status", nextStatus)
                .addOnSuccessListener(aVoid -> {
                    currentStatus = nextStatus; // Update the currentStatus variable
                    tvCurrentStatus.setText("Current Status: " + currentStatus); // Update the UI
                    Toast.makeText(this, "Status updated to: " + currentStatus, Toast.LENGTH_SHORT).show();

                    if ("Delivered".equals(currentStatus)) {
                        btnUpdateStatus.setEnabled(false);
                        btnUpdateStatus.setText("No Further Updates");
                    }
                })

                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status.", Toast.LENGTH_SHORT).show();
                    Log.e("TaskDetailsActivity", "Error updating status: " + e.getMessage());
                });
    }

    private String getNextStatus(String currentStatus) {
        switch (currentStatus) {
            case "Unassigned":
                return "Assigned";
            case "Assigned":
                return "Picked Up";
            case "Picked Up":
                return "Incoming";
            case "Incoming":
                return "Delivered";
            default:
                return "Unknown";
        }
    }



}
