package com.pdt.plume;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by user on 03/12/2016.
 */

public class User implements Serializable {

    public String icon;
    public String name;
    public String flavour;
    public String id;

    public User() {
        super();
    }

    public User(String icon, String name, String flavour, String id) {
        super();
        this.icon = icon;
        this.name = name;
        this.flavour = flavour;
        this.id = id;
    }

}
