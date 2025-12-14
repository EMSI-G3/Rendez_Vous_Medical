package com.example.rendez_vous;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    private List<TimeSlot> slots;
    private String userRole;

    public TimeSlotAdapter(List<TimeSlot> slots, String userRole) {
        this.slots = slots;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);

        holder.timeText.setText(slot.getTime());
        holder.dateText.setText(slot.getDate());
        holder.statusText.setText(slot.getStatus());

        // Logic based on Role and Status
        if (slot.getStatus().equals("Available")) {
            holder.statusText.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.actionButton.setVisibility(View.VISIBLE);

            if ("Client".equals(userRole)) {
                holder.actionButton.setText("Book");
            } else {
                holder.actionButton.setText("Delete"); // Medicine/Secretary can remove
                holder.actionButton.setBackgroundColor(Color.parseColor("#F44336")); // Red
            }
        } else {
            holder.statusText.setTextColor(Color.GRAY);
            holder.actionButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, dateText, statusText;
        Button actionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeText);
            dateText = itemView.findViewById(R.id.dateText);
            statusText = itemView.findViewById(R.id.statusText);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}