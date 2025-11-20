package com.example.swooshv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GenerateReport extends AppCompatActivity {

    private TextView tvReport;
    private ScrollView svReportContainer;
    private FirebaseFirestore db;

    private static final String TAG = "GenerateReport"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        tvReport = findViewById(R.id.tvReport);
        svReportContainer = findViewById(R.id.svReportContainer);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Generate the report by fetching complex data
        generateReport();
    }

    private void generateReport() {
        tvReport.setText("Fetching report data..."); // Initial message

        db.collection("Pickups").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                StringBuilder reportContent = new StringBuilder();

                for (DocumentSnapshot pickupDocument : task.getResult()) {
                    String pickupID = pickupDocument.getString("PickupID");
                    String userEmail = pickupDocument.getString("UEmail");
                    String status = pickupDocument.getString("Status");
                    String datePickupMade = pickupDocument.get("DTPickupMade") instanceof com.google.firebase.Timestamp
                            ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(((com.google.firebase.Timestamp) pickupDocument.get("DTPickupMade")).toDate())
                            : "N/A";

                    reportContent.append("Pickup ID: ").append(pickupID != null ? pickupID : "N/A").append("\n")
                            .append("User Email: ").append(userEmail != null ? userEmail : "N/A").append("\n")
                            .append("Status: ").append(status != null ? status : "N/A").append("\n")
                            .append("Date Pickup Made: ").append(datePickupMade).append("\n");

                    fetchChildData(pickupID, reportContent);
                    reportContent.append("--------------------------------------------------\n");
                }

                tvReport.setText(reportContent.toString());
            } else {
                Log.e(TAG, "Error fetching pickup data", task.getException());
                Toast.makeText(this, "Failed to load report data", Toast.LENGTH_SHORT).show();
                tvReport.setText("Error fetching report data.");
            }
        });
    }

    private void fetchChildData(String pickupID, StringBuilder reportContent) {
        // Fetch Parcel_Pickups
        db.collection("Pickups").document(pickupID).collection("Parcel_Pickups").get()
                .addOnSuccessListener(parcelPickups -> {
                    reportContent.append("  Parcel Pickups:\n");
                    for (DocumentSnapshot parcelPickup : parcelPickups) {
                        String parcelNo = parcelPickup.getString("ParcelNo");
                        reportContent.append("    Parcel No: ").append(parcelNo != null ? parcelNo : "N/A").append("\n");
                        fetchParcelDetails(parcelNo, reportContent);
                    }
                });

        // Fetch Food_Pickups
        db.collection("Pickups").document(pickupID).collection("Food_Pickups").get()
                .addOnSuccessListener(foodPickups -> {
                    reportContent.append("  Food Pickups:\n");
                    for (DocumentSnapshot foodPickup : foodPickups) {
                        String foodNo = foodPickup.getString("FoodNo");
                        reportContent.append("    Food No: ").append(foodNo != null ? foodNo : "N/A").append("\n");
                        fetchFoodDetails(foodNo, reportContent);
                    }
                });
    }

    private void fetchParcelDetails(String parcelNo, StringBuilder reportContent) {
        if (parcelNo != null) {
            db.collection("Parcels").document(parcelNo).get().addOnSuccessListener(parcel -> {
                if (parcel.exists()) {
                    String platform = parcel.getString("PPlatform");
                    String placeOfDrop = parcel.getString("PPlaceOfDrop");
                    String size = parcel.getString("PSize");
                    Object timeOfArrivalObj = parcel.get("PTimeOfArrival");
                    String timeOfArrival = timeOfArrivalObj instanceof com.google.firebase.Timestamp
                            ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(((com.google.firebase.Timestamp) timeOfArrivalObj).toDate())
                            : timeOfArrivalObj != null ? timeOfArrivalObj.toString() : "N/A";

                    reportContent.append("      Platform: ").append(platform != null ? platform : "N/A").append("\n")
                            .append("      Place of Drop: ").append(placeOfDrop != null ? placeOfDrop : "N/A").append("\n")
                            .append("      Size: ").append(size != null ? size : "N/A").append("\n")
                            .append("      Time of Arrival: ").append(timeOfArrival).append("\n");
                } else {
                    reportContent.append("      Parcel details not found.\n");
                }
            });
        }
    }

    private void fetchFoodDetails(String foodNo, StringBuilder reportContent) {
        if (foodNo != null) {
            db.collection("Food").document(foodNo).get().addOnSuccessListener(food -> {
                if (food.exists()) {
                    String platform = food.getString("FPlatform");
                    String placeOfDrop = food.getString("FPlaceOfDrop");
                    String size = food.getString("FSize");
                    Object timeOfArrivalObj = food.get("FTimeOfArrival");
                    String timeOfArrival = timeOfArrivalObj instanceof com.google.firebase.Timestamp
                            ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(((com.google.firebase.Timestamp) timeOfArrivalObj).toDate())
                            : timeOfArrivalObj != null ? timeOfArrivalObj.toString() : "N/A";

                    reportContent.append("      Platform: ").append(platform != null ? platform : "N/A").append("\n")
                            .append("      Place of Drop: ").append(placeOfDrop != null ? placeOfDrop : "N/A").append("\n")
                            .append("      Size: ").append(size != null ? size : "N/A").append("\n")
                            .append("      Time of Arrival: ").append(timeOfArrival).append("\n");
                } else {
                    reportContent.append("      Food details not found.\n");
                }
            });
        }
    }
}

