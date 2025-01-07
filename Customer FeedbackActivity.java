package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class FeedbackActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private AutoCompleteTextView feedbackTypeDropdown;
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private MaterialButton buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initializeViews();
        setupToolbar();
        setupFeedbackTypeDropdown();
        setupSubmitButton();
    }

    private void initializeViews() {
        ratingBar = findViewById(R.id.ratingBar);
        feedbackTypeDropdown = findViewById(R.id.feedbackTypeDropdown);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
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

    private void setupFeedbackTypeDropdown() {
        String[] feedbackTypes = new String[]{
                "General Feedback",
                "Bug Report",
                "Feature Request",
                "Service Issue",
                "Delivery Problem",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                feedbackTypes
        );

        feedbackTypeDropdown.setAdapter(adapter);
    }

    private void setupSubmitButton() {
        buttonSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                submitFeedback();
            }
        });
    }

    private boolean validateInput() {
        if (ratingBar.getRating() == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (feedbackTypeDropdown.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select feedback type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (editTextTitle.getText().toString().trim().isEmpty()) {
            editTextTitle.setError("Title is required");
            return false;
        }

        if (editTextDescription.getText().toString().trim().isEmpty()) {
            editTextDescription.setError("Description is required");
            return false;
        }

        return true;
    }

    private void submitFeedback() {
        // Here you would typically send the feedback to your backend
        String feedbackType = feedbackTypeDropdown.getText().toString();
        float rating = ratingBar.getRating();
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // For now, just show a success message and finish the activity
        Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
