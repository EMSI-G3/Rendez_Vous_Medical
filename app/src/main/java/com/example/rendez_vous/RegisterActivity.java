package com.example.rendez_vous;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText fullName = findViewById(R.id.fullName);
        EditText email = findViewById(R.id.registerEmail);
        Button registerBtn = findViewById(R.id.buttonRegisterSubmit);

        registerBtn.setOnClickListener(v -> {
            if(fullName.getText().toString().isEmpty() || email.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please complete the form", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to Login
            }
        });
    }
}