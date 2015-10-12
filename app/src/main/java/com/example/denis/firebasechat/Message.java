package com.example.denis.firebasechat;

/**
 * Created by denis on 09.10.15.
 */
public class Message {

    public String message;
    public String uid;

    public Message() {}

    public Message( String uid, String message) {
        this.message = message;
        this.uid = uid;
    }
}
