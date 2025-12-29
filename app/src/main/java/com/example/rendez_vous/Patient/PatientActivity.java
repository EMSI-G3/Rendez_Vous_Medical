package com.example.rendez_vous.Patient;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent; // Needed for intent
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rendez_vous.AccessPoint.DatabaseHelper;
import com.example.rendez_vous.AccessPoint.LoginActivity; // Needed for Logout redirection
import com.example.rendez_vous.Medicine.TimeSlotAdapter;
import com.example.rendez_vous.Profile.EditProfileActivity; // Needed for Edit Profile
import com.example.rendez_vous.R;
import com.example.rendez_vous.SessionManager;

import java.util.Calendar;
import java.util.Locale;

public class PatientActivity extends AppCompatActivity {
    DatabaseHelper db;
    SessionManager session;
    RecyclerView recyclerView;
    String selectedDate = "", selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        // --- LOGIC TO FETCH IMAGE BY ID ---
        String userEmail = session.getUserDetails().get(SessionManager.KEY_EMAIL); // Get current user's email

        if (userEmail != null) {
            // 1. Use your existing getUserId method
            int currentUserId = db.getUserId(userEmail);

            if (currentUserId != -1) {
                // 2. Fetch the image bytes using the ID
                byte[] imageBytes = db.getUserProfileImage(currentUserId);

                if (imageBytes != null) {
                    ImageView profileImageView = findViewById(R.id.patientProfileImage);

                    // 3. Convert bytes to Bitmap and display
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    profileImageView.setImageBitmap(bitmap);

                    // 4. Clean up the UI (Remove default tint and padding)
                    profileImageView.setImageTintList(null);
                    profileImageView.setPadding(0, 0, 0, 0);
                }
            }
        }
        // ----------------------------------

        recyclerView = findViewById(R.id.patientRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateList();

        findViewById(R.id.cardBookAppointment).setOnClickListener(v -> showBookingDialog());
        findViewById(R.id.profileIcon).setOnClickListener(this::showProfileMenu);
    }

    private void loadProfilePicture(int userId, ImageView imageView) {
        byte[] imageBytes = db.getUserProfileImage(userId);

        if (imageBytes != null && imageBytes.length > 0) {
            // Decode the byte array into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(bitmap);

            // Remove the default tint and padding so the photo looks good
            imageView.setImageTintList(null);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            // If no image, keep the default calendar icon or set a placeholder
            imageView.setImageResource(android.R.drawable.ic_menu_my_calendar);
        }
    }

    // --- NEW: Method to show the Popup Menu ---
    private void showProfileMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_profile) {
                // Open Edit Profile
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            }
            else if (id == R.id.action_settings) {
                Toast.makeText(this, "Settings (Not Implemented)", Toast.LENGTH_SHORT).show();
                return true;
            }
            else if (id == R.id.action_logout) {
                // Logout Logic
                session.logoutUser(); // Clears session and redirects to Login
                finish();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void updateList() {
        String email = session.getUserDetails().get(SessionManager.KEY_EMAIL);
        int uid = db.getUserId(email);
        // Pass "Client" so adapter hides Delete/Update buttons
        recyclerView.setAdapter(new TimeSlotAdapter(db.getAppointments("Client", uid), "Client", null));
    }

    private void showBookingDialog() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.activity_patient_dialog);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvDate = d.findViewById(R.id.dialogDate);
        TextView tvTime = d.findViewById(R.id.dialogTime);

        // Date Picker
        tvDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, day) -> {
                selectedDate = day + "/" + (m + 1) + "/" + y;
                tvDate.setText(selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time Picker
        tvTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                tvTime.setText(selectedTime);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        d.findViewById(R.id.dialogBtnConfirm).setOnClickListener(v -> {
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Select Date & Time", Toast.LENGTH_SHORT).show();
                return;
            }

            // GET USER ID & NAME
            String email = session.getUserDetails().get(SessionManager.KEY_EMAIL);
            int uid = db.getUserId(email);
            String uName = db.getUserName(email);

            boolean success = db.addAppointment(uid, uName, selectedDate, selectedTime);
            if (success) {
                Toast.makeText(this, "Booked Successfully!", Toast.LENGTH_SHORT).show();
                updateList();
                d.dismiss();
            } else {
                Toast.makeText(this, "Slot already occupied!", Toast.LENGTH_LONG).show();
            }
        });
        d.show();
    }
}