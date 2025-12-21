package com.example.rendez_vous.Medicine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rendezvous.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SLOTS = "slots";
    private static final String COL_ID = "id";
    private static final String COL_DATE = "date";
    private static final String COL_TIME = "time";
    private static final String COL_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SLOTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT, " +
                COL_TIME + " TEXT, " +
                COL_STATUS + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLOTS);
        onCreate(db);
    }

    // 1. CREATE: Add a new slot
    public boolean addSlot(TimeSlot slot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, slot.getDate());
        values.put(COL_TIME, slot.getTime());
        values.put(COL_STATUS, slot.getStatus());

        long result = db.insert(TABLE_SLOTS, null, values);
        return result != -1;
    }

    // 2. READ: Get all slots
    public List<TimeSlot> getAllSlots() {
        List<TimeSlot> slotList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SLOTS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));
                slotList.add(new TimeSlot(id, date, time, status));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return slotList;
    }

    // 3. UPDATE: Book a slot (Change status)
    public void updateSlotStatus(int id, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, newStatus);
        db.update(TABLE_SLOTS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // 4. DELETE: Remove a slot
    public void deleteSlot(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SLOTS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}