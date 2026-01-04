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
import com.example.rendez_vous.AccessPoint.User;
import com.example.rendez_vous.Profile.EditProfileActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.SessionManager;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import android.telephony.SmsManager;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class SecretaryActivity extends AppCompatActivity {
    DatabaseHelper db;
    SessionManager session;
    RecyclerView recyclerView;
    EditText searchBar;

    // State variable to track what list we are currently looking at
    boolean isViewingUsers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient); // Reuse the shared layout

        // Inside SecretaryActivity.java -> onCreate

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

// FIX: Use the key from SessionManager to get the current email
        String userEmail = session.getUserDetails().get(SessionManager.KEY_EMAIL);

        if (userEmail != null) {
            int currentUserId = db.getUserId(userEmail);

            if (currentUserId != -1) {
                byte[] imageBytes = db.getUserProfileImage(currentUserId);
                ImageView profileImageView = findViewById(R.id.patientProfileImage);

                if (imageBytes != null && imageBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    profileImageView.setImageBitmap(bitmap);

                    // Important: Remove the teal tint so the photo looks natural
                    profileImageView.setImageTintList(null);
                    profileImageView.setPadding(0, 0, 0, 0);
                } else {
                    // Optional: If the secretary has no image, set a default icon
                    profileImageView.setImageResource(android.R.drawable.ic_menu_my_calendar);
                }
            }
        }

        // Customize Header
        ((TextView)findViewById(R.id.welcomeText)).setText("Secretary Dashboard");

        // Hide the "Book Appointment" card (Secretary manages, doesn't book self)
        findViewById(R.id.cardBookAppointment).setVisibility(View.GONE);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.patientRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });


        findViewById(R.id.profileIcon).setOnClickListener(this::showProfileMenu);


        loadAppointments();
    }



    private void filterList(String query) {
        if (isViewingUsers) {
            // If viewing Users, search by Name
            if (query.isEmpty()) bindUserAdapter(db.getAllUsers());
            else bindUserAdapter(db.searchUsers(query));
        } else {
            // If viewing Appointments, search by Date or Patient Name
            if (query.isEmpty()) bindAppointmentAdapter(db.getAppointments("Secretary", 0));
            else bindAppointmentAdapter(db.searchAppointments(query));
        }
    }

    private void loadAppointments() {
        isViewingUsers = false;
        searchBar.setHint("Search Appointment by Name or Date...");
        searchBar.setText(""); // Reset search
        bindAppointmentAdapter(db.getAppointments("Secretary", 0));
    }

    private void loadUsers() {
        isViewingUsers = true;
        searchBar.setHint("Search User by Name...");
        searchBar.setText(""); // Reset search
        bindUserAdapter(db.getAllUsers());
    }

    // --- ADAPTER BINDING HELPERS ---

    // 1. Bind Appointment List
    private void bindAppointmentAdapter(List<TimeSlot> list) {
        recyclerView.setAdapter(new TimeSlotAdapter(list, "Secretary", (slot, action) -> {
            CharSequence[] options = {"Confirm", "Complete", "Delete"};
            new AlertDialog.Builder(this)
                    .setTitle("Manage: " + slot.getPatientName())
                    .setItems(options, (d, w) -> {
                        if (w == 0) db.updateStatus(slot.getId(), "Confirmed");
                        if (w == 1) { // Complete
                            db.updateStatus(slot.getId(), "Completed");
                            sendStatusSMS(slot.getId(), "Completed"); // Add this line
                        }
                        if (w == 2) db.deleteAppointment(slot.getId());

                        // Refresh view keeping current search query
                        filterList(searchBar.getText().toString());
                    })
                    .show();
        }));
    }

    // 2. Bind User List
    private void bindUserAdapter(List<User> list) {
        recyclerView.setAdapter(new UserAdapter(list, user -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove User")
                    .setMessage("Delete " + user.getFullname() + " and all their data?")
                    .setPositiveButton("Yes", (d, w) -> {
                        db.deleteUser(user.getId());
                        // Refresh view keeping current search query
                        filterList(searchBar.getText().toString());
                    })
                    .setNegativeButton("No", null)
                    .show();
        }));
    }

    // --- EXPORT TO HTML ---
    private void exportDataAsHtml() {
        try {
            File file = new File(getExternalFilesDir(null), "rendez_vous_report.html");
            FileWriter writer = new FileWriter(file);
            StringBuilder html = new StringBuilder();

            html.append("<html><body><h1>System Report</h1><hr>");

            // Export Users
            html.append("<h2>Registered Users</h2><table border='1' width='100%'>");
            html.append("<tr><th>Name</th><th>Email</th><th>Role</th></tr>");
            for (User u : db.getAllUsers()) {
                html.append("<tr><td>").append(u.getFullname()).append("</td>");
                html.append("<td>").append(u.getEmail()).append("</td>");
                html.append("<td>").append(u.getRole()).append("</td></tr>");
            }
            html.append("</table>");

            // Export Appointments
            html.append("<h2>All Appointments</h2><table border='1' width='100%'>");
            html.append("<tr><th>Date</th><th>Time</th><th>Patient</th><th>Status</th></tr>");
            for (TimeSlot s : db.getAppointments("Secretary", 0)) {
                html.append("<tr><td>").append(s.getDate()).append("</td>");
                html.append("<td>").append(s.getTime()).append("</td>");
                html.append("<td>").append(s.getPatientName()).append("</td>");
                html.append("<td>").append(s.getStatus()).append("</td></tr>");
            }
            html.append("</table></body></html>");

            writer.write(html.toString());
            writer.close();
            Toast.makeText(this, "Exported: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // --- MENU LOGIC ---
    private void showProfileMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);

        // Add dynamic options for Secretary
        popup.getMenu().add(0, 1, 0, "Show Appointments");
        popup.getMenu().add(0, 2, 0, "Show Users");
        popup.getMenu().add(0, 3, 0, "Export Data (HTML)");

        // Add standard profile options from XML
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) { loadAppointments(); return true; }
            if (id == 2) { loadUsers(); return true; }
            if (id == 3) { exportDataAsHtml(); return true; }

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
    private void sendStatusSMS(int appointmentId, String status) {
        // 1. Check for permission at runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
            return;
        }

        // 2. Get the phone number
        String phoneNumber = db.getPatientPhoneByAppointmentId(appointmentId);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                String message = "Hello, your appointment (ID: " + appointmentId + ") status has been updated to: " + status;
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "Notification SMS sent to patient.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "SMS Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}