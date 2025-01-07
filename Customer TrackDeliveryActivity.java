package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
public class TrackDeliveryActivity extends AppCompatActivity {
    private GoogleMap mMap;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_delivery);

        // Get order ID from intent
        orderId = getIntent().getStringExtra("ORDER_ID");

        // Setup toolbar
        setupToolbar();

        // Setup map
        setupMap();

        // Setup call button
        setupCallButton();

        // Setup order received button
        setupOrderReceivedButton();
    }

    private void setupOrderReceivedButton() {
        MaterialButton buttonOrderReceived = findViewById(R.id.buttonOrderReceived);
        buttonOrderReceived.setOnClickListener(v -> {
            Intent intent = new Intent(TrackDeliveryActivity.this, OrderReceivedActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            startActivity(intent);
            finish(); // This will close the tracking page after confirming receipt
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView textViewOrderId = findViewById(R.id.textViewOrderId);
        textViewOrderId.setText("Order #" + orderId);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;

            // UiTM Shah Alam coordinates
            LatLng uitm = new LatLng(3.0733, 101.5185);
            mMap.addMarker(new MarkerOptions().position(uitm).title("Delivery Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uitm, 15));
        });
    }

    private void setupCallButton() {
        ImageButton buttonCall = findViewById(R.id.buttonCall);
        buttonCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+60123456789"));
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
