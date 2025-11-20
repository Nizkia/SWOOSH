package com.example.swooshv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListAvailableTasksActivity extends AppCompatActivity {

    private RecyclerView rvAvailableTasks;
    private TaskAdapter taskAdapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String runnerID; // RunnerID passed from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_available_tasks);

        rvAvailableTasks = findViewById(R.id.rvAvailableTasks);
        rvAvailableTasks.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with an empty list and set the click listener
        taskAdapter = new TaskAdapter(new ArrayList<>(), task -> {
            // Pass only PickupID and RunnerID to the next activity
            Intent intent = new Intent(this, TaskDetailsActivity.class);
            intent.putExtra("PickupID", task.getId());
            intent.putExtra("RunnerID", runnerID); // Pass RunnerID for context
            startActivity(intent);
        });

        rvAvailableTasks.setAdapter(taskAdapter);

        // Get RunnerID from intent
        runnerID = getIntent().getStringExtra("RunnerID");
        if (runnerID == null || runnerID.isEmpty()) {
            Log.e("ListAvailableTasks", "RunnerID is missing!");
        } else {
            Log.d("ListAvailableTasks", "RunnerID: " + runnerID);
        }

        // Load available tasks
        loadAvailableTasks();
    }

    private void loadAvailableTasks() {
        if (runnerID == null || runnerID.isEmpty()) {
            Toast.makeText(this, "RunnerID is missing. Cannot load tasks.", Toast.LENGTH_SHORT).show();
            Log.e("ListAvailableTasks", "RunnerID is missing or empty.");
            return;
        }
        db.collection("Pickups")
                .whereEqualTo("Status", "Assigned")
                .whereEqualTo("RunnerID", runnerID)// Filter only assigned tasks
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TaskModel> tasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Fetch and parse only required fields
                                String id = document.getId();
                                Timestamp timestamp = document.getTimestamp("DTPickupMade");
                                String dtPickupMade = (timestamp != null) ? timestamp.toDate().toString() : "Unknown Date";
                                String uEmail = document.getString("UEmail");
                                Long noOfItemsLong = document.getLong("NoOfItems");
                                int noOfItems = (noOfItemsLong != null) ? noOfItemsLong.intValue() : 0;
                                String status = document.getString("Status");

                                // Add to tasks list
                                tasks.add(new TaskModel(id, dtPickupMade, uEmail, noOfItems, status));
                                Log.d("ListAvailableTasks", "Task added: ID=" + id + ", Status=" + status);
                            } catch (Exception e) {
                                Log.e("ListAvailableTasks", "Error parsing task data: " + e.getMessage());
                            }
                        }

                        Log.d("ListAvailableTasks", "Total tasks loaded: " + tasks.size());
                        taskAdapter.updateTasks(tasks);
                    } else {
                        Toast.makeText(this, "Failed to load tasks.", Toast.LENGTH_SHORT).show();
                        Log.e("ListAvailableTasks", "Error loading tasks: " + task.getException().getMessage());
                    }
                });
    }
}
