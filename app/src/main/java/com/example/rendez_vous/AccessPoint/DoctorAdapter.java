package com.example.rendez_vous.AccessPoint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rendez_vous.R;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors;
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_selection, parent, false);
        return new DoctorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doc = doctors.get(position);

        holder.tvName.setText(doc.getName());
        holder.tvSpecialty.setText(doc.getSpecialty());

        // --- IMAGE & TINT LOADING LOGIC ---
        byte[] imageBytes = doc.getProfilePic();

        if (imageBytes != null && imageBytes.length > 0) {
            // 1. Decode and set the doctor's photo
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.imgIcon.setImageBitmap(bitmap);
            holder.imgIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // 2. CRITICAL: Remove the tint so the face is visible in natural colors
            holder.imgIcon.setImageTintList(null);

        } else {
            // 3. Set the default icon
            holder.imgIcon.setImageResource(android.R.drawable.ic_menu_my_calendar);
            holder.imgIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            // 4. Re-apply the teal tint for the default icon (matching your brand)
            holder.imgIcon.setImageTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#009688")
            ));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDoctorClick(doc);
        });
    }

    @Override
    public int getItemCount() { return doctors.size(); }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpecialty;
        ImageView imgIcon;

        DoctorViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.itemTitle);
            tvSpecialty = v.findViewById(R.id.itemSubtitle);
            imgIcon = v.findViewById(R.id.itemIcon);
        }
    }
}