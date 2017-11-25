package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


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
            holder.more = (ImageView) row.findViewById(R.id.more);

            row.setTag(holder);
        }

        else {
            holder = (ViewHolder) row.getTag();
        }

        final Peer peer = data.get(position);
        Log.v(LOG_TAG, "Peer name: " + peer.peerName);
        holder.name.setText(peer.peerName);
        int textColor = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(context.getString(R.string.KEY_THEME_TEXT_COLOUR), context.getResources().getColor(R.color.gray_900));
        holder.name.setTextColor(textColor);

        final int menuRes;
        if (peer.id.equals("")) menuRes = R.menu.menu_manage_class;
        else menuRes = R.menu.menu_peer;
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                         if (item.getItemId() == R.id.action_remove) {
                            FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            final String mUserId = mFirebaseUser.getUid();
                            final String uid = peer.id;
                            String profileName = peer.peerName;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(context.getString(R.string.dialog_remove_peer, profileName))
                                    .setNegativeButton(context.getString(R.string.cancel), null)
                                    .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // Remove the peer from the peers tab
                                            final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                                                    .child("users");
                                            usersRef.child(mUserId).child("peers").child(uid).removeValue();
                                            usersRef.child(uid).child("peers").child(mUserId).removeValue();

                                            // Remove the peer from each class
                                            final DatabaseReference classesRef = usersRef.child(mUserId).child("classes");
                                            classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                                                        DatabaseReference peerRef = classesRef.child(classSnapshot.getKey())
                                                                .child("peers").child(uid);
                                                        if (peerRef != null)
                                                            peerRef.removeValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            final DatabaseReference classesRef1 = usersRef.child(uid).child("classes");
                                            classesRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                                                        DatabaseReference peerRef = classesRef1.child(classSnapshot.getKey())
                                                                .child("peers").child(mUserId);
                                                        if (peerRef != null)
                                                            peerRef.removeValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            // Remove peer from the adapter
                                            PeerAdapter.this.remove(peer);
                                            PeerAdapter.this.notifyDataSetChanged();
                                        }
                                    })
                                    .show();
                            return true;
                        } else if (item.getItemId() == R.id.action_delete) {
                             FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                             final String mUserId;
                             if (mFirebaseUser != null) {
                                 mUserId = mFirebaseUser.getUid();
                             } else if (peer.id.equals("") || peer.id == null) {
                                 mUserId = peer.peerName;
                             } else mUserId = peer.id;
                             String profileName = peer.peerName;
                             AlertDialog.Builder builder = new AlertDialog.Builder(context);
                             builder.setTitle(context.getString(R.string.dialog_delete_class_title))
                                     .setMessage(context.getString(R.string.dialog_delete_class_text, profileName))
                                     .setNegativeButton(context.getString(R.string.cancel), null)
                                     .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialogInterface, int i) {
                                             // Remove the peer from the peers tab
                                             String uid = PreferenceManager.getDefaultSharedPreferences(context)
                                                     .getString(context.getString(R.string.TEMP_MANAGING_PEER), "");
                                             final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                                                     .child("users");
                                             usersRef.child(mUserId).child("peers").child(uid).child("classes").child(peer.peerName)
                                                     .removeValue();
                                             usersRef.child(mUserId).child("classes").child(peer.peerName).child("peers").child(uid)
                                                     .removeValue();
                                             usersRef.child(uid).child("peers").child(mUserId).child("classes").child(peer.peerName)
                                                     .removeValue();
                                             usersRef.child(uid).child("classes").child(peer.peerName).child("peers").child(mUserId)
                                                     .removeValue();
                                             PeerAdapter.this.remove(peer);
                                             PeerAdapter.this.notifyDataSetChanged();
                                         }
                                     })
                                     .show();
                             return true;
                         } else return false;
                    }
                });
                popupMenu.show();
            }
        });

        // Set the icon
        if (peer.peerIcon != null) {
            holder.icon.setImageURI(Uri.parse(peer.peerIcon));
            holder.icon.setTag(peer.peerIcon);
        }
        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        ImageView more;
    }

}
