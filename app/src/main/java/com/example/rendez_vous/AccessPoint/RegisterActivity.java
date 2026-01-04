package com.example.rendez_vous.AccessPoint;

import com.example.rendez_vous.R;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private ImageView profileImageView;
    private byte[] imageByteArray = null;
    private List<Clinic> clinicList; // To store clinic objects for ID retrieval

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        // Basic Fields
        profileImageView = findViewById(R.id.registerUserImage);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        EditText fullNameInput = findViewById(R.id.fullName);
        EditText emailInput = findViewById(R.id.registerEmail);
        EditText phoneInput = findViewById(R.id.registerPhone);
        EditText passwordInput = findViewById(R.id.registerPassword);
        Spinner roleSpinner = findViewById(R.id.registerRoleSpinner);
        Button registerBtn = findViewById(R.id.buttonRegisterSubmit);

        // Doctor Specific Fields
        LinearLayout doctorFields = findViewById(R.id.doctorExtraFields);
        EditText specialtyInput = findViewById(R.id.registerSpecialty);
        Spinner clinicSpinner = findViewById(R.id.registerClinicSpinner);

        // 1. Setup Role Spinner
        String[] roles = {"Client", "Medicine", "Secretary"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(roleAdapter);

        // 2. Setup Clinic Spinner (Fetch from DB)
        clinicList = db.getAllClinics();
        List<String> clinicNames = new ArrayList<>();
        for (Clinic c : clinicList) {
            clinicNames.add(c.getName());
        }
        ArrayAdapter<String> clinicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clinicNames);
        clinicSpinner.setAdapter(clinicAdapter);

        // 3. Toggle Doctor Fields visibility based on selection
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (roles[position].equals("Medicine")) {
                    doctorFields.setVisibility(View.VISIBLE);
                } else {
                    doctorFields.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 4. Image Selection
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });

        // 5. Registration Logic
        registerBtn.setOnClickListener(v -> {
            String fullname = fullNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            // Basic Validation
            if(fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // STEP 1: Register in the 'users' table
            boolean userInserted = db.registerUser(fullname, email, phone, password, role, imageByteArray);

            if(userInserted) {
                // STEP 2: If the user is a doctor, create the professional profile
                if(role.equals("Medicine")) {
                    // Get the ID of the user we just created
                    int newUserId = db.getUserId(email);

                    // Get Specialty and Clinic ID from the new fields
                    String specialty = specialtyInput.getText().toString().trim();

                    // Assuming clinicList is a List<Clinic> populated from db.getAllClinics()
                    int selectedClinicId = clinicList.get(clinicSpinner.getSelectedItemPosition()).getId();

                    if(specialty.isEmpty()) {
                        Toast.makeText(this, "Doctor registration requires a specialty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // STEP 3: Register in 'doctors' table
                    boolean doctorLinked = db.registerDoctor(newUserId, selectedClinicId, specialty);

                    if(!doctorLinked) {
                        Toast.makeText(this, "Failed to link doctor to clinic", Toast.LENGTH_SHORT).show();
                    }
                }

                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity and return to login
            } else {
                Toast.makeText(this, "Registration Failed (Email already exists?)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                profileImageView.setImageBitmap(bitmap);
                imageByteArray = bitmapToByteArray(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        return stream.toByteArray();
    }
}