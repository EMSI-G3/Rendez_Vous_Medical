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
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, fullname TEXT, email TEXT, phone TEXT, password TEXT, role TEXT, profile_pic BLOB)");
        db.execSQL("CREATE TABLE appointments (id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER, patient_name TEXT, date TEXT, time TEXT, status TEXT)");

        // SEED DATA: Role is "Medicine"
        db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Dr. House', 'doc@test.com', '123', 'Medicine')");
        db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Ms. Potts', 'sec@test.com', '123', 'Secretary')");
        db.execSQL("INSERT INTO users (fullname, email, password, role) VALUES ('Admin', 'admin@test.com', '123', 'Admin')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS appointments");
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

    public boolean checkUser(String email, String password, String role) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM users WHERE email=? AND password=? AND role=?", new String[]{email, password, role});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkEmail(String email) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM users WHERE email=?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public int getUserId(String email) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM users WHERE email=?", new String[]{email});
        if(c.moveToFirst()) return c.getInt(0);
        return -1;
    }

    public String getUserName(String email) {
        Cursor c = getReadableDatabase().rawQuery("SELECT fullname FROM users WHERE email=?", new String[]{email});
        if(c.moveToFirst()) return c.getString(0);
        return "User";
    }


    // --- APPOINTMENT LOGIC ---
    public boolean isSlotTaken(String date, String time) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM appointments WHERE date=? AND time=?", new String[]{date, time});
        boolean taken = cursor.getCount() > 0;
        cursor.close();
        return taken;
    }

    public boolean addAppointment(int patientId, String patientName, String date, String time) {
        if (isSlotTaken(date, time)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("patient_id", patientId);
        values.put("patient_name", patientName);
        values.put("date", date);
        values.put("time", time);
        values.put("status", "Pending");
        return db.insert("appointments", null, values) != -1;
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


    public List<TimeSlot> getAppointments(String role, int userId) {
        List<TimeSlot> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        // If Client, filter by ID. If Medicine/Secretary, show all.
        if (role.equals("Client")) {
            cursor = db.rawQuery("SELECT * FROM appointments WHERE patient_id=?", new String[]{String.valueOf(userId)});
        } else {
            cursor = db.rawQuery("SELECT * FROM appointments", null);
        }

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