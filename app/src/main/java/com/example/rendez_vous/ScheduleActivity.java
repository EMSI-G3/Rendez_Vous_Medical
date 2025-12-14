package com.example.rendez_vous;


import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TimeSlotAdapter adapter;
    private List<TimeSlot> slotList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Get Role if needed (Secretary ou Medicine)
        String role = getIntent().getStringExtra("ROLE");

        recyclerView = findViewById(R.id.slotsRecyclerView);
        ImageButton btnAdd = findViewById(R.id.btnAddSlot);

        // Setup Data
        slotList = new ArrayList<>();
        slotList.add(new TimeSlot("12 Oct", "09:00 - 09:30", "Available"));
        slotList.add(new TimeSlot("12 Oct", "10:00 - 10:30", "Booked"));
        slotList.add(new TimeSlot("12 Oct", "11:00 - 11:30", "Available"));

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TimeSlotAdapter(slotList, role);
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            // Logic to add a new slot
            Toast.makeText(this, "Add Slot Feature", Toast.LENGTH_SHORT).show();
            slotList.add(new TimeSlot("12 Oct", "12:00 - 12:30", "Available"));
            adapter.notifyItemInserted(slotList.size() - 1);
        });
    }
}
