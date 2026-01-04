package com.example.rendez_vous.Medicine;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rendez_vous.AccessPoint.DatabaseHelper;
import com.example.rendez_vous.Profile.EditProfileActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.SessionManager;

import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private String role;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Fetch user info from session
        role = session.getUserDetails().get("role");
        String email = session.getUserDetails().get("email");
        currentUserId = dbHelper.getUserId(email);

        if (role == null) role = "Secretary";

        recyclerView = findViewById(R.id.slotsRecyclerView);
        ImageButton btnAdd = findViewById(R.id.btnAddSlot);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();

        findViewById(R.id.profileIcon).setOnClickListener(this::showProfileMenu);

        btnAdd.setOnClickListener(v -> {
            // FIX: addAppointment now needs patientId, doctorId, and clinicId.
            // For a 'Walk-in', we use 0 for patientId.
            // Here we assume Doctor ID 1 and Clinic ID 1 for testing purposes.
            boolean success = dbHelper.addAppointment(0, 1, 1, "Walk-in Patient", "25/12/2025", "10:00");

            if (success) {
                Toast.makeText(this, "Slot Added Successfully", Toast.LENGTH_SHORT).show();
                loadData();
            } else {
                Toast.makeText(this, "Slot/Time already taken or Doctor unavailable!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void showProfileMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_profile) {
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            } else if (id == R.id.action_logout) {
                session.logoutUser();
                finish();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void loadData() {
        // FIX: Using currentUserId from session so the DB knows which appointments to show
        List<TimeSlot> slotList = dbHelper.getAppointments(role, currentUserId);

        TimeSlotAdapter adapter = new TimeSlotAdapter(slotList, role, (slot, action) -> {
            if (action.equals("delete")) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Appointment")
                        .setMessage("Remove appointment for " + slot.getPatientName() + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.deleteAppointment(slot.getId());
                            loadData();
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else if (action.equals("update")) {
                new AlertDialog.Builder(this)
                        .setTitle("Update Status")
                        .setItems(new String[]{"Confirm", "Complete"}, (dialog, which) -> {
                            String newStatus = (which == 0) ? "Confirmed" : "Completed";
                            dbHelper.updateStatus(slot.getId(), newStatus);

                            if (newStatus.equals("Completed")) {
                                sendStatusSMS(slot.getId(), "Completed");
                            }
                            loadData();
                        })
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void sendStatusSMS(int appointmentId, String status) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
            return;
        }

        String phoneNumber = dbHelper.getPatientPhoneByAppointmentId(appointmentId);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                String message = "Rendez-Vous: Your appointment (ID: " + appointmentId + ") is now " + status + ".";
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "Notification SMS sent.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "SMS Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}