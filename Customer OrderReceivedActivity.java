package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

public class OrderReceivedActivity extends AppCompatActivity {
    private int currentRating = 0;
    private ImageView[] stars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_received);

        // Get order ID from intent
        String orderId = getIntent().getStringExtra("ORDER_ID");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set order ID in view
        TextView textViewOrderId = findViewById(R.id.textViewOrderId);
        textViewOrderId.setText("Order #" + orderId);

        // Setup stars
        setupStars();

        // Setup buttons
        setupButtons();
    }

    private void setupStars() {
        // Initialize stars array
        stars = new ImageView[5];
        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        // Set click listeners for each star
        for (int i = 0; i < stars.length; i++) {
            final int starPosition = i + 1;
            stars[i].setOnClickListener(v -> updateStars(starPosition));
        }
    }

    private void updateStars(int rating) {
        currentRating = rating;

        // Update all stars based on the rating
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                stars[i].setImageResource(android.R.drawable.btn_star_big_off);
            }
        }
    }

    private void setupButtons() {
        MaterialButton buttonSubmitRating = findViewById(R.id.buttonSubmitRating);
        MaterialButton buttonReturnHome = findViewById(R.id.buttonReturnHome);

        buttonSubmitRating.setOnClickListener(v -> {
            if (currentRating > 0) {
                // Navigate to rating success activity with the rating
                Intent intent = new Intent(OrderReceivedActivity.this, RatingSubmissionSuccessActivity.class);
                intent.putExtra("RATING", currentRating);
                startActivity(intent);
            }
        });

        buttonReturnHome.setOnClickListener(v -> {
            // Navigate to home screen
            Intent intent = new Intent(OrderReceivedActivity.this, PickupRequestActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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
