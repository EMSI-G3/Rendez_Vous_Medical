package com.example.rendez_vous.Medicine;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rendez_vous.R;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    private List<TimeSlot> slots;
    private String userRole;
    private OnSlotActionListener listener;

    public interface OnSlotActionListener { void onSlotAction(TimeSlot slot, String action); }

    public TimeSlotAdapter(List<TimeSlot> slots, String userRole, OnSlotActionListener listener) {
        this.slots = slots;
        this.userRole = userRole;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);

        holder.timeText.setText(slot.getTime());
        holder.statusText.setText(slot.getStatus());

        // Show Patient Name if Doctor/Secretary, otherwise show Date
        if(userRole.equals("Client")) {
            holder.dateText.setText(slot.getDate());
        } else {
            holder.dateText.setText(slot.getDate() + " - " + slot.getPatientName());
        }

        // Status Colors
        if(slot.getStatus().equals("Confirmed")) holder.statusText.setTextColor(Color.parseColor("#4CAF50")); // Green
        else if(slot.getStatus().equals("Pending")) holder.statusText.setTextColor(Color.parseColor("#FF9800")); // Orange
        else holder.statusText.setTextColor(Color.GRAY);

        // Button Logic
        if(userRole.equals("Client")) {
            holder.actionButton.setVisibility(View.GONE);// Clients can't modify list directly here

        }
        else if (userRole.equals("Doctor")) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setText("UPDATE");
            holder.actionButton.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
            holder.actionButton.setOnClickListener(v -> listener.onSlotAction(slot, "update"));
        }
        else if (userRole.equals("Secretary")) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setText("DELETE");
            holder.actionButton.setBackgroundColor(Color.parseColor("#F44336")); // Red
            holder.actionButton.setOnClickListener(v -> listener.onSlotAction(slot, "delete"));
        }
    }

    @Override public int getItemCount() { return slots.size(); }

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