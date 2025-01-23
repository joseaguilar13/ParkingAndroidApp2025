package com.navix.mapboxvainilla;

public class ReservationRequest {
    private String userID;
    private String slotID;
    private String Location;
    private String startTime;
    private int duration;

    public ReservationRequest(String userID, String slotID, String Location, String startTime, int duration) {
        this.userID = userID;
        this.slotID = slotID;
        this.Location = Location;
        this.startTime = startTime;
        this.duration = duration;
    }

    // Getters y setters
    // ...
}