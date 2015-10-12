package com.example.denis.firebasechat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by denis on 09.10.15.
 */
public class User implements Parcelable {

    public String name;
    public String email;
    public String avatarPath;

    public User() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.avatarPath);
    }

    protected User(Parcel in) {
        this.name = in.readString();
        this.email = in.readString();
        this.avatarPath = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
