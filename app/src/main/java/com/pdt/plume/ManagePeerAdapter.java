package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class ManagePeerAdapter extends ArrayAdapter {

    String LOG_TAG = ManagePeerAdapter.class.getSimpleName();

    Context context;
    int layoutResourceId;
    ArrayList<Peer> data = null;

    public ManagePeerAdapter(Context context, int layoutResourceId, ArrayList<Peer> data) {
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
            holder.delete = (ImageView) row.findViewById(R.id.delete);
            holder.edit = (ImageView) row.findViewById(R.id.edit);

            row.setTag(holder);
        }

        else {
            holder = (ViewHolder) row.getTag();
        }

        final Peer peer = data.get(position);
        holder.name.setText(peer.peerName);
        int textColor = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.KEY_THEME_TITLE_COLOUR), context.getResources().getColor(R.color.gray_900));
        holder.name.setTextColor(textColor);

        // Set the icon
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final String mUserId = mFirebaseUser.getUid();

        if (peer.peerIcon != null) {
            holder.icon.setImageURI(Uri.parse(peer.peerIcon));
            holder.icon.setTag(peer.peerIcon);
        }

        // Set the actions of edit and delete
        final String peerName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.TEMP_MANAGING_PEER), "");

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.manage_class_delete_title, peer.peerName, peerName))
                        .setMessage(context.getString(R.string.manage_class_delete_text, peerName))
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(mUserId).child("peers")
                                        .child(peer.id).child("classes").child(peer.peerName)
                                        .removeValue();
                                FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(peer.id).child("peers")
                                        .child(mUserId).child("classes").child(peer.peerName)
                                        .removeValue();
                            }
                        }).show();
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(context, MatchClassesActivity.class);
//                intent.putExtra(context.getString(R.string.INTENT_EXTRA_TITLE), peer.peerName);
//                context.startActivity(intent);
            }
        });

        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        ImageView delete;
        ImageView edit;
    }

}
