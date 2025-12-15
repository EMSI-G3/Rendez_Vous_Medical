package com.example.rendez_vous;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PatientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        Button bookBtn = findViewById(R.id.btnBookAppointment);

        bookBtn.setOnClickListener(v -> {
            // Logic
            Toast.makeText(this, "Opening Booking System...", Toast.LENGTH_SHORT).show();

        });
    }
}