package com.example.swooshv2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;

public class FindRunner extends AppCompatActivity {
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvFindingRunners, tvRunnerName, tvRunnerPhone, tvRunnerVehicleNo, tvRunnerVehicleType, tvTotalPrice;
    private LinearLayout runnerDetailsLayout;
    private FirebaseFirestore firebaseFirestore;

    private LatLng pppLocation = new LatLng(2.310064406660653, 102.31859876730901); // PPP Location




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_runner);
        String userEmail = getIntent().getStringExtra("UEmail");
        if (userEmail != null) {
            Log.d("FindRunner", "Received UEmail: " + userEmail);
        }
        else {
            Log.e("FindRunner", "UEmail not passed to this activity");
        }

        // Initialize Firestore
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initialize Views
        mapView = findViewById(R.id.mapView);
        tvFindingRunners = findViewById(R.id.tvFindingRunners);
        runnerDetailsLayout = findViewById(R.id.runnerDetails);
        tvRunnerName = findViewById(R.id.tvRunnerName);
        tvRunnerPhone = findViewById(R.id.tvRunnerPhone);
        tvRunnerVehicleNo = findViewById(R.id.tvRunnerVehicleNo);
        tvRunnerVehicleType = findViewById(R.id.tvRunnerVehicleType);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);


        // Initialize Map
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pppLocation, 15));
            fetchRunners();
        });
    }




    private void fetchRunners() {
        firebaseFirestore.collection("Runners")
                .whereEqualTo("RisAvailable", true) // Ensure RisAvailable exists and is true
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Map<String, Object> bestRunner = findBestRunner(querySnapshot);
                        if (bestRunner != null) {
                            displayRunnerDetails(bestRunner);
                            showConfirmationDialog(bestRunner);
                        } else {
                            Toast.makeText(this, "No suitable runners found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No available runners found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FindRunner", "Failed to fetch runners", e);
                    Toast.makeText(this, "Failed to fetch runners!", Toast.LENGTH_SHORT).show();
                });




    }




    private Map<String, Object> findBestRunner(QuerySnapshot querySnapshot) {
        Map<String, Object> bestRunner = null;
        double closestDistance = Double.MAX_VALUE; // Initialize to a very high value




        for (QueryDocumentSnapshot doc : querySnapshot) {
            String name = doc.getString("RName");
            String phone = doc.getString("RPhoneNo");
            String vehicleNo = doc.getString("RVehicleNo");
            String vehicleType = doc.getString("RVehicleType");
            GeoPoint location = doc.getGeoPoint("RLocation");
            Double currentTasks = doc.getDouble("CurrentTasks");




            if (name == null || phone == null || vehicleNo == null || vehicleType == null || location == null || currentTasks == null) {
                continue; // Skip runners with missing fields
            }




            // Calculate distance to PPP
            double distance = calculateDistance(location.getLatitude(), location.getLongitude(), pppLocation.latitude, pppLocation.longitude);




            if (currentTasks < 2) { // Only consider runners with less than 2 tasks
                if (bestRunner == null || distance < closestDistance) {
                    closestDistance = distance;
                    bestRunner = doc.getData();
                }
            }
        }




        return bestRunner;
    }




    private void displayRunnerDetails(Map<String, Object> runner) {
        tvFindingRunners.setVisibility(View.GONE);
        runnerDetailsLayout.setVisibility(View.VISIBLE);




        tvRunnerName.setText("Runner Name: " + runner.get("RName"));
        tvRunnerPhone.setText("Phone Number: " + runner.get("RPhoneNo"));
        tvRunnerVehicleNo.setText("Vehicle Number: " + runner.get("RVehicleNo"));
        tvRunnerVehicleType.setText("Vehicle Type: " + runner.get("RVehicleType"));





        Intent intent = getIntent();
        String parcelSize = intent.getStringExtra("ParcelSize");
        int noOfItems = intent.getIntExtra("NoOfItems", 0);
        double totalPrice = calculateCharge(parcelSize, noOfItems);
        tvTotalPrice.setText("Total Price: MYR " + totalPrice);
    }




    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);




        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);




        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }




    private void showConfirmationDialog(Map<String, Object> bestRunner) {
        // Retrieve and calculate total price
        Intent intent = getIntent();
        String parcelSize = intent.getStringExtra("ParcelSize");
        int noOfItems = intent.getIntExtra("NoOfItems", 0);
        double totalPrice = calculateCharge(parcelSize, noOfItems);
        String userEmail = intent.getStringExtra("UEmail");
        if (userEmail == null) {
            Log.e("FindRunner", "UEmail is missing. Cannot proceed to PaymentActivity.");
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }




        new AlertDialog.Builder(this)
                .setTitle("Runner Found!")
                .setMessage("Do you want to confirm this task with " + bestRunner.get("RName") + "?\n\n" +
                        "Total Price: MYR " + totalPrice)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Pass the RunnerID and other details to PaymentActivity
                    Log.d("FindRunner", "Selected RunnerID: " + bestRunner.get("RunnerID"));
                    Intent trackingIntent = new Intent(FindRunner.this, PaymentActivity.class);
                    trackingIntent.putExtra("RunnerID", (String) bestRunner.get("RunnerID"));
                    trackingIntent.putExtra("RName", (String) bestRunner.get("RName"));
                    trackingIntent.putExtra("RPhoneNo", (String) bestRunner.get("RPhoneNo"));
                    trackingIntent.putExtra("RVehicleNo", (String) bestRunner.get("RVehicleNo"));
                    trackingIntent.putExtra("RVehicleType", (String) bestRunner.get("RVehicleType"));




                    // Pass pickup details
                    trackingIntent.putExtra("UEmail", userEmail); // Pass UEmail
                    trackingIntent.putExtra("UName", intent.getStringExtra("RecipientName"));
                    trackingIntent.putExtra("UDormAddress", intent.getStringExtra("RecipientAddress"));
                    trackingIntent.putExtra("UPhoneNo", intent.getStringExtra("RecipientPhone"));
                    trackingIntent.putExtra("NoOfItems", noOfItems);
                    trackingIntent.putExtra("ParcelNo", intent.getStringExtra("TrackingNo"));
                    trackingIntent.putExtra("PPlatform", intent.getStringExtra("DeliveryCourier"));
                    trackingIntent.putExtra("PSize", parcelSize);
                    trackingIntent.putExtra("PPlaceOfDrop", intent.getStringExtra("DropOffPlace"));
                    trackingIntent.putExtra("PTimeOfArrival", intent.getStringExtra("TimeOfArrival"));
                    trackingIntent.putExtra("Amount", String.valueOf(totalPrice));




                    startActivity(trackingIntent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    private double calculateCharge(String size, int noOfItems) {
        double baseCharge = 2.00;
        double sizeCharge;

        switch (size.toLowerCase()) {
            case "small":
                sizeCharge = 1.00;
                break;
            case "medium":
                sizeCharge = 2.00;
                break;
            case "large":
                sizeCharge = 3.00;
                break;
            default:
                sizeCharge = 0.00;
        }




        return baseCharge + sizeCharge + (noOfItems * 0.50);
    }
}
