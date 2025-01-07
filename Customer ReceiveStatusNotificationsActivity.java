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

public class ReceiveStatusNotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_status_notifications);

        // RecyclerView initialization
        RecyclerView recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);

        // Mock notifications data
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(new Notification("Order #1", "Your order has been picked up."));
        notificationList.add(new Notification("Order #2", "Your order is on the way."));
        notificationList.add(new Notification("Order #3", "Your order has been delivered."));
        notificationList.add(new Notification("Order #4", "Delivery has been cancelled."));

        // Set up RecyclerView
        NotificationAdapter adapter = new NotificationAdapter(notificationList);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(adapter);
    }

    // Inner class for notification data
    private static class Notification {
        private final String title;
        private final String message;

        public Notification(String title, String message) {
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
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        private final List<Notification> notificationList;

        public NotificationAdapter(List<Notification> notificationList) {
            this.notificationList = notificationList;
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
            Notification notification = notificationList.get(position);
            holder.titleTextView.setText(notification.getTitle());
            holder.messageTextView.setText(notification.getMessage());
        }

        @Override
        public int getItemCount() {
            return notificationList.size();
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
