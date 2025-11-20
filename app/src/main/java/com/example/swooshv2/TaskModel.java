package com.example.swooshv2;

public class TaskModel {
    private String id; // DocumentID
    private String dtPickupMade; // DTPickupMade
    private String uEmail; // UEmail
    private String customerName; // Customer Name (optional)
    private String dropOffPlace; // PPlaceOfDrop
    private int noOfItems;  // NoOfItems
    private String status; // Status
    private double amount; // Amount for the task

    // Constructor
    public TaskModel(String id, String dtPickupMade, String uEmail, int noOfItems, String status) {
        this.id = id;
        this.dtPickupMade = dtPickupMade;
        this.uEmail = uEmail;


        this.noOfItems = noOfItems;
        this.status = status;

    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDtPickupMade() {
        return dtPickupMade;
    }

    public String getUEmail() {
        return uEmail;
    }




    public int getNoOfItems() {
        return noOfItems;
    }

    public String getStatus() {
        return status;
    }


}
