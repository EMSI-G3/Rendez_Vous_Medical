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
import com.example.rendez_vous.AccessPoint.Specialty;
import com.example.rendez_vous.Medicine.TimeSlot;
import com.example.rendez_vous.Medicine.TimeSlotAdapter;
import com.example.rendez_vous.Profile.EditProfileActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PatientActivity extends AppCompatActivity {
    DatabaseHelper db;
    SessionManager session;
    RecyclerView recyclerView;
    String selectedDate = "", selectedTime = "";

    private String slectedSpecialty = "";
    private String selectedSpecialtyName = "";

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
                if (navigationStep == 3) {
                    showSpecialtySelection(selectedClinicId); // Go back to specialties
                } else if (navigationStep == 2) {
                    showClinicSelection(); // Go back to clinics
                } else if (navigationStep == 1) {
                    resetToDashboard();
                } else {
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
        RecyclerView rvSec = findViewById(R.id.patientRecyclerViewSecond);

        if (navigationStep == 0) {
            // Search Appointments (Dashboard)
            recyclerView.setAdapter(new TimeSlotAdapter(db.searchPatientAppointments(uid, query), "Client", null));
        }
        else if (navigationStep == 1) {
            // Search Clinics -> Now leads to Specialty
            rvSec.setAdapter(new ClinicAdapter(db.searchClinics(query), c -> showSpecialtySelection(c.getId())));
        }
        else if (navigationStep == 2) {
            // Search Specialties (Filtering the list)
            List<Specialty> allSpecs = db.getSpecialtiesByClinic(selectedClinicId);
            List<Specialty> filteredSpecs = new ArrayList<>();
            for (Specialty s : allSpecs) {
                if (s.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredSpecs.add(s);
                }
            }
            rvSec.setAdapter(new SpecialtyAdapter(filteredSpecs, s -> showDoctorSelection(selectedClinicId, s.getName())));
        }
        else if (navigationStep == 3) {
            // FIX: Added selectedSpecialtyName to match the new DatabaseHelper method signature
            List<Doctor> filteredDoctors = db.searchDoctors(selectedClinicId, selectedSpecialtyName, query);

            rvSec.setAdapter(new DoctorAdapter(filteredDoctors, d -> showBookingDialog(selectedClinicId, d.getId())));
        }
    }

    private void showClinicSelection() {
        navigationStep = 1;

        // UI Visibility
        findViewById(R.id.cardBookAppointment).setVisibility(View.GONE);
        findViewById(R.id.patientRecyclerView).setVisibility(View.GONE);
        findViewById(R.id.patientRecyclerViewSecond).setVisibility(View.VISIBLE);
        findViewById(R.id.btnBackSelection).setVisibility(View.VISIBLE);

        // Header updates
        ((TextView)findViewById(R.id.listTitle)).setText("Select a Clinic");
        ((TextView)findViewById(R.id.tvHeaderCategory)).setText("Step 1 of 3");

        RecyclerView secondList = findViewById(R.id.patientRecyclerViewSecond);
        secondList.setLayoutManager(new LinearLayoutManager(this));

        List<Clinic> clinics = db.getAllClinics();
        if (clinics.isEmpty()) {
            Toast.makeText(this, "No clinics found!", Toast.LENGTH_SHORT).show();
        }

        secondList.setAdapter(new ClinicAdapter(clinics, clinic -> {
            showSpecialtySelection(clinic.getId()); // Move to Step 2
        }));

        findViewById(R.id.btnBackSelection).setOnClickListener(v -> resetToDashboard());
    }

    /**
     * Step 2: Select Specialty
     */
    private void showSpecialtySelection(int clinicId) {
        this.selectedClinicId = clinicId; // Store clinic context
        navigationStep = 2;

        ((TextView)findViewById(R.id.listTitle)).setText("Select Specialty");
        ((TextView)findViewById(R.id.tvHeaderCategory)).setText("Step 2 of 3");

        RecyclerView secondList = findViewById(R.id.patientRecyclerViewSecond);

        // Fetch specialties from DB (Ensure you added this method to DatabaseHelper)
        List<Specialty> specialties = db.getSpecialtiesByClinic(clinicId);

        if (specialties.isEmpty()) {
            Toast.makeText(this, "No specialties found for this clinic!", Toast.LENGTH_SHORT).show();
        }

        secondList.setAdapter(new SpecialtyAdapter(specialties, specialty -> {
            showDoctorSelection(clinicId, specialty.getName()); // Move to Step 3
        }));

        findViewById(R.id.btnBackSelection).setOnClickListener(v -> showClinicSelection());
    }

    /**
     * Step 3: Select Doctor (Filtered by Clinic AND Specialty)
     */
    private void showDoctorSelection(int clinicId, String specialty) {
        this.selectedSpecialtyName = specialty;
        navigationStep = 3;

        ((TextView)findViewById(R.id.listTitle)).setText("Doctors: " + specialty);
        ((TextView)findViewById(R.id.tvHeaderCategory)).setText("Step 3 of 3");

        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.setText("");
        searchBar.setHint("Search doctors...");

        RecyclerView secondList = findViewById(R.id.patientRecyclerViewSecond);

        // Use the filtered method from DatabaseHelper
        List<Doctor> doctors = db.getDoctorsByClinicAndSpecialty(clinicId, specialty);

        secondList.setAdapter(new DoctorAdapter(doctors, d -> showBookingDialog(clinicId, d.getId())));

        findViewById(R.id.btnBackSelection).setOnClickListener(v -> showSpecialtySelection(clinicId));
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
            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, day) -> {
                selectedDate = day + "/" + (m + 1) + "/" + y;
                tvDate.setText(selectedDate);

                // Optionnel : Réinitialiser l'heure si la date change pour forcer une nouvelle vérification
                selectedTime = "";
                tvTime.setText("Select Time");
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            // --- EMPECHER LES DATES PASSEES ---
            // On fixe la date minimale sur "aujourd'hui" (en millisecondes)
            dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

            dpd.show();
        });

        tvTime.setOnClickListener(v -> {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar now = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) -> {

                // Vérification si la date sélectionnée est aujourd'hui
                String todayStr = now.get(Calendar.DAY_OF_MONTH) + "/" + (now.get(Calendar.MONTH) + 1) + "/" + now.get(Calendar.YEAR);

                if (selectedDate.equals(todayStr)) {
                    // Si c'est aujourd'hui, on vérifie si l'heure est passée
                    if (h < now.get(Calendar.HOUR_OF_DAY) || (h == now.get(Calendar.HOUR_OF_DAY) && m <= now.get(Calendar.MINUTE))) {
                        Toast.makeText(this, "This time has already passed!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Vérification des horaires de travail (09h - 18h)
                if (h < 9 || h >= 18) {
                    Toast.makeText(this, "We are closed. Choose between 09:00 and 18:00", Toast.LENGTH_SHORT).show();
                } else {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                    tvTime.setText(selectedTime);
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        });

        d.findViewById(R.id.dialogBtnConfirm).setOnClickListener(v -> {
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select both Date and Time", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedHour = Integer.parseInt(selectedTime.split(":")[0]);

            // Définition de la plage horaire (ex: 09:00 - 18:00)
            if (selectedHour < 9 || selectedHour >= 18) {
                Toast.makeText(this, "Appointments are only available between 09:00 and 18:00", Toast.LENGTH_LONG).show();
                return; // On arrête l'exécution ici
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

    private void showPrettyToast(String message, int iconRes, int bgColor) {
        View layout = getLayoutInflater().inflate(R.layout.custom_toast_layout, findViewById(R.id.custom_toast_container));

        // Set text and icon
        TextView text = layout.findViewById(R.id.toast_text);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        View container = layout.findViewById(R.id.custom_toast_container);

        text.setText(message);
        icon.setImageResource(iconRes);
        container.getBackground().setColorFilter(bgColor, android.graphics.PorterDuff.Mode.SRC_IN);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
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