package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;
import java.util.HashMap;


public class MismatchListAdapter extends ArrayAdapter {

    String LOG_TAG = MismatchListAdapter.class.getSimpleName();

    Context context;
    int layoutResourceId;
    ArrayList<String> data = new ArrayList<>();
    String uid;

    public MismatchListAdapter(Context context, int layoutResourceId, ArrayList<String> data, String uid) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.uid = uid;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) row.findViewById(R.id.title);
            holder.dropdown = (LinearLayout) row.findViewById(R.id.field_class_dropdown);
            holder.dropdownText = (TextView) row.findViewById(R.id.field_class_textview);

            // Add the dropdown to the linear layout
            final PopupMenu popupMenu = new PopupMenu(context, holder.dropdown);

            // Get the schedule list from Firebase
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            String userId = firebaseUser.getUid();
            final DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("classes");
            classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                        // Roll through each class and add it to a list of Schedules
                        final ArrayList<Schedule> classesList = new ArrayList<>();
                        String title = classSnapshot.getKey();
                        String icon = classSnapshot.child("icon").getValue(String.class);
                        String teacher = classSnapshot.child("teacher").getValue(String.class);
                        String room = classSnapshot.child("room").getValue(String.class);
                        classesList.add(new Schedule(context, icon, title, teacher, room, "", "", ""));

                        for (int i = 0; i < classesList.size(); i++) {
                            popupMenu.getMenu().add(classesList.get(i).scheduleLesson);
                        }

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                if (menuItem.getTitle().equals(context.getString(R.string.add_new_class))) {
                                    // Get the data of the class and add it as a new class
                                    final String title = data.get(position);
                                    final DatabaseReference newClassRef = classesRef.child(title);

                                    Log.v(LOG_TAG, "UID: " + uid + ", newClassRef: " + newClassRef + ", Title: " + title);
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(uid).child("classes")
                                            .child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String icon = dataSnapshot.child("icon").getValue(String.class);
                                            String teacher = dataSnapshot.child("teacher").getValue(String.class);
                                            String room = dataSnapshot.child("room").getValue(String.class);
                                            int i00 = 0;
                                            for (DataSnapshot arraySnapshot: dataSnapshot.child("occurrence").getChildren()) {
                                                newClassRef.child("occurrence").child(String.valueOf(i00)).setValue(arraySnapshot.getKey());
                                                i00++;
                                            }
                                            int i0 = 0;
                                            for (DataSnapshot arraySnapshot: dataSnapshot.child("periods").getChildren()) {
                                                newClassRef.child("periods").child(String.valueOf(i0)).setValue(arraySnapshot.getKey());
                                                i0++;
                                            }
                                            int i1 = 0;
                                            for (DataSnapshot arraySnapshot: dataSnapshot.child("timein").getChildren()) {
                                                newClassRef.child("timein").child(String.valueOf(i1)).setValue(arraySnapshot.getValue(long.class));
                                                i1++;
                                            }
                                            int i2 = 0;
                                            for (DataSnapshot arraySnapshot: dataSnapshot.child("timeout").getChildren()) {
                                                newClassRef.child("timeout").child(String.valueOf(i2)).setValue(arraySnapshot.getValue(long.class));
                                                i2++;
                                            }
                                            int i3 = 0;
                                            for (DataSnapshot arraySnapshot: dataSnapshot.child("timeinalt").getChildren()) {
                                                newClassRef.child("timeinalt").child(String.valueOf(i3)).setValue(arraySnapshot.getValue(long.class));
                                                i3++;
                                            }
                                            int i4 = 0;
                                            for (DataSnapshot arraySnapshot: dataSnapshot.child("timeoutalt").getChildren()) {
                                                newClassRef.child("timeoutalt").child(String.valueOf(i4)).setValue(arraySnapshot.getValue(long.class));
                                                i4++;
                                            }

                                            newClassRef.child("icon").setValue(icon);
                                            newClassRef.child("teacher").setValue(teacher);
                                            newClassRef.child("room").setValue(room);

                                            popupMenu.getMenu().add(title);
                                            holder.dropdownText.setText(title);
                                            holder.dropdownText.setTag(title);
                                            holder.dropdown.setTag(icon);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                } else {
                                    holder.dropdownText.setText(menuItem.getTitle());
                                    holder.dropdownText.setTag(menuItem.getTitle());
                                    int position = -1;
                                    for (int i = 0; i < data.size(); i++)
                                        if (data.get(i).equals(menuItem.getTitle()))
                                            position = i;


                                    if (position != -1){
                                        String icon = data.get(position);
                                        holder.dropdown.setTag(icon);
                                    }

                                }
                                return true;
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            popupMenu.getMenu().add(context.getString(R.string.add_new_class));

            holder.dropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.show();
                }
            });


            row.setTag(holder);
        }

        else {
            holder = (ViewHolder) row.getTag();
        }

        holder.title.setText(data.get(position));

        return row;
    }

    static class ViewHolder {
        TextView title;
        LinearLayout dropdown;
        TextView dropdownText;
    }

}
