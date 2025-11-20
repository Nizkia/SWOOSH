package com.example.swooshv2;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PickupRequestParcel extends AppCompatActivity {

    private Button btnPickTime, btnConfirm;
    private TextView tvRecipientName, tvRecipientAddress, tvRecipientPhone, tvRecipientDormitory, tvRecipientDormBlock;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String selectedTimeOfArrival;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_request_parcel);
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initialize Views
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientAddress = findViewById(R.id.tvRecipientAddress);
        tvRecipientPhone = findViewById(R.id.tvRecipientPhone);
        tvRecipientDormitory = findViewById(R.id.tvRecipientDormitory);
        tvRecipientDormBlock = findViewById(R.id.tvRecipientDormBlock);

        // Load User Data
        loadUserData();

        // Initialize Spinners
        Spinner spNoOfParcel = findViewById(R.id.spNoOfParcel);
        Spinner spDeliveryCourier = findViewById(R.id.spDeliveryCourier);

        // Set ArrayAdapter for spNoOfParcel
        ArrayAdapter<CharSequence> parcelAdapter = ArrayAdapter.createFromResource(
                this, R.array.parcel_number_options, android.R.layout.simple_spinner_item);
        parcelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNoOfParcel.setAdapter(parcelAdapter);

        spNoOfParcel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = parent.getItemAtPosition(position).toString();

                if (selectedValue.equals("1")) {
                    // If user selects 1 parcel, provide instructions for single tracking number
                    Toast.makeText(PickupRequestParcel.this, "Enter the tracking number for one parcel.", Toast.LENGTH_SHORT).show();
                } else {
                    // If user selects more than 1 parcel, provide instructions for multiple tracking numbers
                    Toast.makeText(PickupRequestParcel.this, "Enter tracking numbers separated by commas.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(PickupRequestParcel.this, "Select Something", Toast.LENGTH_SHORT).show();
            }
        });

        // Set ArrayAdapter for spDeliveryCourier
        ArrayAdapter<CharSequence> courierAdapter = ArrayAdapter.createFromResource(
                this, R.array.courier_options, android.R.layout.simple_spinner_item);
        courierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeliveryCourier.setAdapter(courierAdapter);

        // Initialize Time Picker
        btnPickTime = findViewById(R.id.btnPickTime);
        btnConfirm= findViewById(R.id.btnConfirm);
        btnPickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String deliveryCourier = spDeliveryCourier.getSelectedItem().toString();
                String parcelSize = ((RadioButton) findViewById(((RadioGroup) findViewById(R.id.radioGroupParcelSize))
                        .getCheckedRadioButtonId())).getText().toString();
                String dropOffPlace = ((TextView) findViewById(R.id.textViewDropOffPlace)).getText().toString();
                String recipientName = tvRecipientName.getText().toString().replace("Recipient Name: ", "");
                String recipientAddress = tvRecipientAddress.getText().toString().replace("Address: ", "");
                String recipientPhone = tvRecipientPhone.getText().toString().replace("Phone Number: ", "");
                String dormitory = tvRecipientDormitory.getText().toString().replace("Dormitory: ", "");
                String dormBlock = tvRecipientDormBlock.getText().toString().replace("Dorm Block: ", "");
                String noOfItemsStr = spNoOfParcel.getSelectedItem().toString();
                String trackingNo = ((EditText) findViewById(R.id.etParcelTrackingNo)).getText().toString().trim();
                int noOfParcels = Integer.parseInt(noOfItemsStr);
                if (noOfParcels > 1) {
                    if (!trackingNo.contains(",")) {
                        Toast.makeText(PickupRequestParcel.this, "Please separate tracking numbers with commas.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] trackingNumbers = trackingNo.split(",");
                    if (trackingNumbers.length != noOfParcels) {
                        Toast.makeText(PickupRequestParcel.this, "Number of tracking numbers must match the selected parcel count.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (trackingNo.isEmpty()) {
                    Toast.makeText(PickupRequestParcel.this, "Please enter a tracking number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate required fields
                if (noOfItemsStr.isEmpty() || trackingNo.isEmpty() || deliveryCourier.isEmpty() || parcelSize.isEmpty()) {
                    Toast.makeText(PickupRequestParcel.this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the logged-in user's email
                String userEmail = firebaseAuth.getCurrentUser().getEmail();
                if (userEmail == null || userEmail.isEmpty()) {
                    Toast.makeText(PickupRequestParcel.this, "User email not available. Please log in again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Redirect to FindRunner with collected data and UEmail
                Intent intent = new Intent(PickupRequestParcel.this, FindRunner.class);

                intent.putExtra("UEmail", userEmail);
                intent.putExtra("NoOfItems", noOfParcels);
                intent.putExtra("TrackingNo", trackingNo);
                intent.putExtra("DeliveryCourier", deliveryCourier);
                intent.putExtra("ParcelSize", parcelSize);
                intent.putExtra("DropOffPlace", dropOffPlace);
                intent.putExtra("RecipientName", recipientName);
                intent.putExtra("RecipientAddress", recipientAddress);
                intent.putExtra("RecipientPhone", recipientPhone);
                intent.putExtra("UDormitory", dormitory);
                intent.putExtra("UDormBlock", dormBlock);

                // Default value for Status
                intent.putExtra("Status", "Unassigned");

                // Pass the selected time of arrival
                if (selectedTimeOfArrival != null) {
                    intent.putExtra("TimeOfArrival", selectedTimeOfArrival);
                } else {
                    Toast.makeText(PickupRequestParcel.this, "Please select a time for pickup!", Toast.LENGTH_SHORT).show();
                    return;
                }

                startActivity(intent);
            }
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

            TimePickerDialog timePickerDialog = new TimePickerDialog(PickupRequestParcel.this, (view1, hourOfDay, minute1) -> {
                // Create a Calendar object with the selected date and time
                Calendar selectedDateTime = Calendar.getInstance();
                selectedDateTime.set(year1, month1, dayOfMonth, hourOfDay, minute1);

                // Get the current time
                long currentTimeMillis = System.currentTimeMillis();

                // Validate the selected time
                if (selectedDateTime.getTimeInMillis() > currentTimeMillis) {
                    Toast.makeText(PickupRequestParcel.this, "You cannot select a future time!", Toast.LENGTH_SHORT).show();
                } else {
                    selectedTimeOfArrival = dayOfMonth + "/" + (month1 + 1) + "/" + year1 + " " + hourOfDay + ":" + minute1;
                    btnPickTime.setText(selectedTimeOfArrival); // Update button text
                    Toast.makeText(PickupRequestParcel.this, "Time Selected: " + selectedTimeOfArrival, Toast.LENGTH_SHORT).show();
                }
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

                                // Fetch user details
                                String name = documentSnapshot.getString("UName");
                                String address = documentSnapshot.getString("UDormAddress");
                                String phone = documentSnapshot.getString("UPhoneNo");
                                String dormitory = documentSnapshot.getString("UDormitory");
                                String dormBlock = documentSnapshot.getString("UDormBlock");

                                // Update the UI
                                tvRecipientName.setText("Recipient Name: " + name);
                                tvRecipientAddress.setText("Address: " + address);
                                tvRecipientPhone.setText("Phone Number: " + phone);
                                tvRecipientDormitory.setText("Dormitory: " + dormitory);
                                tvRecipientDormBlock.setText("Dorm Block: " + dormBlock);
                            } else {
                                Toast.makeText(this, "No user data found for this email.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Logged-in user's email is null.", Toast.LENGTH_SHORT).show();
            }
        } else {
            tvRecipientName.setText("Please log in to make a request.");
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }


}
