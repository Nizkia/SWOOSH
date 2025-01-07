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

public class TrackPickupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_pickup);

        // RecyclerView initialization
        RecyclerView recyclerViewDeliveries = findViewById(R.id.recyclerViewDeliveries);

        // Mock delivery data
        List<Delivery> deliveryList = new ArrayList<>();
        deliveryList.add(new Delivery("Order #1", "In Progress"));
        deliveryList.add(new Delivery("Order #2", "Delivered"));
        deliveryList.add(new Delivery("Order #3", "Pending"));
        deliveryList.add(new Delivery("Order #4", "Cancelled"));

        // Set up RecyclerView
        DeliveryAdapter adapter = new DeliveryAdapter(deliveryList);
        recyclerViewDeliveries.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDeliveries.setAdapter(adapter);
    }

    // Inner class for delivery data
    private static class Delivery {
        private final String title;
        private final String status;

        public Delivery(String title, String status) {
            this.title = title;
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getStatus() {
            return status;
        }
    }

    // Inner class for RecyclerView Adapter
    private static class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder> {

        private final List<Delivery> deliveryList;

        public DeliveryAdapter(List<Delivery> deliveryList) {
            this.deliveryList = deliveryList;
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
            Delivery delivery = deliveryList.get(position);
            holder.titleTextView.setText(delivery.getTitle());
            holder.statusTextView.setText(delivery.getStatus());
        }

        @Override
        public int getItemCount() {
            return deliveryList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView statusTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(android.R.id.text1);
                statusTextView = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
