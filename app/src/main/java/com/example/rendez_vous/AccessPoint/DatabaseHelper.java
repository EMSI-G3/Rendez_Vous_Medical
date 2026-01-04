package com.example.rendez_vous.AccessPoint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.rendez_vous.Medicine.TimeSlot;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RendezVous_Final.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Users Table
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, fullname TEXT, email TEXT, phone TEXT, password TEXT, role TEXT, profile_pic BLOB)");

        // 2. Clinics Table
        db.execSQL("CREATE TABLE clinics (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, location TEXT)");

        // 3. Doctors Table (Linked to Users and Clinics)
        db.execSQL("CREATE TABLE doctors (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, clinic_id INTEGER, specialty TEXT, available INTEGER, FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(clinic_id) REFERENCES clinics(id))");

        // 4. Appointments Table (Linked to Patient, Doctor, and Clinic)
        db.execSQL("CREATE TABLE appointments (id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER, doctor_id INTEGER, clinic_id INTEGER, patient_name TEXT, date TEXT, time TEXT, status TEXT, FOREIGN KEY(patient_id) REFERENCES users(id), FOREIGN KEY(doctor_id) REFERENCES doctors(id), FOREIGN KEY(clinic_id) REFERENCES clinics(id))");

        seedData(db);
    }

    private void seedData(SQLiteDatabase db) {
        // Seed Users
        db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Dr. House', 'doc@test.com', '123', 'Medicine')");
        db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Ms. Potts', 'sec@test.com', '123', 'Secretary')");
        db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Admin', 'admin@test.com', '123', 'Admin')");

        // Seed Clinics
        db.execSQL("INSERT INTO clinics (name, location) VALUES ('General City Clinic', 'Downtown')");
        db.execSQL("INSERT INTO clinics (name, location) VALUES ('Westside Medical', 'West District')");

        // Seed Doctor Link (Link User 1 to Clinic 1)
        db.execSQL("INSERT INTO doctors (user_id, clinic_id, specialty, available) VALUES (1, 1, 'Diagnostics', 1)");

       db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Dr. House', 'doc@test.com', '123', 'Medicine')");
       db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Ms. Potts', 'sec@test.com', '123', 'Secretary')");
       db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Admin', 'admin@test.com', '123', 'Admin')");

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS appointments");
        db.execSQL("DROP TABLE IF EXISTS doctors");
        db.execSQL("DROP TABLE IF EXISTS clinics");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public boolean registerUser(String fullname, String email, String phone, String password, String role, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", password);
        values.put("role", role);
        values.put("profile_pic", image);
        return db.insert("users", null, values) != -1;
    }
    public boolean registerDoctor(int userId, int clinicId, String specialty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("user_id", userId);      // Link to the user table
        values.put("clinic_id", clinicId);  // Link to the selected clinic
        values.put("specialty", specialty);
        values.put("available", 1);         // Default to available (1 = true)

        long result = db.insert("doctors", null, values);
        return result != -1;
    }




    public boolean checkUser(String email, String password, String role) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM users WHERE email=? AND password=? AND role=?", new String[]{email, password, role});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

//    public boolean checkEmail(String email) {
//        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM users WHERE email=?", new String[]{email});
//        boolean exists = cursor.getCount() > 0;
//        cursor.close();
//        return exists;
//    }

    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email});

        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public String getUserName(String email) {
        Cursor c = getReadableDatabase().rawQuery("SELECT fullname FROM users WHERE email=?", new String[]{email});
        String name = "User";
        if(c.moveToFirst()) name = c.getString(0);
        c.close();
        return name;
    }


    // Clinic logic

    public List<Clinic> getAllClinics() {
        List<Clinic> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM clinics", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String loc = cursor.getString(cursor.getColumnIndexOrThrow("location"));

                // Get list of available doctor names for this specific clinic
                List<String> docs = getDoctorNamesForClinic(id);
                list.add(new Clinic(id, name, loc, docs));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private List<String> getDoctorNamesForClinic(int clinicId) {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u.fullname FROM users u JOIN doctors d ON u.id = d.user_id WHERE d.clinic_id = ? AND d.available = 1";
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(clinicId)});
        if (c.moveToFirst()) {
            do { names.add(c.getString(0)); } while (c.moveToNext());
        }
        c.close();
        return names;
    }

    // --- APPOINTMENT LOGIC ---
    public boolean isSlotTaken(int doctorId, String date, String time) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM appointments WHERE doctor_id=? AND date=? AND time=?",
                new String[]{String.valueOf(doctorId), date, time});
        boolean taken = cursor.getCount() > 0;
        cursor.close();
        return taken;
    }
    public List<Doctor> getDoctorsByClinic(int clinicId) {
        List<Doctor> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // JOIN with Users to get Full Name AND Profile Pic
        String query = "SELECT d.id, u.fullname, d.specialty, d.available, u.profile_pic " +
                "FROM doctors d " +
                "JOIN users u ON d.user_id = u.id " +
                "WHERE d.clinic_id = ? AND d.available = 1";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(clinicId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String specialty = cursor.getString(2);
                int available = cursor.getInt(3);
                byte[] pic = cursor.getBlob(4); // Get the image blob

                list.add(new Doctor(id, name, specialty, available == 1, pic));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean addAppointment(int patientId, int doctorId, int clinicId, String patientName, String date, String time) {
        if (isSlotTaken(doctorId, date, time)) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("patient_id", patientId);  // Ensure this matches the ID from session
        values.put("doctor_id", doctorId);
        values.put("clinic_id", clinicId);
        values.put("patient_name", patientName);
        values.put("date", date);
        values.put("time", time);
        values.put("status", "Pending");

        long result = db.insert("appointments", null, values);
        return result != -1;
    }

    public List<TimeSlot> searchAppointments(String query) {
        List<TimeSlot> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Use wildcards for partial matching
        String search = "%" + query + "%";

        // Search in Date OR Patient Name
        Cursor cursor = db.rawQuery(
                "SELECT * FROM appointments WHERE date LIKE ? OR patient_name LIKE ?",
                new String[]{search, search}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String pName = cursor.getString(cursor.getColumnIndexOrThrow("patient_name"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                TimeSlot slot = new TimeSlot(id, date, time, status);
                slot.setPatientName(pName);
                list.add(slot);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Clinic> searchClinics(String query) {
        List<Clinic> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String searchPattern = "%" + query + "%";

        Cursor cursor = db.rawQuery("SELECT * FROM clinics WHERE name LIKE ? OR location LIKE ?",
                new String[]{searchPattern, searchPattern});

        if (cursor.moveToFirst()) {
            do {
                list.add(new Clinic(cursor.getInt(0), cursor.getString(1), cursor.getString(2), null));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    public List<Doctor> searchDoctors(int clinicId, String query) {
        List<Doctor> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String searchPattern = "%" + query + "%";

        // We only search doctors within the SELECTED clinic
        String sql = "SELECT d.id, u.fullname, d.specialty, d.available, u.profile_pic " +
                "FROM doctors d " +
                "JOIN users u ON d.user_id = u.id " +
                "WHERE d.clinic_id = ? AND (u.fullname LIKE ? OR d.specialty LIKE ?)";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(clinicId), searchPattern, searchPattern});

        if (cursor.moveToFirst()) {
            do {
                byte[] pic = cursor.getBlob(4);
                list.add(new Doctor(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3) == 1, pic));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    public List<TimeSlot> searchPatientAppointments(int patientId, String query) {
        List<TimeSlot> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String search = "%" + query + "%";

        // This JOIN ensures we get Doctor and Clinic names while searching
        String sql = "SELECT a.*, u.fullname as doc_name, c.name as clin_name FROM appointments a " +
                "LEFT JOIN doctors d ON a.doctor_id = d.id " +
                "LEFT JOIN users u ON d.user_id = u.id " +
                "LEFT JOIN clinics c ON a.clinic_id = c.id " +
                "WHERE a.patient_id = ? AND (a.date LIKE ? OR doc_name LIKE ? OR clin_name LIKE ?)";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(patientId), search, search, search});

        if (cursor.moveToFirst()) {
            do {
                TimeSlot slot = new TimeSlot(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
                slot.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
                slot.setDoctorName(cursor.getString(cursor.getColumnIndexOrThrow("doc_name")));
                slot.setClinicName(cursor.getString(cursor.getColumnIndexOrThrow("clin_name")));
                list.add(slot);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    public List<TimeSlot> getAppointments(String role, int userId) {
        List<TimeSlot> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Use LEFT JOIN to ensure appointments show up even if there's a minor link issue
        String query = "SELECT a.*, u.fullname as doc_name, c.name as clinic_name " +
                "FROM appointments a " +
                "LEFT JOIN doctors d ON a.doctor_id = d.id " +
                "LEFT JOIN users u ON d.user_id = u.id " +
                "LEFT JOIN clinics c ON a.clinic_id = c.id ";

        Cursor cursor;
        if (role.equals("Client")) {
            // We filter strictly by the patient_id we got from the session
            query += "WHERE a.patient_id = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        } else {
            cursor = db.rawQuery(query, null);
        }

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String pName = cursor.getString(cursor.getColumnIndexOrThrow("patient_name"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                // Get joined names
                String docName = cursor.getString(cursor.getColumnIndexOrThrow("doc_name"));
                String clinName = cursor.getString(cursor.getColumnIndexOrThrow("clinic_name"));

                TimeSlot slot = new TimeSlot(id, date, time, status);
                slot.setPatientName(pName);
                slot.setDoctorName(docName != null ? docName : "Unknown Doctor");
                slot.setClinicName(clinName != null ? clinName : "Unknown Clinic");
                list.add(slot);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateStatus(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update("appointments", values, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteAppointment(int id) {
        getWritableDatabase().delete("appointments", "id=?", new String[]{String.valueOf(id)});
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));

                // Hide the Admin from the list so they can't be deleted
                if (!role.equals("Admin")) {
                    list.add(new User(id, name, email, role));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", "id=?", new String[]{String.valueOf(id)});
        // Also delete their appointments to keep data clean
        db.delete("appointments", "patient_id=?", new String[]{String.valueOf(id)});
    }

    public List<User> searchUsers(String query) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String search = "%" + query + "%";

        // Filter by Full Name
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE fullname LIKE ?", new String[]{search});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));

                // Exclude Admin from search results
                if (!role.equals("Admin")) {
                    list.add(new User(id, name, email, role));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    public String getPatientPhoneByAppointmentId(int appId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String phone = null;
        // Join users and appointments to get the phone number for a specific appointment
        String query = "SELECT u.phone FROM users u " +
                "JOIN appointments a ON u.id = a.patient_id " +
                "WHERE a.id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(appId)});
        if (cursor.moveToFirst()) {
            phone = cursor.getString(0);
        }
        cursor.close();
        return phone;
    }

    public byte[] getUserProfileImage(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        byte[] image = null;
        // Querying the profile_pic column specifically by ID
        Cursor cursor = db.rawQuery("SELECT profile_pic FROM users WHERE id=?", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            image = cursor.getBlob(0);
        }
        cursor.close();
        return image;
    }

    public boolean updateUserProfile(int id, String name, String phone, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fullname", name);
        values.put("phone", phone);

        // Only update image if the user actually picked a new one
        if (image != null) {
            values.put("profile_pic", image);
        }

        return db.update("users", values, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

}