package com.example.rendez_vous.Medicine;

public class TimeSlot {
    private int id;
    private String date;
    private String time;
    private String status;
    private String patientName; // New Field

    public TimeSlot(int id, String date, String time, String status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getPatientName() { return patientName; }

    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setStatus(String status) { this.status = status; }
}