package com.example.froyo;

import java.util.ArrayList;

public class Post {
    private String id;
    private String userEmail;
    private  ArrayList<String> images;
    private String majorTag;
    private ArrayList<String> hashTag;
    private String content;
    private int likes;
    private ArrayList<String> comments;

    public Post(String id, String userEmail, ArrayList<String> imagesUrl, String majorTag, ArrayList<String> hashTag, String content, int likes, ArrayList<String> comments) {
        this.id = id;
        this.userEmail = userEmail;
        this.images = imagesUrl;
        this.majorTag = majorTag;
        this.hashTag = hashTag;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
    }

//    public Post(){
//        this.id = "";
//        this.userEmail = "";
//        this.images = new ArrayList<>();
//        this.majorTag = "";
//        this.hashTag = new ArrayList<>();
//        this.content = "";
//        this.likes = 0;
//        this.comments = new ArrayList<>();
//    }
    public Post() {
        // Required empty public constructor for Firestore
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getMajorTag() {
        return majorTag;
    }

    public void setMajorTag(String majorTag) {
        this.majorTag = majorTag;
    }

    public ArrayList<String> getHashTag() {
        return hashTag;
    }

    public void setHashTag(ArrayList<String> hashTag) {
        this.hashTag = hashTag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }
}
