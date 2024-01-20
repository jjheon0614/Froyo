package com.example.froyo;

public class ListUser {
    private String email;
    private String username;
    private String imageUrl;

    public ListUser(String email, String username, String imageUrl) {
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
