package com.example.rendez_vous.AccessPoint;

import com.example.rendez_vous.R;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private ImageView profileImageView;
    private byte[] imageByteArray = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        profileImageView = findViewById(R.id.registerUserImage);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);


        EditText fullNameInput = findViewById(R.id.fullName);
        EditText emailInput = findViewById(R.id.registerEmail);
        EditText phoneInput = findViewById(R.id.registerPhone); // New Field
        EditText passwordInput = findViewById(R.id.registerPassword);
        Spinner roleSpinner = findViewById(R.id.registerRoleSpinner); // New Field in XML
        Button registerBtn = findViewById(R.id.buttonRegisterSubmit);
        // to open the gallary
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });
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
                // Pass the imageByteArray to the DB helper
                boolean isInserted = db.registerUser(fullname, email, phone, password, role, imageByteArray);
                if(isInserted) {
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                // Convert Uri to Bitmap
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Show in ImageView
                profileImageView.setImageBitmap(bitmap);

                // Convert Bitmap to byte[] for Database
                imageByteArray = bitmapToByteArray(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to compress image (important to prevent DB lag)
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress to 50% quality to save space
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        return stream.toByteArray();
    }
}