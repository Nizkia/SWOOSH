package com.example.swooshv2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class PickupRequestFood extends AppCompatActivity {

    private Button btnPickTime, btnConfirm;
    private TextView tvRecipientName, tvRecipientAddress, tvRecipientPhone, tvRecipientDormitory, tvRecipientDormBlock, tvDropOff;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String selectedTimeOfArrival;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_request_food);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initialize Views
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientAddress = findViewById(R.id.tvRecipientAddress);
        tvRecipientPhone = findViewById(R.id.tvRecipientPhone);
        tvRecipientDormitory = findViewById(R.id.tvRecipientDormitory);
        tvRecipientDormBlock = findViewById(R.id.tvRecipientDormBlock);

        tvDropOff=findViewById(R.id.textViewDropOffPlace);

        // Load User Data
        loadUserData();

        // Initialize Spinners
        Spinner spNoOfItems = findViewById(R.id.spNoOfItems);
        Spinner spDeliveryCourier = findViewById(R.id.spDeliveryCourier);

        // Set ArrayAdapter for spNoOfItems
        ArrayAdapter<CharSequence> itemsAdapter = ArrayAdapter.createFromResource(
                this, R.array.parcel_number_options, android.R.layout.simple_spinner_item);
        itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNoOfItems.setAdapter(itemsAdapter);

        spNoOfItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = parent.getItemAtPosition(position).toString();

                if (selectedValue.equals("1")) {
                    Toast.makeText(PickupRequestFood.this, "Enter the parcel number for one item.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PickupRequestFood.this, "Enter parcel numbers separated by commas.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(PickupRequestFood.this, "Please Select Number Of Items.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set ArrayAdapter for spDeliveryCourier
        ArrayAdapter<CharSequence> courierAdapter = ArrayAdapter.createFromResource(
                this, R.array.food_courier_options, android.R.layout.simple_spinner_item);
        courierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeliveryCourier.setAdapter(courierAdapter);

        // Initialize Time Picker
        btnPickTime = findViewById(R.id.btnPickTime);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnPickTime.setOnClickListener(v -> showDateTimePicker());

        btnConfirm.setOnClickListener(v -> {

            if (selectedTimeOfArrival == null || selectedTimeOfArrival.isEmpty()) {
                Toast.makeText(this, "Please select a time for pickup!", Toast.LENGTH_SHORT).show();
                return;
            }

            String deliveryCourier = spDeliveryCourier.getSelectedItem().toString();
            String foodSize = ((RadioButton) findViewById(((RadioGroup) findViewById(R.id.radioGroupFoodSize))
                    .getCheckedRadioButtonId())).getText().toString();
            String dropOffPlace = tvDropOff.getText().toString().replace("UGuardhouseLoc: ", "");
            String recipientName = tvRecipientName.getText().toString().replace("Recipient Name: ", "");
            String recipientAddress = tvRecipientAddress.getText().toString().replace("Address: ", "");
            String recipientPhone = tvRecipientPhone.getText().toString().replace("Phone Number: ", "");
            String dormitory = tvRecipientDormitory.getText().toString().replace("Dormitory: ", "");
            String dormBlock = tvRecipientDormBlock.getText().toString().replace("Dorm Block: ", "");
            String noOfItemsStr = spNoOfItems.getSelectedItem().toString();
            String trackingNo = ((EditText) findViewById(R.id.etFoodTrackingNo)).getText().toString().trim();
            int noOfItems = Integer.parseInt(noOfItemsStr);

            if (noOfItems > 1) {
                if (!trackingNo.contains(",")) {
                    Toast.makeText(PickupRequestFood.this, "Please separate tracking numbers with commas.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] trackingNumbers = trackingNo.split(",");
                if (trackingNumbers.length != noOfItems) {
                    Toast.makeText(PickupRequestFood.this, "Number of tracking numbers must match the selected item count.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (trackingNo.isEmpty()) {
                Toast.makeText(PickupRequestFood.this, "Please enter a tracking number.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate required fields
            if (noOfItemsStr.isEmpty() || trackingNo.isEmpty() || deliveryCourier.isEmpty() || foodSize.isEmpty()) {
                Toast.makeText(PickupRequestFood.this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            String uGuardhouseLoc = null;
            switch (dormitory) {
                case "Satria":
                    uGuardhouseLoc = "2.3097575795914516, 102.31544188879347";
                    tvDropOff.setText("Satria Guardhouse");
                    break;
                case "Lestari":
                    uGuardhouseLoc = "2.314584103123155, 102.31661351058278";
                    tvDropOff.setText("Lestari Guardhouse");
                    break;
                case "Al-Jazari":
                    uGuardhouseLoc = "2.3214742595905347, 102.32867687471179";
                    tvDropOff.setText("Al-Jazari Guardhouse");
                    break;
                default:
                    Toast.makeText(PickupRequestFood.this, "Invalid Dormitory selected!", Toast.LENGTH_SHORT).show();
                    return;
            }

            // Get the logged-in user's email
            String userEmail = firebaseAuth.getCurrentUser().getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                Toast.makeText(PickupRequestFood.this, "User email not available. Please log in again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Redirect to FindRunner with collected data and UEmail
            Intent intent = new Intent(PickupRequestFood.this, FindRunnerFood.class);

            intent.putExtra("UEmail", userEmail);
            intent.putExtra("NoOfItems", noOfItems);
            intent.putExtra("FoodNo", trackingNo);
            intent.putExtra("FPlatform", deliveryCourier);
            intent.putExtra("FSize", foodSize);
            intent.putExtra("FPlaceOfDrop", tvDropOff.getText());
            intent.putExtra("RecipientName", recipientName);
            intent.putExtra("RecipientAddress", recipientAddress);
            intent.putExtra("RecipientPhone", recipientPhone);
            intent.putExtra("UDormitory", dormitory);
            intent.putExtra("UDormBlock", dormBlock);
            intent.putExtra("UGuardhouseLoc", uGuardhouseLoc);
            intent.putExtra("Status", "Unassigned");
            if (selectedTimeOfArrival != null) {
                intent.putExtra("FTimeOfArrival", selectedTimeOfArrival);
            } else {
                Toast.makeText(PickupRequestFood.this, "Please select a time for pickup!", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(intent);
        });
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(PickupRequestFood.this, (view1, hourOfDay, minute1) -> {
                Calendar selectedDateTime = Calendar.getInstance();
                selectedDateTime.set(year1, month1, dayOfMonth, hourOfDay, minute1);

                selectedTimeOfArrival = dayOfMonth + "/" + (month1 + 1) + "/" + year1 + " " + hourOfDay + ":" + minute1;
                btnPickTime.setText(selectedTimeOfArrival);
                Toast.makeText(PickupRequestFood.this, "Time Selected: " + selectedTimeOfArrival, Toast.LENGTH_SHORT).show();
            }, hour, minute, true);

            timePickerDialog.show();
        }, year, month, day);

        datePickerDialog.show();
    }

        private void loadUserData() {
            if (firebaseAuth.getCurrentUser() != null) {
                String userEmail = firebaseAuth.getCurrentUser().getEmail();

                if (userEmail != null) {
                    firebaseFirestore.collection("Users")
                            .whereEqualTo("UEmail", userEmail)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                                    String name = documentSnapshot.getString("UName");
                                    String address = documentSnapshot.getString("UDormAddress");
                                    String phone = documentSnapshot.getString("UPhoneNo");
                                    String dormitory = documentSnapshot.getString("UDormitory");
                                    String dormBlock = documentSnapshot.getString("UDormBlock");

                                    tvRecipientName.setText("Recipient Name: " + name);
                                    tvRecipientAddress.setText("Address: " + address);
                                    tvRecipientPhone.setText("Phone Number: " + phone);
                                    tvRecipientDormitory.setText("Dormitory: " + dormitory);
                                    tvRecipientDormBlock.setText("Dorm Block: " + dormBlock);
                                } else {
                                    Toast.makeText(this, "No user data found for this email.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "Logged-in user's email is null.", Toast.LENGTH_SHORT).show();
                }
            } else {
                tvRecipientName.setText("Please log in to make a request.");
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            }
        }
}
