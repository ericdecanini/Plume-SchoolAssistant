package com.pdt.plume;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

import static com.pdt.plume.R.id.room;
import static com.pdt.plume.R.id.teacher;



public class MatchingClass implements Parcelable {

    public String icon;
    public String title;
    public String originalIcon;
    public String originalTitle;
    public int activated;

    public MatchingClass() {
        super();
    }



    public MatchingClass(String icon, String title,
                         String originalIcon, String originalTitle) {
        super();
        this.icon = icon;
        this.title = title;
        this.originalIcon = originalIcon;
        this.originalTitle = originalTitle;
    }

    protected MatchingClass(Parcel in) {
        icon = in.readString();
        title = in.readString();
        originalIcon = in.readString();
        originalTitle = in.readString();
    }

    public static final Creator<MatchingClass> CREATOR = new Creator<MatchingClass>() {
        @Override
        public MatchingClass createFromParcel(Parcel in) {
            return new MatchingClass(in);
        }

        @Override
        public MatchingClass[] newArray(int size) {
            return new MatchingClass[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(icon);
        parcel.writeString(title);
        parcel.writeString(originalIcon);
        parcel.writeString(originalTitle);
    }
}
