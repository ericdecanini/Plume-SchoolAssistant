package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
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



public class MismatchListAdapter extends ArrayAdapter {

    String LOG_TAG = MismatchListAdapter.class.getSimpleName();

    Context context;
    int layoutResourceId;
    ArrayList<Bundle> data = null;
    ArrayList<String> classTitles = new ArrayList<>();
    ArrayList<String> classIcons = new ArrayList<>();

    public MismatchListAdapter(Context context, int layoutResourceId, ArrayList<Bundle> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
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
            holder.dropdownText.setTag("null");
            holder.dropdown.setTag(data.get(position).getString("icon"));

            // Add the dropdown to the linear layout
            final PopupMenu popupMenu = new PopupMenu(context, holder.dropdown);

            // Get the schedule list from Firebase
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            String userId = firebaseUser.getUid();
            final DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("classes");
            classesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                        // Roll through each class and add it to a list of Schedules
                        ArrayList<Schedule> classesList = new ArrayList<>();
                        String title = classSnapshot.getKey();
                        String icon = classSnapshot.child("icon").getValue(String.class);
                        String teacher = classSnapshot.child("teacher").getValue(String.class);
                        String room = classSnapshot.child("room").getValue(String.class);
                        classesList.add(new Schedule(context, icon, title, teacher, room, "", "", ""));

                        classTitles.add(title);
                        classIcons.add(icon);

                        for (int i = 0; i < classesList.size(); i++) {
                            popupMenu.getMenu().add(classesList.get(i).scheduleLesson);
                        }

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Bundle bundle = data.get(position);
                                if (menuItem.getTitle().equals(context.getString(R.string.add_new_class))) {
                                    // Get the data of the class and add it as a new class
                                    DatabaseReference newClassRef = classesRef.child(bundle.getString("title"));
                                    // Get key data first
                                    newClassRef.child("icon").setValue(bundle.getString("icon"));
                                    newClassRef.child("teacher").setValue(bundle.getString("teacher"));
                                    newClassRef.child("room").setValue(bundle.getString("room"));
                                    // Get all the listed data
                                    ArrayList<String> occurrences = bundle.getStringArrayList("occurrence");
                                    Log.v(LOG_TAG, "Occurrences Size: " + occurrences.size());
                                    ArrayList<Integer> timeins = bundle.getIntegerArrayList("timein");
                                    ArrayList<Integer> timeouts = bundle.getIntegerArrayList("timeout");
                                    ArrayList<Integer> timeinalts = bundle.getIntegerArrayList("timeinalt");
                                    ArrayList<Integer> timeoutalts = bundle.getIntegerArrayList("timeoutalt");
                                    ArrayList<String> periods = bundle.getStringArrayList("periods");
                                    for (int i = 0; i < occurrences.size(); i++) {
                                        newClassRef.child("occurrence").child(occurrences.get(i)).setValue("");
                                        newClassRef.child("timein").child(String.valueOf(i)).setValue(timeins.get(i));
                                        newClassRef.child("timeout").child(String.valueOf(i)).setValue(timeouts.get(i));
                                        newClassRef.child("timeinalt").child(String.valueOf(i)).setValue(timeinalts.get(i));
                                        newClassRef.child("timeoutalt").child(String.valueOf(i)).setValue(timeoutalts.get(i));
                                        newClassRef.child("periods").child(periods.get(i)).setValue("");
                                    }

                                    Toast.makeText(context, bundle.getString("title") + " " + context.getString(R.string.new_schedule_toast_class_inserted), Toast.LENGTH_SHORT).show();
                                    holder.dropdownText.setText(bundle.getString("title"));
                                    holder.dropdownText.setTag(bundle.getString("title"));
                                } else {
                                    holder.dropdownText.setText(menuItem.getTitle());
                                    holder.dropdownText.setTag(menuItem.getTitle());
                                    int position = -1;
                                    for (int i = 0; i < classTitles.size(); i++)
                                        if (classTitles.get(i).equals(menuItem.getTitle()))
                                            position = i;

                                    String icon = classIcons.get(position);
                                    if (position != -1)
                                        holder.dropdown.setTag(icon);
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

        holder.title.setText(data.get(position).getString("title"));

        return row;
    }

    static class ViewHolder {
        TextView title;
        LinearLayout dropdown;
        TextView dropdownText;
    }

}
