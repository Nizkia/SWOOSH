package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private ImageView imageViewLogo;
    private TextView textViewSwoosh;
    private TextView textViewCampusConvenience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        imageViewLogo = findViewById(R.id.imageViewLogo);
        textViewSwoosh = findViewById(R.id.textViewSwoosh);
        textViewCampusConvenience = findViewById(R.id.textViewCampusConvenience);

        // Set text
        textViewSwoosh.setText("Swoosh");
        textViewCampusConvenience.setText("Campus Convenience, Delivered");

        // Redirect after 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Create an Intent to redirect to PickupRequestActivity
                Intent intent = new Intent(HomeActivity.this, PickupRequestActivity.class);
                startActivity(intent);

                // Finish the current HomeActivity so the user can't return to it
                finish();
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }
}
