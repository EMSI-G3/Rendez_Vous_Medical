package com.example.rendez_vous.AccessPoint;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.SessionManager;
import com.example.rendez_vous.Patient.PatientActivity;
import com.example.rendez_vous.Medicine.DoctorActivity;
import com.example.rendez_vous.Medicine.SecretaryActivity;

public class LoginActivity extends AppCompatActivity {
    DatabaseHelper db;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        Spinner roleSpinner = findViewById(R.id.roleSpinner);
        EditText emailIn = findViewById(R.id.emailEditText);
        EditText passIn = findViewById(R.id.passwordEditText);
        Button btnLogin = findViewById(R.id.loginButton);

        // Populate Roles
        String[] roles = {"Client", "Medicine", "Secretary", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(adapter);

        btnLogin.setOnClickListener(v -> {
            String email = emailIn.getText().toString();
            String pass = passIn.getText().toString();
            String role = roleSpinner.getSelectedItem().toString();

            if (db.checkUser(email, pass, role)) {
                session.createLoginSession(email, role);
                redirectUser(role);
            } else {
                Toast.makeText(this, "Invalid Credentials for " + role, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.registerLink).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void redirectUser(String role) {
        Intent intent = null;
        switch (role) {
            case "Client": intent = new Intent(this, PatientActivity.class); break;
            case "Medicine": intent = new Intent(this, DoctorActivity.class); break; // "Medicine" goes to DoctorActivity
            case "Secretary": intent = new Intent(this, SecretaryActivity.class); break;
            case "Admin": intent = new Intent(this, SecretaryActivity.class); break;
        }
        startActivity(intent);
        finish();
    }
}