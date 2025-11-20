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

public class FindRunnerFood extends AppCompatActivity {
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvFindingRunners, tvRunnerName, tvRunnerPhone, tvRunnerVehicleNo, tvRunnerVehicleType, tvTotalPrice;
    private LinearLayout runnerDetailsLayout;
    private FirebaseFirestore firebaseFirestore;
    private LatLng uGuardhouseLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_runner);
        Intent intent = getIntent();
        String userEmail = getIntent().getStringExtra("UEmail");
        if (userEmail != null) {
            Log.d("FindRunner", "Received UEmail: " + userEmail);
        }
        else {
            Log.e("FindRunner", "UEmail not passed to this activity");
        }
        // Retrieve UGuardhouseLoc from Intent
        String uGuardhouseLoc = intent.getStringExtra("UGuardhouseLoc");
        if (uGuardhouseLoc != null) {
            String[] latLngParts = uGuardhouseLoc.split(",");
            if (latLngParts.length == 2) {
                try {
                    double latitude = Double.parseDouble(latLngParts[0].trim());
                    double longitude = Double.parseDouble(latLngParts[1].trim());
                    uGuardhouseLatLng = new LatLng(latitude, longitude);
                    Log.d("FindRunnerFood", "UGuardhouseLatLng: " + uGuardhouseLatLng);
                } catch (NumberFormatException e) {
                    Log.e("FindRunnerFood", "Invalid UGuardhouseLoc format: " + uGuardhouseLoc, e);
                    uGuardhouseLatLng = null; // Handle invalid input
                }
            } else {
                Log.e("FindRunnerFood", "UGuardhouseLoc is malformed: " + uGuardhouseLoc);
            }
        } else {
            Log.e("FindRunnerFood", "UGuardhouseLoc is missing in the intent.");
        }

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

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;
            if (uGuardhouseLatLng != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uGuardhouseLatLng, 15));
            } else {
                Log.e("FindRunnerFood", "uGuardhouseLatLng is null, cannot move camera.");
            }
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
        if (uGuardhouseLatLng == null) {
            Log.e("FindRunnerFood", "uGuardhouseLatLng is null. Cannot calculate distances.");
            return null;
        }

        Map<String, Object> bestRunner = null;
        double closestDistance = Double.MAX_VALUE;

        for (QueryDocumentSnapshot doc : querySnapshot) {
            String name = doc.getString("RName");
            String phone = doc.getString("RPhoneNo");
            String vehicleNo = doc.getString("RVehicleNo");
            String vehicleType = doc.getString("RVehicleType");
            GeoPoint location = doc.getGeoPoint("RLocation");
            Double currentTasks = doc.getDouble("CurrentTasks");

            if (name == null || phone == null || vehicleNo == null || vehicleType == null || location == null || currentTasks == null) {
                continue;
            }

            // Calculate distance to the user's guardhouse
            double distance = calculateDistance(location.getLatitude(), location.getLongitude(),
                    uGuardhouseLatLng.latitude, uGuardhouseLatLng.longitude);

            if (currentTasks < 2 && (bestRunner == null || distance < closestDistance)) {
                closestDistance = distance;
                bestRunner = doc.getData();
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
        // Calculate and display total price
        Intent intent = getIntent();
        String foodSize = intent.getStringExtra("FSize");
        int noOfItems = intent.getIntExtra("NoOfItems", 0);
        double totalPrice = calculateCharge(foodSize, noOfItems);
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
        String fSize = intent.getStringExtra("FSize");
        int noOfItems = intent.getIntExtra("NoOfItems", 0);
        double totalPrice = calculateCharge(fSize, noOfItems);
        String placeGuard = intent.getStringExtra("FPlaceOfDrop");
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
                    Intent trackingIntent = new Intent(FindRunnerFood.this, PaymentMethodActivity.class);
                    trackingIntent.putExtra("RunnerID", (String) bestRunner.get("RunnerID"));
                    trackingIntent.putExtra("RName", (String) bestRunner.get("RName"));
                    trackingIntent.putExtra("RPhoneNo", (String) bestRunner.get("RPhoneNo"));
                    trackingIntent.putExtra("RVehicleNo", (String) bestRunner.get("RVehicleNo"));
                    trackingIntent.putExtra("RVehicleType", (String) bestRunner.get("RVehicleType"));
                    // Pass food details
                    trackingIntent.putExtra("UEmail", userEmail); // Pass UEmail
                    trackingIntent.putExtra("FPlaceOfDrop", placeGuard);
                    trackingIntent.putExtra("UName", intent.getStringExtra("RecipientName"));
                    trackingIntent.putExtra("UDormAddress", intent.getStringExtra("RecipientAddress"));
                    trackingIntent.putExtra("UPhoneNo", intent.getStringExtra("RecipientPhone"));
                    trackingIntent.putExtra("NoOfItems", noOfItems);
                    trackingIntent.putExtra("FoodNo", intent.getStringExtra("FoodNo"));
                    trackingIntent.putExtra("FPlatform", intent.getStringExtra("FPlatform"));
                    trackingIntent.putExtra("FSize", fSize);
                    trackingIntent.putExtra("FPlaceOfDropLat", uGuardhouseLatLng.latitude);
                    trackingIntent.putExtra("FPlaceOfDropLng", uGuardhouseLatLng.longitude);
                    trackingIntent.putExtra("FTimeOfArrival", intent.getStringExtra("FTimeOfArrival"));
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

        return baseCharge + sizeCharge + (noOfItems * 0.70);
    }
}
