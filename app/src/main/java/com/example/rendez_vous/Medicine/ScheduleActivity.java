package com.example.rendez_vous.Medicine;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rendez_vous.AccessPoint.DatabaseHelper;
import com.example.rendez_vous.AccessPoint.LoginActivity;
import com.example.rendez_vous.Profile.EditProfileActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.SessionManager;

import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private SessionManager session; // 1. Added SessionManager
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this); // 2. Initialize SessionManager

        role = getIntent().getStringExtra("ROLE");
        if (role == null) role = "Secretary";

        recyclerView = findViewById(R.id.slotsRecyclerView);
        ImageButton btnAdd = findViewById(R.id.btnAddSlot);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();

        // 3. Add Profile Click Listener
        findViewById(R.id.profileIcon).setOnClickListener(this::showProfileMenu);

        btnAdd.setOnClickListener(v -> {
            boolean success = dbHelper.addAppointment(0, "Walk-in Patient", "25/12/2025", "10:00");
            if (success) {
                Toast.makeText(this, "Slot Added Successfully", Toast.LENGTH_SHORT).show();
                loadData();
            } else {
                Toast.makeText(this, "Slot/Time already taken!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // 4. Added the missing method
    private void showProfileMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_profile) {
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            } else if (id == R.id.action_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
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
        List<TimeSlot> slotList = dbHelper.getAppointments(role, 0);

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
                            loadData();
                        })
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);
    }
}