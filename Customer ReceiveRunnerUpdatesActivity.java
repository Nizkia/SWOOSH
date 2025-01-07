package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReceiveRunnerUpdatesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_runner_updates);

        // RecyclerView initialization
        RecyclerView recyclerViewRunnerUpdates = findViewById(R.id.recyclerViewRunnerUpdates);

        // Mock runner updates data
        List<RunnerUpdate> runnerUpdatesList = new ArrayList<>();
        runnerUpdatesList.add(new RunnerUpdate("Update #1", "Runner is on the way."));
        runnerUpdatesList.add(new RunnerUpdate("Update #2", "Runner has reached the pickup point."));
        runnerUpdatesList.add(new RunnerUpdate("Update #3", "Runner has picked up the package."));
        runnerUpdatesList.add(new RunnerUpdate("Update #4", "Runner is approaching the delivery location."));

        // Set up RecyclerView
        RunnerUpdateAdapter adapter = new RunnerUpdateAdapter(runnerUpdatesList);
        recyclerViewRunnerUpdates.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRunnerUpdates.setAdapter(adapter);
    }

    // Inner class for runner update data
    private static class RunnerUpdate {
        private final String title;
        private final String message;

        public RunnerUpdate(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }
    }

    // Inner class for RecyclerView Adapter
    private static class RunnerUpdateAdapter extends RecyclerView.Adapter<RunnerUpdateAdapter.ViewHolder> {

        private final List<RunnerUpdate> runnerUpdatesList;

        public RunnerUpdateAdapter(List<RunnerUpdate> runnerUpdatesList) {
            this.runnerUpdatesList = runnerUpdatesList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RunnerUpdate runnerUpdate = runnerUpdatesList.get(position);
            holder.titleTextView.setText(runnerUpdate.getTitle());
            holder.messageTextView.setText(runnerUpdate.getMessage());
        }

        @Override
        public int getItemCount() {
            return runnerUpdatesList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView messageTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(android.R.id.text1);
                messageTextView = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
