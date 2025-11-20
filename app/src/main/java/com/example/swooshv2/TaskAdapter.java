package com.example.swooshv2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskModel> tasks;
    private OnTaskAcceptListener listener;

    public interface OnTaskAcceptListener {
        void onTaskAccept(TaskModel task);
    }

    public TaskAdapter(List<TaskModel> tasks, OnTaskAcceptListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void updateTasks(List<TaskModel> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = tasks.get(position);

        // Bind task details
        holder.tvDocumentID.setText("ID: " + task.getId());
        holder.tvDTPickupMade.setText("Pickup Date: " + task.getDtPickupMade()); // Bind the date
        holder.tvUEmail.setText("Email: " + task.getUEmail());
        holder.tvNoOfItems.setText("Items: " + task.getNoOfItems());
        holder.tvStatus.setText("Status: " + task.getStatus());

        // Handle "Accept" button click
        holder.btnAcceptTask.setOnClickListener(v -> listener.onTaskAccept(task));
        Log.d("TaskAdapter", "Binding task: " + task.getId() + ", Date: " + task.getDtPickupMade());

    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocumentID, tvDTPickupMade, tvUEmail, tvNoOfItems, tvStatus;
        Button btnAcceptTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDocumentID = itemView.findViewById(R.id.tvDocumentID);
            tvDTPickupMade = itemView.findViewById(R.id.tvDTPickupMade);
            tvUEmail = itemView.findViewById(R.id.tvUEmail);
            tvNoOfItems = itemView.findViewById(R.id.tvNoOfItems);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAcceptTask = itemView.findViewById(R.id.btnAcceptTask);
        }
    }
}
