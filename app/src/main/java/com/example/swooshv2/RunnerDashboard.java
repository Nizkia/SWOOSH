package com.example.swooshv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RunnerDashboard extends AppCompatActivity {

    private RecyclerView rvRunnerDashboard;
    private RunnerDashboardAdapter adapter;
    private String runnerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_dashboard);

        // Initialize RecyclerView for menu
        rvRunnerDashboard = findViewById(R.id.rvRunnerDashboard);
        runnerID = getIntent().getStringExtra("RunnerID");

        // Create a list of menu items
        List<RunnerMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new RunnerMenuItem("See Profile", R.drawable.ic_profile));
        menuItems.add(new RunnerMenuItem("Set Availability", R.drawable.ic_availability));
        menuItems.add(new RunnerMenuItem("View Assigned Task", R.drawable.ic_tasks));
        menuItems.add(new RunnerMenuItem("See My Completed Pickups", R.drawable.ic_completed));
        menuItems.add(new RunnerMenuItem("My Earnings", R.drawable.ic_earnings));

        for (RunnerMenuItem item : menuItems) {
            Log.d("RunnerDashboard", "Menu item: " + item.getTitle());
        }

        // Set up the RecyclerView and Adapter
        adapter = new RunnerDashboardAdapter(menuItems, position -> {
            // Handle menu item clicks
            switch (position) {
                case 0: // See Profile
                    Intent profileIntent = new Intent(RunnerDashboard.this, RunnerProfile.class);
                    profileIntent.putExtra("RunnerID", runnerID);
                    startActivity(profileIntent);
                    break;

                case 1: // Set Availability
                    Intent availabilityIntent = new Intent(RunnerDashboard.this, RunnerSetAv.class);
                    availabilityIntent.putExtra("RunnerID", runnerID);
                    startActivity(availabilityIntent);
                    break;

                case 2: // View Assigned Task
                    Intent taskIntent = new Intent(RunnerDashboard.this, ListAvailableTasksActivity.class);
                    taskIntent.putExtra("RunnerID", runnerID);
                    Log.d("AdminDashboard", "RunnerID: " + runnerID);
                    startActivity(taskIntent);
                    break;

                case 3: // See My Completed Pickups
                    Intent comIntent = new Intent(RunnerDashboard.this, RunnerComplete.class);
                    comIntent.putExtra("RunnerID", runnerID);
                    Log.d("AdminDashboard", "RunnerID: " + runnerID);
                    startActivity(comIntent);
                    break;

                case 4: // My Earnings
                    Intent earnIntent = new Intent(RunnerDashboard.this, RunnerEarns.class);
                    earnIntent.putExtra("RunnerID", runnerID);
                    Log.d("AdminDashboard", "RunnerID: " + runnerID);
                    startActivity(earnIntent);
                    break;
            }
        });

        rvRunnerDashboard.setLayoutManager(new LinearLayoutManager(this));
        rvRunnerDashboard.setAdapter(adapter);
    }
}
