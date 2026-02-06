package com.example.rendez_vous.Patient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rendez_vous.AccessPoint.Specialty;
import com.example.rendez_vous.R;
import java.util.List;

public class SpecialtyAdapter extends RecyclerView.Adapter<SpecialtyAdapter.SpecialtyViewHolder> {

    private List<Specialty> specialties;
    private OnSpecialtyClickListener listener;

    public interface OnSpecialtyClickListener {
        void onSpecialtyClick(Specialty specialty);
    }

    public SpecialtyAdapter(List<Specialty> specialties, OnSpecialtyClickListener listener) {
        this.specialties = specialties;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpecialtyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_selection, parent, false);
        return new SpecialtyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialtyViewHolder holder, int position) {
        Specialty specialty = specialties.get(position);
        holder.name.setText(specialty.getName());
        holder.subtitle.setText("Find specialized doctors");
        holder.icon.setImageResource(android.R.drawable.ic_menu_agenda);

        holder.itemView.setOnClickListener(v -> listener.onSpecialtyClick(specialty));
    }

    @Override
    public int getItemCount() { return specialties.size(); }

    static class SpecialtyViewHolder extends RecyclerView.ViewHolder {
        TextView name, subtitle;
        ImageView icon;

        SpecialtyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.itemTitle);
            subtitle = v.findViewById(R.id.itemSubtitle);
            icon = v.findViewById(R.id.itemIcon);
        }
    }
}