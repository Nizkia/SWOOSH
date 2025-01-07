package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class OrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Setup menu buttons
        setupMenuButtons();
    }

    private void setupMenuButtons() {
        ImageButton menuButton111 = findViewById(R.id.menuButton111);
        ImageButton menuButton112 = findViewById(R.id.menuButton112);
        ImageButton menuButton113 = findViewById(R.id.menuButton113);

        View.OnClickListener menuClickListener = v -> {
            String orderId = "";
            int id = v.getId();
            if (id == R.id.menuButton111) {
                orderId = "111";
            } else if (id == R.id.menuButton112) {
                orderId = "112";
            } else if (id == R.id.menuButton113) {
                orderId = "113";
            }

            // Changed to navigate to TrackDeliveryActivity instead of PickupRequestActivity
            Intent intent = new Intent(OrderActivity.this, TrackDeliveryActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            startActivity(intent);
        };

        menuButton111.setOnClickListener(menuClickListener);
        menuButton112.setOnClickListener(menuClickListener);
        menuButton113.setOnClickListener(menuClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
