package com.example.swooshv2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnGenerateReport = findViewById(R.id.btnGenerateReport);
        Button btnReviewFeedback = findViewById(R.id.btnReviewFeedback);
        Button btnCalculateRevenue = findViewById(R.id.btnCalculateRevenue);
        Button btnDistributeRevenue = findViewById(R.id.btnDistributeRevenue);

        // Navigate to Generate Report activity
        btnGenerateReport.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, GenerateReport.class);
            startActivity(intent);
        });

        // Navigate to Review Feedback activity
        btnReviewFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, ReviewFeedback.class);
            startActivity(intent);
        });

        // Navigate to Calculate Revenue activity
        btnCalculateRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, CalculateRevenue.class);
            startActivity(intent);
        });

        // Navigate to Distribute Revenue activity
        btnDistributeRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, DistributeRevenue.class);
            startActivity(intent);
        });

        Button btnGenerateGraph = findViewById(R.id.btnGenerateGraph);
        btnGenerateGraph.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, GenerateGraphActivity.class);
            startActivity(intent);
        });

    }
}