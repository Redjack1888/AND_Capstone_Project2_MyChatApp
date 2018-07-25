package com.example.alessandro.mychatapp.models;

public class Chat {

    String lastMessageKey;

    public Chat() {

    }

    public Chat(String lastMessageKey) {
        this.lastMessageKey = lastMessageKey;
    }

    public String getLastMessageKey() {
        return lastMessageKey;
    }

    public void setLastMessageKey(String lastMessageKey) {
        this.lastMessageKey = lastMessageKey;
    }

}