package com.example.froyo;
// ha
public class ChatRoom{
    public String profile = "https://images.pexels.com/photos/416160/pexels-photo-416160.jpeg?auto=compress&cs=tinysrgb&w=1200";
    public String title;
    public String currentMessage;
    public String chatId;
    public int numPeople;
    public int nonCheckedMessage;
    public String time;

    public ChatRoom(String title, String currentMessage, String chatId, int numPeople, int nonCheckedMessage, String time){
        this.title = title;
        this.currentMessage = currentMessage;
        this.chatId = chatId;
        this.numPeople = numPeople;
        this.nonCheckedMessage = nonCheckedMessage;
        this.time = time;
    };
}

