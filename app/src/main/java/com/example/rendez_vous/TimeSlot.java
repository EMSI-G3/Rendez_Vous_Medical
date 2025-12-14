package com.example.rendez_vous;

public class TimeSlot {
    private int id; // Added ID for Database
    private String date;
    private String time;
    private String status;

    // Constructor without ID (for creating new slots before saving to DB)
    public TimeSlot(String date, String time, String status) {
        this.date = date;
        this.time = time;
        this.status = status;
    }

    // Constructor with ID (for reading from DB)
    public TimeSlot(int id, String date, String time, String status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}