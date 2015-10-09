package com.example.denis.firebasechat;

import android.app.Application;
import android.content.Context;

import com.firebase.client.Firebase;

/**
 * Created by denis on 09.10.15.
 */
public class FirebaseChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
