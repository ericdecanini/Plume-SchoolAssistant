package com.pdt.plume;

import android.content.Context;

/**
 * Created by user on 03/12/2016.
 */

public class Peer {

    public String peerIcon;
    public String peerName;
    public String id;

    public Peer() {
        super();
    }

    public Peer(String icon, String name, String id) {
        super();
        this.peerIcon = icon;
        this.peerName = name;
        this.id = id;
    }

}
