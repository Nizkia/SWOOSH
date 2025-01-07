package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class ManageDeliveryPreferencesActivity extends AppCompatActivity {

    private TextInputEditText editTextDefaultAddress;
    private SwitchMaterial switchDeliveryUpdates;
    private SwitchMaterial switchPromotions;
    private SwitchMaterial switchSMS;
    private AutoCompleteTextView timePreferenceDropdown;
    private MaterialButton buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_preferences);

        initializeViews();
        setupToolbar();
        setupTimePreferenceDropdown();
        loadSavedPreferences();
        setupSaveButton();
    }

    private void initializeViews() {
        editTextDefaultAddress = findViewById(R.id.editTextDefaultAddress);
        switchDeliveryUpdates = findViewById(R.id.switchDeliveryUpdates);
        switchPromotions = findViewById(R.id.switchPromotions);
        switchSMS = findViewById(R.id.switchSMS);
        timePreferenceDropdown = findViewById(R.id.timePreferenceDropdown);
        buttonSave = findViewById(R.id.buttonSave);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupTimePreferenceDropdown() {
        String[] timeSlots = new String[]{
                "Morning (9AM - 12PM)",
                "Afternoon (12PM - 3PM)",
                "Evening (3PM - 6PM)",
                "Night (6PM - 9PM)",
                "Any Time"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                timeSlots
        );

        timePreferenceDropdown.setAdapter(adapter);
    }

    private void loadSavedPreferences() {
        // Here you would typically load saved preferences from SharedPreferences
        // For now, we'll set some default values
        editTextDefaultAddress.setText("123 University Road, Campus City");
        switchDeliveryUpdates.setChecked(true);
        switchPromotions.setChecked(false);
        switchSMS.setChecked(true);
        timePreferenceDropdown.setText("Any Time");
    }

    private void savePreferences() {
        // Here you would typically save to SharedPreferences
        String address = editTextDefaultAddress.getText().toString().trim();
        boolean deliveryUpdates = switchDeliveryUpdates.isChecked();
        boolean promotions = switchPromotions.isChecked();
        boolean sms = switchSMS.isChecked();
        String timePreference = timePreferenceDropdown.getText().toString();

        // For now, just show a success message
        Toast.makeText(this, "Preferences saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupSaveButton() {
        buttonSave.setOnClickListener(v -> {
            if (validateInput()) {
                savePreferences();
            }
        });
    }

    private boolean validateInput() {
        if (editTextDefaultAddress.getText().toString().trim().isEmpty()) {
            editTextDefaultAddress.setError("Default address is required");
            return false;
        }

        if (timePreferenceDropdown.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select preferred delivery time", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
