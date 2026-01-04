package com.example.rendez_vous.Patient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rendez_vous.AccessPoint.Clinic;
import com.example.rendez_vous.R;
import java.util.List;

public class ClinicAdapter extends RecyclerView.Adapter<ClinicAdapter.ClinicViewHolder> {

    private List<Clinic> clinics;
    private OnClinicClickListener listener;

    public interface OnClinicClickListener {
        void onClinicClick(Clinic clinic);
    }

    public ClinicAdapter(List<Clinic> clinics, OnClinicClickListener listener) {
        this.clinics = clinics;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClinicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use your new custom layout here
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_selection, parent, false);
        return new ClinicViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ClinicViewHolder holder, int position) {
        Clinic clinic = clinics.get(position);
        holder.name.setText(clinic.getName());
        holder.location.setText(clinic.getLocation());

        // Set clinic-specific icon
        holder.icon.setImageResource(android.R.drawable.ic_menu_mapmode);

        holder.itemView.setOnClickListener(v -> listener.onClinicClick(clinic));
    }

    @Override
    public int getItemCount() {
        return clinics.size();
    }

    static class ClinicViewHolder extends RecyclerView.ViewHolder {
        TextView name, location;
        ImageView icon;

        ClinicViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.itemTitle);
            location = v.findViewById(R.id.itemSubtitle);
            icon = v.findViewById(R.id.itemIcon);
        }
    }
}