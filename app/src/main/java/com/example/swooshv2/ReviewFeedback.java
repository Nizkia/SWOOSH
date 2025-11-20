package com.example.swooshv2;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewFeedback extends AppCompatActivity {

    private ListView lvFeedback;
    private FirebaseFirestore db; // Firestore database reference
    private ArrayAdapter<String> adapter;
    private List<String> feedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_feedback);

        lvFeedback = findViewById(R.id.lvFeedback);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the feedback list and adapter
        feedbackList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, feedbackList);
        lvFeedback.setAdapter(adapter);

        // Load feedback from Firestore
        loadFeedbackFromFirestore();
    }

    private void loadFeedbackFromFirestore() {
        db.collection("Feedbacks")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            feedbackList.clear(); // Clear the list before adding new data
                            for (DocumentSnapshot document : task.getResult()) {
                                try {
                                    // Extract fields from Firestore
                                    Double rating = document.getDouble("Rating");
                                    String description = document.getString("Description");
                                    String timestamp = document.getString("Timestamp"); // Timestamp stored as a string

                                    // Use the string timestamp as it is
                                    String formattedTimestamp = (timestamp != null && !timestamp.isEmpty()) ? timestamp : "N/A";

                                    // Combine fields into a displayable string
                                    String feedback = "Rating: " + (rating != null ? rating : "N/A") + "\n" +
                                            "Description: " + (description != null ? description : "N/A") + "\n" +
                                            "Timestamp: " + formattedTimestamp;

                                    feedbackList.add(feedback);
                                } catch (Exception e) {
                                    Log.e("ReviewFeedback", "Error parsing document: " + document.getId(), e);
                                }
                            }
                            adapter.notifyDataSetChanged(); // Refresh the ListView
                        } else {
                            Log.e("ReviewFeedback", "Error fetching data: ", task.getException());
                            Toast.makeText(ReviewFeedback.this, "Failed to load feedback.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}