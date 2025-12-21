package com.example.rendez_vous.AccessPoint;

import com.example.rendez_vous.R;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        EditText fullNameInput = findViewById(R.id.fullName);
        EditText emailInput = findViewById(R.id.registerEmail);
        EditText phoneInput = findViewById(R.id.registerPhone); // New Field
        EditText passwordInput = findViewById(R.id.registerPassword);
        Spinner roleSpinner = findViewById(R.id.registerRoleSpinner); // New Field in XML
        Button registerBtn = findViewById(R.id.buttonRegisterSubmit);

        // Populate Spinner
        // Inside RegisterActivity.java onCreate...
        String[] roles = {"Client", "Medicine", "Secretary"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(adapter);

        registerBtn.setOnClickListener(v -> {
            String fullname = fullNameInput.getText().toString();
            String email = emailInput.getText().toString();
            String phone = phoneInput.getText().toString();
            String password = passwordInput.getText().toString();
            String role = roleSpinner.getSelectedItem().toString();

            if(fullname.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please complete the form", Toast.LENGTH_SHORT).show();
            } else {
                if(db.checkEmail(email)) {
                    Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
                } else {
                    // Pass phone to DB
                    boolean isInserted = db.registerUser(fullname, email, phone, password, role);
                    if(isInserted) {
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}