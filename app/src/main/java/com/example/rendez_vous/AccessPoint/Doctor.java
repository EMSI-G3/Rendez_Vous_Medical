package com.example.rendez_vous.AccessPoint;

public class Doctor {
    private int id;
    private String name;
    private String specialty;
    private boolean available;
    private byte[] profilePic;
    public Doctor(int id, String name, String specialty, boolean available, byte[] profilePic) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.available = available;
        this.profilePic = profilePic;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }

    public byte[] getProfilePic() { return profilePic; }

}