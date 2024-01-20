package com.example.froyo;

public class StripeData {
    private String email;
    private String customerID;

    // Required public no-argument constructor
    public StripeData() {
    }

    public StripeData(String email, String customerID) {
        this.email = email;
        this.customerID = customerID;
    }

    // Add getters and setters as needed
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
}
