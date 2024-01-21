package com.example.froyo;
// ha
public class ChatRoom{
    public String profile;
    public String title;
    public String currentMessage;
    public String chatId;
    public int numPeople;
    public int nonCheckedMessage;
    public String time;

    public ChatRoom(String profile, String title, String currentMessage, String chatId, int numPeople, int nonCheckedMessage, String time){
        this.profile = profile;
        this.title = title;
        this.currentMessage = currentMessage;
        this.chatId = chatId;
        this.numPeople = numPeople;
        this.nonCheckedMessage = nonCheckedMessage;
        this.time = time;
    }
}

