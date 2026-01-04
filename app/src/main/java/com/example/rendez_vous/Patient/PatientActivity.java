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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rendez_vous.AccessPoint.Clinic;
import com.example.rendez_vous.AccessPoint.DatabaseHelper;
import com.example.rendez_vous.AccessPoint.Doctor;
import com.example.rendez_vous.AccessPoint.DoctorAdapter;
import com.example.rendez_vous.Medicine.TimeSlot;
import com.example.rendez_vous.Medicine.TimeSlotAdapter;
import com.example.rendez_vous.Profile.EditProfileActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.SessionManager;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PatientActivity extends AppCompatActivity {
    DatabaseHelper db;
    SessionManager session;
    RecyclerView recyclerView;
    String selectedDate = "", selectedTime = "";

    private int navigationStep = 0; // 0: Dashboard, 1: Clinics, 2: Doctors
    private int selectedClinicId = -1; // Store this globally in the activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        // 1. Initialize SearchBar and attach the listener
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Trigger search logic based on current view
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 2. BACK DISPATCHER MIGRATION (Fixed setEnabled)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (navigationStep == 2) {
                    showClinicSelection();
                } else if (navigationStep == 1) {
                    resetToDashboard();
                } else {
                    // Fix: 'this' refers to the OnBackPressedCallback
                    this.setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Profile Image Logic
        String userEmail = session.getUserDetails().get(SessionManager.KEY_EMAIL);
        if (userEmail != null) {
            int currentUserId = db.getUserId(userEmail);
            ImageView profileImageView = findViewById(R.id.patientProfileImage);
            loadProfilePicture(currentUserId, profileImageView);
        }

        recyclerView = findViewById(R.id.patientRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateList();

        findViewById(R.id.cardBookAppointment).setOnClickListener(v -> showClinicSelection());
        findViewById(R.id.profileIcon).setOnClickListener(this::showProfileMenu);
    }

    private void performSearch(String query) {
        String email = session.getUserDetails().get(SessionManager.KEY_EMAIL);
        int uid = db.getUserId(email);

        if (navigationStep == 0) {
            // Search Appointments
            recyclerView.setAdapter(new TimeSlotAdapter(db.searchPatientAppointments(uid, query), "Client", null));
        }
        else if (navigationStep == 1) {
            // Search Clinics
            RecyclerView rvSec = findViewById(R.id.patientRecyclerViewSecond);
            rvSec.setAdapter(new ClinicAdapter(db.searchClinics(query), c -> showDoctorSelection(c.getId())));
        }
        else if (navigationStep == 2) {
            // Search Doctors inside the chosen clinic
            RecyclerView rvSec = findViewById(R.id.patientRecyclerViewSecond);
            List<Doctor> filteredDoctors = db.searchDoctors(selectedClinicId, query);
            rvSec.setAdapter(new DoctorAdapter(filteredDoctors, d -> showBookingDialog(selectedClinicId, d.getId())));
        }
    }

    /**
     * Step 1: Show Clinic List
     */
    private void showClinicSelection() {
        navigationStep = 1;

        // 1. Get references to your UI components
        View card = findViewById(R.id.cardBookAppointment);
        View search = findViewById(R.id.searchContainer);
        RecyclerView mainList = findViewById(R.id.patientRecyclerView);
        RecyclerView secondList = findViewById(R.id.patientRecyclerViewSecond);
        TextView title = findViewById(R.id.listTitle);
        ImageButton backBtn = findViewById(R.id.btnBackSelection);

        // 2. Switch Visibility
        card.setVisibility(View.GONE);
        mainList.setVisibility(View.GONE);
        secondList.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);

        // 3. Update Text
        title.setText("Select a Clinic");
        ((TextView)findViewById(R.id.tvHeaderCategory)).setText("Step 1 of 2");

        // 4. Setup RecyclerView
        secondList.setLayoutManager(new LinearLayoutManager(this));

        // 5. Fetch Data - If this list is empty, check seedData in DatabaseHelper
        List<Clinic> clinics = db.getAllClinics();

        if (clinics.isEmpty()) {
            Toast.makeText(this, "No clinics found in database!", Toast.LENGTH_SHORT).show();
        }

        // 6. Bind Adapter
        ClinicAdapter adapter = new ClinicAdapter(clinics, clinic -> {
            showDoctorSelection(clinic.getId());
        });
        secondList.setAdapter(adapter);

        // Handle back button
        backBtn.setOnClickListener(v -> resetToDashboard());
    }

    private void showDoctorSelection(int clinicId) {
        this.selectedClinicId = clinicId; // Save the ID for searching later
        navigationStep = 2;

        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.setText("");
        searchBar.setHint("Search doctor name or specialty...");

        RecyclerView secondList = findViewById(R.id.patientRecyclerViewSecond);
        List<Doctor> doctors = db.getDoctorsByClinic(clinicId);
        secondList.setAdapter(new DoctorAdapter(doctors, d -> showBookingDialog(clinicId, d.getId())));
    }

    private void resetToDashboard() {
        navigationStep = 0;

        // Switch views
        findViewById(R.id.cardBookAppointment).setVisibility(View.VISIBLE);
        findViewById(R.id.searchContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.patientRecyclerView).setVisibility(View.VISIBLE);
        findViewById(R.id.patientRecyclerViewSecond).setVisibility(View.GONE);

        // Update header text
        ((TextView)findViewById(R.id.listTitle)).setText("Your Appointments");

        // Refresh the data!
        updateList();
    }

    /**
     * Step 3: Show Date/Time Picker Dialog
     */
    private void showBookingDialog(int clinicId, int doctorId) {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.activity_patient_dialog);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvDate = d.findViewById(R.id.dialogDate);
        TextView tvTime = d.findViewById(R.id.dialogTime);

        // RESET variables locally to ensure fresh selection
        selectedDate = "";
        selectedTime = "";

        tvDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, day) -> {
                selectedDate = day + "/" + (m + 1) + "/" + y;
                tvDate.setText(selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                tvTime.setText(selectedTime);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        d.findViewById(R.id.dialogBtnConfirm).setOnClickListener(v -> {
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select both Date and Time", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = session.getUserDetails().get(SessionManager.KEY_EMAIL);
            int uid = db.getUserId(email);
            String uName = db.getUserName(email);

            // 1. Attempt to save to Database
            boolean success = db.addAppointment(uid, doctorId, clinicId, uName, selectedDate, selectedTime);

            if (success) {
                Toast.makeText(this, "Booked Successfully!", Toast.LENGTH_SHORT).show();

                // 2. Clear selections for next time
                selectedDate = "";
                selectedTime = "";

                // 3. UI RESET: Move back to dashboard
                resetToDashboard();

                // 4. CRITICAL: Force the list to refresh from DB immediately
                updateList();

                d.dismiss();
            } else {
                Toast.makeText(this, "This slot is already occupied for this doctor!", Toast.LENGTH_LONG).show();
            }
        });
        d.show();
    }





    private void updateList() {
        String email = session.getUserDetails().get(SessionManager.KEY_EMAIL);
        int uid = db.getUserId(email);

        // DEBUG: Check if UID is valid
        if (uid == -1) {
            Toast.makeText(this, "Error: User ID not found for " + email, Toast.LENGTH_LONG).show();
            return;
        }

        List<TimeSlot> appointments = db.getAppointments("Client", uid);

        // DEBUG: Check if DB actually returned data
        if (appointments.isEmpty()) {
            ((TextView)findViewById(R.id.listTitle)).setText("No Appointments Found");
        } else {
            ((TextView)findViewById(R.id.listTitle)).setText("Your Appointments (" + appointments.size() + ")");
        }

        recyclerView.setAdapter(new TimeSlotAdapter(appointments, "Client", null));
    }

    private void loadProfilePicture(int userId, ImageView imageView) {
        byte[] imageBytes = db.getUserProfileImage(userId);
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(bitmap);
            imageView.setImageTintList(null);
            imageView.setPadding(0, 0, 0, 0);
        }
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
}