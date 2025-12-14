package com.example.rendez_vous;
public class TimeSlot {
    String date;
    String time;
    String status;

    public TimeSlot(String date, String time, String status) {
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
}
