package com.example.swooshv2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RunnerDashboardAdapter extends RecyclerView.Adapter<RunnerDashboardAdapter.ViewHolder> {

    private List<RunnerMenuItem> menuItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public RunnerDashboardAdapter(List<RunnerMenuItem> menuItems, OnItemClickListener listener) {
        this.menuItems = menuItems != null ? menuItems : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_runner_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RunnerMenuItem menuItem = menuItems.get(position);
        holder.tvCardTitle.setText(menuItem.getTitle());
        holder.ivCardIcon.setImageResource(menuItem.getIconResId());

        // Debug Log
        Log.d("RunnerDashboardAdapter", "Binding item: " + menuItem.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardTitle;
        ImageView ivCardIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardTitle = itemView.findViewById(R.id.tvCardTitle);
            ivCardIcon = itemView.findViewById(R.id.ivCardIcon);
        }
    }
}
