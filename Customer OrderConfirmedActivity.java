package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import android.content.Intent;
public class OrderConfirmedActivity extends AppCompatActivity {

    private TextView textPaymentMethod;
    private MaterialButton buttonBackToHome;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmed);

        // Initialize views
        textPaymentMethod = findViewById(R.id.textPaymentMethod);
        buttonBackToHome = findViewById(R.id.buttonBackToHome);
        toolbar = findViewById(R.id.toolbar);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Get payment method from intent
        String paymentMethod = getIntent().getStringExtra("payment_method");
        if (paymentMethod != null) {
            textPaymentMethod.setText(paymentMethod);
        }

        // Setup back to home button
        buttonBackToHome.setOnClickListener(v -> {
            // Clear activity stack and go to home
            finishAffinity();
            // If you have a specific home activity, start it here:
             Intent intent = new Intent(this, HomeActivity.class);
             startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
