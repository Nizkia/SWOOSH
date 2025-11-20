package com.example.swooshv2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RevenueAdapter extends RecyclerView.Adapter<RevenueAdapter.RevenueViewHolder> {

    private List<Revenue> revenueList;

    public RevenueAdapter(List<Revenue> revenueList) {
        this.revenueList = revenueList;
    }

    @NonNull
    @Override
    public RevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_revenue, parent, false);
        return new RevenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RevenueViewHolder holder, int position) {
        Revenue revenue = revenueList.get(position);
        holder.date.setText(revenue.getDate());
        holder.amount.setText("RM" + revenue.getAmount());
    }

    @Override
    public int getItemCount() {
        return revenueList.size();
    }

    public void updateData(List<Revenue> newRevenueList) {
        this.revenueList.clear();
        this.revenueList.addAll(newRevenueList);
        notifyDataSetChanged();
    }


    public static class RevenueViewHolder extends RecyclerView.ViewHolder {
        TextView date, amount;

        public RevenueViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tvDate);
            amount = itemView.findViewById(R.id.tvAmount);
        }
    }
}