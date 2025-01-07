package com.example.myapplication;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MakePickupRequestActivity extends AppCompatActivity {
    private TextInputEditText editTextDeliveryTime;
    private AutoCompleteTextView spinnerRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_pickup_request);

        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        editTextDeliveryTime = findViewById(R.id.editTextDeliveryTime);
        spinnerRunner = findViewById(R.id.spinnerRunner);

        // Setup runner dropdown
        setupRunnerDropdown();

        // Setup delivery time picker
        setupDeliveryTimePicker();

        // Setup confirm order button
        MaterialButton buttonConfirmOrder = findViewById(R.id.buttonConfirmOrder);
        buttonConfirmOrder.setOnClickListener(v -> {
            Intent intent = new Intent(MakePickupRequestActivity.this, PaymentActivity.class);
            startActivity(intent);
        });
    }

    private void setupRunnerDropdown() {
        // Create sample runner list - you can replace this with actual data from your backend
        String[] runners = new String[]{
                "John Doe (4.8★)",
                "Jane Smith (4.9★)",
                "Mike Johnson (4.7★)",
                "Sarah Williams (4.9★)",
                "David Brown (4.6★)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                runners
        );

        spinnerRunner.setAdapter(adapter);
    }

    private void setupDeliveryTimePicker() {
        editTextDeliveryTime.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    MakePickupRequestActivity.this,
                    (view, hourOfDay, selectedMinute) -> {
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, selectedMinute);

                        // Format the selected time
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        editTextDeliveryTime.setText(sdf.format(selectedTime.getTime()));
                    },
                    hour,
                    minute,
                    false // 12 hour format
            );

            timePickerDialog.show();
        });
    }
}
