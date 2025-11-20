package com.example.swooshv2;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;


public class PaymentMethodActivity extends AppCompatActivity {


    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);


        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();


        String userEmail = getIntent().getStringExtra("UEmail");
        if (userEmail != null) {
            Log.d("PaymentMethodActivity", "Received UEmail: " + userEmail);
        }
        else {
            Log.e("PaymentMethodActivity", "UEmail not passed to this activity");
        }


        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        // Retrieve data from Intent
        Intent intent = getIntent();
        String recipientName = intent.getStringExtra("UName");
        String recipientAddress = intent.getStringExtra("UDormAddress");
        String recipientPhone = intent.getStringExtra("UPhoneNo");
        int numberOfParcels = intent.getIntExtra("NoOfItems", 0);
        String trackingNumber = intent.getStringExtra("FoodNo");
        String deliveryCourier = intent.getStringExtra("FPlatform");
        String parcelSize = intent.getStringExtra("FSize");
        String dropOffPlace = intent.getStringExtra("FPlaceOfDrop");
        String timeOfArrival = intent.getStringExtra("FTimeOfArrival");
        String runnerID = intent.getStringExtra("RunnerID");
        String runnerName = intent.getStringExtra("RunnerName");
        String totalPrice = intent.getStringExtra("Amount");


        if (recipientName == null || recipientAddress == null || recipientPhone == null) {
            Log.e("PaymentMethodActivity", "Missing recipient details.");
            Toast.makeText(this, "Recipient details are incomplete.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind views and set data
        ((TextView) findViewById(R.id.tvRecipientName)).setText("Name: " + recipientName);
        ((TextView) findViewById(R.id.tvRecipientAddress)).setText("Address: " + recipientAddress);
        ((TextView) findViewById(R.id.tvRecipientPhone)).setText("Phone: " + recipientPhone);
        ((TextView) findViewById(R.id.tvNumberOfParcels)).setText("Number of Food Items: " + numberOfParcels);
        ((TextView) findViewById(R.id.tvTrackingNumber)).setText("Food Tracking Number: " + trackingNumber);
        ((TextView) findViewById(R.id.tvDeliveryCourier)).setText("Food Delivery Courier: " + deliveryCourier);
        ((TextView) findViewById(R.id.tvParcelSize)).setText("Item Size: " + parcelSize);
        ((TextView) findViewById(R.id.tvDropOffPlace)).setText("Drop-Off Place: " + dropOffPlace);
        ((TextView) findViewById(R.id.tvTimeOfArrival)).setText("Time of Arrival: " + timeOfArrival);
        ((TextView) findViewById(R.id.tvRunnerName)).setText("Runner Name: " + runnerName);
        ((TextView) findViewById(R.id.tvTotalPrice)).setText("MYR " + totalPrice);


        // Confirm Payment Button
        MaterialButton buttonConfirmPayment = findViewById(R.id.buttonConfirmPayment);
        buttonConfirmPayment.setOnClickListener(v -> {
            String paymentID = generateCustomID("PMF3", 4);
            String pickupID = generateCustomID("PUF2", 4);

            // Current timestamp
            Timestamp currentTimestamp = Timestamp.now();

            // Payment data
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("PaymentID", paymentID);
            paymentData.put("Amount", totalPrice);
            paymentData.put("PayMethod", "CASH");
            paymentData.put("Date", currentTimestamp);

            // Pickup data
            Map<String, Object> pickupData = new HashMap<>();
            pickupData.put("PickupID", pickupID);
            pickupData.put("RunnerID", runnerID);
            pickupData.put("UEmail", userEmail);
            pickupData.put("NoOfItems", numberOfParcels);
            pickupData.put("Status", "Assigned");
            pickupData.put("DTPickupMade", currentTimestamp);
            pickupData.put("PaymentID", paymentID);

            // Save payment and pickup to top-level collections
            firestore.collection("Payments").document(paymentID).set(paymentData);
            firestore.collection("Pickups").document(pickupID).set(pickupData);


            // Extract parcel numbers and insert into "Parcels" collection and "Parcel_Pickups" subcollection
            String[] parcelNumbers = trackingNumber.split(",");
            for (String foodNo : parcelNumbers) {
                foodNo = foodNo.trim();

                // Food  data
                Map<String, Object> foodData = new HashMap<>();
                foodData.put("FoodNo", foodNo);
                foodData.put("FPlatform", deliveryCourier);
                foodData.put("FSize", parcelSize);
                foodData.put("FPlaceOfDrop", dropOffPlace);
                foodData.put("FTimeOfArrival", timeOfArrival);

                // Save food to "Foods" collection
                firestore.collection("Foods").document(foodNo).set(foodData);

                // Save food-pickup relationship in "Food_Pickups" subcollection
                Map<String, Object> foodPickupData = new HashMap<>();

                foodPickupData.put("PickupID", pickupID);
                foodPickupData.put("FoodNo", foodNo);


                firestore.collection("Pickups")
                        .document(pickupID)
                        .collection("Food_Pickups")
                        .document(foodNo)
                        .set(foodPickupData);
            }


            // Redirect to RunnerTracking
            Intent trackingIntent = new Intent(PaymentMethodActivity.this, RunnerTracking.class);
            trackingIntent.putExtra("RunnerID", runnerID);
            trackingIntent.putExtra("RName", runnerName);
            trackingIntent.putExtra("UEmail", userEmail);
            trackingIntent.putExtra("Status", "Assigned");
            trackingIntent.putExtra("PickupID", pickupID);
            trackingIntent.putExtra("FPlaceOfDropLat", getIntent().getDoubleExtra("FPlaceOfDropLat", 0.0));
            trackingIntent.putExtra("FPlaceOfDropLng", getIntent().getDoubleExtra("FPlaceOfDropLng", 0.0));
            startActivity(trackingIntent);
        });






    }


    private String generateCustomID(String prefix, int numDigits) {
        int randomNum = (int) (Math.random() * Math.pow(10, numDigits)); // Generate a random number with the desired number of digits
        return String.format("%s%0" + numDigits + "d", prefix, randomNum);
    }
}
