package com.example.rendez_vous.Medicine;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.CalendarContract;
import android.content.DialogInterface;
import android.content.Intent;

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
    TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient); // Reuse shared layout

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

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

        // Customize Header
        welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("Medicine Dashboard");

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

    // 1. THIS IS THE ADAPTER METHOD
    private void bindAdapter(List<TimeSlot> list) {
        // Update the Dashboard Text
        if (welcomeText != null) {
            welcomeText.setText("Medicine Dashboard (" + list.size() + ")");
        }

        // Setup the Adapter
        recyclerView.setAdapter(new TimeSlotAdapter(list, "Medicine", (slot, action) -> {

            // --- SMART ACTION MENU ---
            String[] options = {
                    "✅ Confirm Appointment",
                    "🏁 Mark Completed",
                    "📅 Add to Phone Calendar",
                    "🗑️ Delete"
            };

            new AlertDialog.Builder(this)
                    .setTitle("Actions for " + slot.getPatientName())
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: // Confirm
                                    db.updateStatus(slot.getId(), "Confirmed");
                                    filterList(searchBar.getText().toString());
                                    break;

                                case 1: // Complete
                                    db.updateStatus(slot.getId(), "Completed");
                                    filterList(searchBar.getText().toString());
                                    break;

                                case 2: // CALENDAR INTEGRATION
                                    addToDeviceCalendar(slot);
                                    break;

                                case 3: // Delete
                                    com.example.rendez_vous.Medicine.DatabaseHelper medDb =
                                            new com.example.rendez_vous.Medicine.DatabaseHelper(DoctorActivity.this);
                                    medDb.deleteSlot(slot.getId());
                                    medDb.close();
                                    Toast.makeText(DoctorActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                    filterList(searchBar.getText().toString());
                                    break;
                            }
                        }
                    })
                    .show();
        }));
    } // <--- THIS BRACKET CLOSES bindAdapter. IMPORTANT!


    // 2. THIS IS THE HELPER METHOD (IT MUST BE OUTSIDE)
    private void addToDeviceCalendar(TimeSlot slot) {
        try {
            // Create an Intent to insert a new Event
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setData(CalendarContract.Events.CONTENT_URI);

            // Pre-fill the details
            intent.putExtra(CalendarContract.Events.TITLE, "Appointment: " + slot.getPatientName());
            intent.putExtra(CalendarContract.Events.DESCRIPTION, "Medical appointment. Status: " + slot.getStatus());
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "EMSI Clinic");

            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Could not open Calendar app", Toast.LENGTH_SHORT).show();
        }
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