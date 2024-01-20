package com.example.froyo;

import com.google.firebase.Timestamp;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Post {
    private String id;
    private String userEmail;
    private  ArrayList<String> images;
    private String majorTag;
    private ArrayList<String> hashTag;
    private String content;
    private int likes;
    // private ArrayList<String> comments;
    private List<Map<String, String>> comments;
    private Timestamp date;

    public Post(String id, String userEmail, ArrayList<String> imagesUrl, String majorTag, ArrayList<String> hashTag, String content, int likes, List<Map<String, String>> comments,  Timestamp date) {
        this.id = id;
        this.userEmail = userEmail;
        this.images = imagesUrl;
        this.majorTag = majorTag;
        this.hashTag = hashTag;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.date = date;
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

    public List<Map<String, String>> getComments() {
        return comments;
    }

    public void setComments(List<Map<String, String>> comments) {
        this.comments = comments;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
