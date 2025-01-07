package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SelectRunnerActivity extends AppCompatActivity {

    private ListView listViewRunners;
    private Button buttonConfirmRunner;
    private String selectedRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_runner);

        // Initialize views
        listViewRunners = findViewById(R.id.listViewRunners);
        buttonConfirmRunner = findViewById(R.id.buttonConfirmRunner);

        // Mock runner data
        String[] runners = {"Runner A", "Runner B", "Runner C", "Runner D", "Runner E"};

        // Set up ListView with runners
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, runners);
        listViewRunners.setAdapter(adapter);
        listViewRunners.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Handle runner selection
        listViewRunners.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedRunner = runners[position];
                Toast.makeText(SelectRunnerActivity.this, selectedRunner + " selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Confirm button listener
        buttonConfirmRunner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedRunner != null) {
                    Toast.makeText(SelectRunnerActivity.this, "You confirmed: " + selectedRunner, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SelectRunnerActivity.this, "Please select a runner first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
