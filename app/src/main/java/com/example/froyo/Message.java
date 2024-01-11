package com.example.froyo;

public class Message {
    public Boolean isMine;
    public Boolean isSearched;
    public String message;
    public String searchStr;
    public String id;
    public String time;

    public Message(Boolean isMine, Boolean isSearched, String message, String searchStr, String time, String id){
        this.isMine = isMine;
        this.isSearched = isSearched;
        this.message = message;
        this.searchStr = searchStr;
        this.time = time;
        this.id = id;
    };
}

