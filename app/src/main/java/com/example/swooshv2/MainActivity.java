package com.example.swooshv2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView tvWelcomeUser, tvRegister, tvAdminlogin;
    Button btnParcel, btnFood, btnRegRunner;
    ImageView ivLogout; // Logout icon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI elements
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        btnParcel = findViewById(R.id.btnParcel);
        btnFood = findViewById(R.id.btnFood);
        tvRegister = findViewById(R.id.tvRegister);
        tvAdminlogin = findViewById(R.id.tvAdminlogin);
        btnRegRunner = findViewById(R.id.btnRegisterRunner);
        ivLogout = findViewById(R.id.ivLogout); // Logout icon

        // Check if user is logged in
        if (!isUserLoggedIn()) {
            navigateToLogin(); // Redirect to login if not logged in
            return;
        }

        // Setup logout button
        ivLogout.setOnClickListener(v -> logoutUser());

        // Hide Register and Admin buttons if logged in
        toggleButtonsVisibility(isUserLoggedIn());

        // Handle Parcel Button
        btnParcel.setOnClickListener(v -> {
            if (isUserLoggedIn()) {
                Intent intent = new Intent(MainActivity.this, PickupRequestParcel.class);
                startActivity(intent);
            } else {
                navigateToLogin();
            }
        });

        btnFood.setOnClickListener(v -> {
            if (isUserLoggedIn()) {
                Intent intent = new Intent(MainActivity.this, PickupRequestFood.class);
                startActivity(intent);
            } else {
                navigateToLogin();
            }
        });

        // Register Runner Button
        btnRegRunner.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RunnerRegister.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh user session when returning to MainActivity
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            navigateToLogin(); // Redirect to login if session has ended
            return;
        }

        // Retrieve the username from SharedPreferences
        String userName = sharedPreferences.getString("UName", "User");
        tvWelcomeUser.setText("Hello, " + userName + "!");
        tvWelcomeUser.setVisibility(View.VISIBLE);
        ivLogout.setVisibility(View.VISIBLE);

        // Hide Register and Admin buttons if logged in
        toggleButtonsVisibility(true);
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false); // Default to false if no value found
    }

    private void navigateToLogin() {
        Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, CustomerLogin.class);
        startActivity(intent);
        finish(); // Close MainActivity to prevent returning
    }

    private void logoutUser() {
        // Clear session data
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Remove all saved data
        editor.apply();

        // Redirect to login screen
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void toggleButtonsVisibility(boolean isLoggedIn) {
        if (isLoggedIn) {
            tvRegister.setVisibility(View.GONE); // Hide Register button
            tvAdminlogin.setVisibility(View.GONE); // Hide Admin login button
        } else {
            tvRegister.setVisibility(View.VISIBLE); // Show Register button
            tvAdminlogin.setVisibility(View.VISIBLE); // Show Admin login button
        }
    }
}
