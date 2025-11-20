package com.example.swooshv2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private TextInputEditText editTextDescription;
    private RatingBar ratingBar;
    private MaterialButton buttonSubmit;
    private String userEmail, feedbackID;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        userEmail = getIntent().getStringExtra("UEmail");
        feedbackID = generateCustomID("F1", 4);
        initializeViews();
        setupToolbar();
        setupSubmitButton();
    }

    private void initializeViews() {
        editTextDescription = findViewById(R.id.editTextDescription);
        ratingBar = findViewById(R.id.ratingBar);
        buttonSubmit = findViewById(R.id.buttonSubmit);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupSubmitButton() {
        buttonSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                submitFeedback();
            }
        });
    }

    private boolean validateInput() {
        if (editTextDescription.getText().toString().trim().isEmpty()) {
            editTextDescription.setError("Description is required");
            return false;
        }

        if (ratingBar.getRating() == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void submitFeedback() {
        String description = editTextDescription.getText().toString().trim();
        float rating = ratingBar.getRating();

        // Get the current timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Check if user is logged in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in. Please log in to submit feedback.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore = FirebaseFirestore.getInstance();
        // Create a Feedback map
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("FeedbackID", feedbackID);
        feedback.put("Description", description);
        feedback.put("Rating", rating);
        feedback.put("Timestamp", timestamp);
        feedback.put("UEmail", userEmail);
        firestore.collection("Feedbacks").document(feedbackID).set(feedback);

        // Store feedback in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Feedbacks").add(feedback)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                    } else {
                        Toast.makeText(this, "Failed to submit feedback: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        Intent feedbackIntent = new Intent(FeedbackActivity.this, MainActivity.class);
        startActivity(feedbackIntent);
    }

    private String generateCustomID(String prefix, int numDigits) {
        int randomNum = (int) (Math.random() * Math.pow(10, numDigits)); // Generate a random number with the desired number of digits
        return String.format("%s%0" + numDigits + "d", prefix, randomNum);
    }

    private void clearFields() {
        editTextDescription.setText("");
        ratingBar.setRating(0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
