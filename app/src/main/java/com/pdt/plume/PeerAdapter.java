package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class PeerAdapter extends ArrayAdapter {

    String LOG_TAG = PeerAdapter.class.getSimpleName();

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
        final ViewHolder holder;

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

        final Peer peer = data.get(position);
        holder.name.setText(peer.peerName);

        // Set the icon
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String mUserId = mFirebaseUser.getUid();

        holder.icon.setImageURI(Uri.parse(peer.peerIcon));

        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
    }

}
