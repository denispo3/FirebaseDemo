package com.example.denis.firebasechat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by denis on 14.10.15.
 */
public abstract class SharedPrefUtils {

    public static final String SHARED_PREFS_USER_TOKEN_KEY = "sp_user_token";

    public static void saveToken(Activity ctx, String token) {
        SharedPreferences sp = ctx.getPreferences(Context.MODE_PRIVATE);
        sp.edit().putString(SHARED_PREFS_USER_TOKEN_KEY, token).apply();
    }

    public static String getToken(Activity ctx) {
        SharedPreferences sp = ctx.getPreferences(Context.MODE_PRIVATE);
        return sp.getString(SHARED_PREFS_USER_TOKEN_KEY, "");
    }

    public static void clearToken(Activity ctx) {
        saveToken(ctx, "");
    }
}
