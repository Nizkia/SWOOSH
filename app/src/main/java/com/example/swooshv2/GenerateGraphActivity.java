package com.example.swooshv2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class GenerateGraphActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private BarChart barChart;
    private Button generateGraphButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_earnings_graph);

        // Initialize Firestore and views
        db = FirebaseFirestore.getInstance();
        barChart = findViewById(R.id.barChart);
        generateGraphButton = findViewById(R.id.btnGenerateGraph);

        // Set up the button click listener to generate the graph
        generateGraphButton.setOnClickListener(v -> fetchDataAndGenerateGraph());
    }

    private void fetchDataAndGenerateGraph() {
        // Fetch data from Firestore for parcels and food collected
        db.collection("Pickups")
                .whereEqualTo("Status", "Delivered")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            ArrayList<BarEntry> entries = new ArrayList<>();
                            ArrayList<String> labels = new ArrayList<>();
                            int parcelCount = 0;
                            int foodCount = 0;

                            // Process the documents and calculate totals
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String pickupID = document.getId(); // Assuming PickupID is the document ID
                                if (pickupID != null && pickupID.length() > 2) {
                                    char categoryChar = pickupID.charAt(2); // Get the third character
                                    if (categoryChar == 'F') {
                                        foodCount++;
                                    } else if (categoryChar == 'P') {
                                        parcelCount++;
                                    }
                                }
                            }

                            // Prepare data for the bar chart
                            entries.add(new BarEntry(0, parcelCount));  // First entry for parcels
                            entries.add(new BarEntry(1, foodCount));    // Second entry for food
                            labels.add("Parcels");
                            labels.add("Food");

                            // Generate the bar chart with the fetched data
                            generateBarChart(entries, labels);
                        } else {
                            Toast.makeText(GenerateGraphActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(GenerateGraphActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void generateBarChart(ArrayList<BarEntry> entries, ArrayList<String> labels) {
        // Create a dataset from the entries
        BarDataSet dataSet = new BarDataSet(entries, "Collected Items");
        dataSet.setColor(getResources().getColor(R.color.darkBlue));  // Customize the color

        // Create BarData from the dataset and set it on the chart
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Customize the appearance of the chart
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setGranularity(1f);  // Set granularity so only labels appear
        barChart.getDescription().setText("Collected Items");  // Set description
        barChart.invalidate();  // Refresh the chart with the new data
    }
}
