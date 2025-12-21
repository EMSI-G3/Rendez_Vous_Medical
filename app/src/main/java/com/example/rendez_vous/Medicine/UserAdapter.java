package com.example.rendez_vous.Medicine;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rendez_vous.AccessPoint.User;
import com.example.rendez_vous.R;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> users;
    private OnUserActionListener listener;

    public interface OnUserActionListener { void onAction(User user); }

    public UserAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We reuse item_time_slot.xml for simplicity, or you can create item_user.xml
        // Here we map fields to existing IDs in item_time_slot.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        // Mapping User data to the layout views
        holder.nameText.setText(user.getFullname());  // Reuse dateText for Name
        holder.emailText.setText(user.getEmail());    // Reuse timeText for Email
        holder.roleText.setText(user.getRole().toUpperCase()); // Reuse statusText

        // Styling
        holder.roleText.setTextColor(Color.parseColor("#009688"));
        holder.btnDelete.setText("REMOVE");
        holder.btnDelete.setBackgroundColor(Color.parseColor("#F44336"));
        holder.btnDelete.setVisibility(View.VISIBLE);

        holder.btnDelete.setOnClickListener(v -> listener.onAction(user));
    }

    @Override public int getItemCount() { return users.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, roleText;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Reusing IDs from item_time_slot.xml
            nameText = itemView.findViewById(R.id.dateText);
            emailText = itemView.findViewById(R.id.timeText);
            roleText = itemView.findViewById(R.id.statusText);
            btnDelete = itemView.findViewById(R.id.actionButton);
        }
    }
}