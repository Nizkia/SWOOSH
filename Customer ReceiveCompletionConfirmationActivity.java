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

public class ReceiveCompletionConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_completion_confirmation);

        // RecyclerView initialization
        RecyclerView recyclerViewConfirmations = findViewById(R.id.recyclerViewConfirmations);

        // Mock confirmation data
        List<Confirmation> confirmationList = new ArrayList<>();
        confirmationList.add(new Confirmation("Order #1", "Delivery completed successfully."));
        confirmationList.add(new Confirmation("Order #2", "Package delivered to your location."));
        confirmationList.add(new Confirmation("Order #3", "Food delivery confirmed."));
        confirmationList.add(new Confirmation("Order #4", "Runner marked the delivery as complete."));

        // Set up RecyclerView
        ConfirmationAdapter adapter = new ConfirmationAdapter(confirmationList);
        recyclerViewConfirmations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewConfirmations.setAdapter(adapter);
    }

    // Inner class for confirmation data
    private static class Confirmation {
        private final String title;
        private final String message;

        public Confirmation(String title, String message) {
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
    private static class ConfirmationAdapter extends RecyclerView.Adapter<ConfirmationAdapter.ViewHolder> {

        private final List<Confirmation> confirmationList;

        public ConfirmationAdapter(List<Confirmation> confirmationList) {
            this.confirmationList = confirmationList;
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
            Confirmation confirmation = confirmationList.get(position);
            holder.titleTextView.setText(confirmation.getTitle());
            holder.messageTextView.setText(confirmation.getMessage());
        }

        @Override
        public int getItemCount() {
            return confirmationList.size();
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
