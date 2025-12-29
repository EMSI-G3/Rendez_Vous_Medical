package com.example.rendez_vous.Profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rendez_vous.R;
import com.example.rendez_vous.AccessPoint.DatabaseHelper;
import com.example.rendez_vous.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    EditText etName, etPhone;
    ImageView profileImageView; // Renamed for clarity
    byte[] selectedImageBytes = null; // To store the newly selected image

    DatabaseHelper db;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        etName = findViewById(R.id.editName);
        etPhone = findViewById(R.id.editPhone);
        profileImageView = findViewById(R.id.editProfileImage);
        Button btChangePhoto = findViewById(R.id.btnChangePhoto);
        Button btnSave = findViewById(R.id.btnSaveProfile);

        // --- FETCH AND DISPLAY CURRENT DATA ---
        String email = session.getUserDetails().get(SessionManager.KEY_EMAIL);
        if (email != null) {
            int userId = db.getUserId(email);
            if (userId != -1) {
                // Fetch image from DB
                byte[] imageBytes = db.getUserProfileImage(userId);
                if (imageBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    profileImageView.setImageBitmap(bitmap);
                }

                // You might also want to set current name/phone
                etName.setText(db.getUserName(email));
                // If you have a getPhone method: etPhone.setText(db.getUserPhone(email));
            }
        }

        // Trigger Gallery to pick a new photo
        btChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        });

        btnSave.setOnClickListener(v -> {
            saveProfileChanges();
        });
    }

    // Handle the image selected from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                profileImageView.setImageBitmap(bitmap);

                // Convert to byte array for DB
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                selectedImageBytes = stream.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfileChanges() {
        String email = session.getUserDetails().get(SessionManager.KEY_EMAIL);
        int userId = db.getUserId(email);
        String name = etName.getText().toString();
        String phone = etPhone.getText().toString();

        // Update the DB (We added this method to DatabaseHelper in previous step)
        boolean success = db.updateUserProfile(userId, name, phone, selectedImageBytes);

        if (success) {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
        }
    }
}