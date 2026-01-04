package com.example.rendez_vous.AccessPoint;

import java.util.List;

public class Clinic {
    private int id;
    private String name;
    private String location;
    private List<String> doctors;

    public Clinic(int id, String name, String location, List<String> doctors) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.doctors = doctors;
    }

    public String getName() { return name; }
    public String getLocation() { return location; }
    public List<String> getDoctors() { return doctors; }

    public int getId() {
        return id;
    }
}