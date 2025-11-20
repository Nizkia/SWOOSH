package com.example.swooshv2;

public class Revenue {
    private String date;
    private double amount;
    private String pickupID;
    private String runnerID;
    private String userEmail;

    public Revenue(String date, double amount, String pickupID, String runnerID, String userEmail) {
        this.date = date;
        this.amount = amount;
        this.pickupID = pickupID;
        this.runnerID = runnerID;
        this.userEmail = userEmail;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPickupID() {
        return pickupID;
    }

    public void setPickupID(String pickupID) {
        this.pickupID = pickupID;
    }

    public String getRunnerID() {
        return runnerID;
    }

    public void setRunnerID(String runnerID) {
        this.runnerID = runnerID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}