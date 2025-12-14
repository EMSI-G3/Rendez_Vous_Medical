package com.example.rendez_vous;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Spinner roleSpinner;
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Views
        roleSpinner = findViewById(R.id.roleSpinner);
        emailInput = findViewById(R.id.emailEditText);
        passwordInput = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // Users
        String[] roles = {"Client", "Medicine", "Secretary"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(adapter);

        // Login Logic
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            String selectedRole = roleSpinner.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // auth logic.
                performLogin(selectedRole);
            }
        });

        // Register Nav
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin(String role) {
        Toast.makeText(this, "Welcome " + role, Toast.LENGTH_SHORT).show();
        Intent intent;

        if (role.equals("Medicine") || role.equals("Secretary")) {
            // Doctors and Secretaries -> Schedule Management
            intent = new Intent(this, ScheduleActivity.class);
            intent.putExtra("ROLE", role); // Pass role to next screen
        } else {
            // Clients -> Patient Dashboard
            intent = new Intent(this, PatientActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
