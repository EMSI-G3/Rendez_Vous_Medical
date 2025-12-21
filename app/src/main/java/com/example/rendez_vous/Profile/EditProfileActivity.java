package com.example.rendez_vous.Profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.AccessPoint.DatabaseHelper;
import com.example.rendez_vous.SessionManager;

public class EditProfileActivity extends AppCompatActivity {

    EditText etName, etPhone;
    DatabaseHelper db;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // We create this next

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        etName = findViewById(R.id.editName);
        etPhone = findViewById(R.id.editPhone);

        // Save Button Logic
        Button btnSave = findViewById(R.id.btnSaveProfile);
        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}