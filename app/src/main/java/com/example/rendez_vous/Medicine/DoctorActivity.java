package com.example.rendez_vous.Medicine;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rendez_vous.AccessPoint.DatabaseHelper;
import com.example.rendez_vous.Profile.EditProfileActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.SessionManager;

import java.util.List;

public class DoctorActivity extends AppCompatActivity {
    DatabaseHelper db;
    SessionManager session;
    RecyclerView recyclerView;
    EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient); // Reuse shared layout

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Customize Header
        ((TextView)findViewById(R.id.welcomeText)).setText("Medicine Dashboard");

        // Hide Booking Card
        findViewById(R.id.cardBookAppointment).setVisibility(View.GONE);

        // Setup List
        recyclerView = findViewById(R.id.patientRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup Search Bar
        searchBar = findViewById(R.id.searchBar);
        searchBar.setHint("Search by Patient Name or Date...");
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Setup Profile Icon
        findViewById(R.id.profileIcon).setOnClickListener(this::showProfileMenu);

        // Initial Load
        updateList();
    }

    private void updateList() {
        // Load all appointments for "Medicine"
        bindAdapter(db.getAppointments("Medicine", 0));
    }

    private void filterList(String query) {
        if (query.isEmpty()) {
            updateList();
        } else {
            // Search Appointment Logic
            bindAdapter(db.searchAppointments(query));
        }
    }

    private void bindAdapter(List<TimeSlot> list) {
        // Pass "Medicine" role to Adapter so it shows UPDATE buttons
        recyclerView.setAdapter(new TimeSlotAdapter(list, "Medicine", (slot, action) -> {
            // Dialog for Confirming or Completing
            new AlertDialog.Builder(this)
                    .setTitle("Update Status")
                    .setMessage("Patient: " + slot.getPatientName())
                    .setPositiveButton("Confirm", (d, w) -> {
                        db.updateStatus(slot.getId(), "Confirmed");
                        // Refresh view maintaining search
                        filterList(searchBar.getText().toString());
                    })
                    .setNegativeButton("Complete", (d, w) -> {
                        db.updateStatus(slot.getId(), "Completed");
                        filterList(searchBar.getText().toString());
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        }));
    }

    private void showProfileMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        // Only standard options for Doctor
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
}