package com.example.rendez_vous;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity implements TimeSlotAdapter.OnSlotActionListener {

    private RecyclerView recyclerView;
    private TimeSlotAdapter adapter;
    private List<TimeSlot> slotList;
    private DatabaseHelper dbHelper;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Initialize DB
        dbHelper = new DatabaseHelper(this);

        // Get Role (Secretary or Medicine or Client)
        role = getIntent().getStringExtra("ROLE");
        // Default role for testing if intent is null
        if (role == null) role = "Secretary";

        recyclerView = findViewById(R.id.slotsRecyclerView);
        ImageButton btnAdd = findViewById(R.id.btnAddSlot);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initial Load of Data
        loadData();

        btnAdd.setOnClickListener(v -> {
            // ADD SLOT LOGIC
            // In a real app, you would open a Dialog to pick Date/Time.
            // Here, we add a dummy slot for demonstration.
            TimeSlot newSlot = new TimeSlot("12 Oct", "14:00 - 14:30", "Available");
            boolean success = dbHelper.addSlot(newSlot);

            if (success) {
                Toast.makeText(this, "Slot Added Successfully", Toast.LENGTH_SHORT).show();
                loadData(); // Refresh list from DB
            } else {
                Toast.makeText(this, "Failed to Add Slot", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        // Fetch all data from SQLite
        slotList = dbHelper.getAllSlots();

        // Set Adapter with the new list and 'this' as the listener
        adapter = new TimeSlotAdapter(slotList, role, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSlotAction(TimeSlot slot, String action) {
        if ("book".equals(action)) {
            // Client is booking the slot
            dbHelper.updateSlotStatus(slot.getId(), "Booked");
            Toast.makeText(this, "Appointment Booked!", Toast.LENGTH_SHORT).show();
        } else if ("delete".equals(action)) {
            // Admin/Secretary is deleting the slot
            dbHelper.deleteSlot(slot.getId());
            Toast.makeText(this, "Slot Deleted", Toast.LENGTH_SHORT).show();
        }

        // Refresh the UI
        loadData();
    }
}