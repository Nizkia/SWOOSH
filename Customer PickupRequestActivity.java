package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PickupRequestActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextInputEditText editTextPickupLocation;
    private TextInputEditText editTextDropOffLocation;
    private TextView textViewDefaultAddress;
    private MaterialButton buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_request);

        // Initialize views
        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupClickListeners();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        editTextPickupLocation = findViewById(R.id.editTextPickupLocation);
        editTextDropOffLocation = findViewById(R.id.editTextDropOffLocation);
        textViewDefaultAddress = findViewById(R.id.textViewDefaultAddress);
        buttonNext = findViewById(R.id.buttonNext);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigationDrawer() {
        // Setup navigation drawer item clicks
        TextView navEditProfile = findViewById(R.id.nav_edit_profile);
        TextView navDeliveryPreferences = findViewById(R.id.nav_delivery_preferences);
        TextView navFeedback = findViewById(R.id.nav_feedback);
        TextView navOrders = findViewById(R.id.nav_orders);  // Add this line
        TextView navSignOut = findViewById(R.id.nav_sign_out);

        navEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(PickupRequestActivity.this, EditProfileActivity.class);
            startActivity(intent);
            closeDrawer();
        });

        navDeliveryPreferences.setOnClickListener(v -> {
            Intent intent = new Intent(PickupRequestActivity.this, ManageDeliveryPreferencesActivity.class);
            startActivity(intent);
            closeDrawer();
        });

        navFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(PickupRequestActivity.this, FeedbackActivity.class);
            startActivity(intent);
            closeDrawer();
        });

        // Add this block for Orders navigation
        navOrders.setOnClickListener(v -> {
            Intent intent = new Intent(PickupRequestActivity.this, OrderActivity.class);
            startActivity(intent);
            closeDrawer();
        });

        navSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(PickupRequestActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            closeDrawer();
        });
    }

    private void setupClickListeners() {
        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(PickupRequestActivity.this, MakePickupRequestActivity.class);
            startActivity(intent);
        });
    }

    private void closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
