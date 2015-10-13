package com.example.denis.firebasechat;

import com.firebase.client.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by denis on 09.10.15.
 */
public class Message {

    public String message;
    public String uid;
    public long timeStamp;

    public Message() {
    }

    public Message(String uid, String message) {
        this.message = message;
        this.uid = uid;
    }

    public Map<String, Object> getObjectMapping() {
        Map<String, Object> msgMap = new HashMap<>();
        // Key and variable name in mapping should be the same
        msgMap.put("uid", uid);
        msgMap.put("message", message);
        Map<String, String> timeStampMap = new HashMap<>();
        timeStampMap.put(".sv", "timestamp");
        msgMap.put("timeStamp", timeStampMap);
        return msgMap;
    }
}
