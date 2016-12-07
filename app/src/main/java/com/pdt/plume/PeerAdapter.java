package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;


public class PeerAdapter extends ArrayAdapter {

    Context context;
    int layoutResourceId;
    ArrayList<Peer> data = null;

    public PeerAdapter(Context context, int layoutResourceId, ArrayList<Peer> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.icon = (ImageView) row.findViewById(R.id.icon);
            holder.name = (TextView) row.findViewById(R.id.name);

            row.setTag(holder);
        }

        else {
            holder = (ViewHolder) row.getTag();
        }

        Peer peer = data.get(position);

//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(peer.peerIcon));
//            holder.icon.setImageBitmap(bitmap);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        holder.name.setText(peer.peerName);

        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
    }

}
