package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;

public class ScheduleAdapter extends ArrayAdapter {
    String LOG_TAG = ScheduleAdapter.class.getSimpleName();

    // Staple mTasksAdapter variables
    Context context;
    int layoutResourceId;
    ArrayList<Schedule> data = null;
    private int mItemSelected = -1 ;

    // Default public constructor
    public ScheduleAdapter(Context context, int layoutResourceId, ArrayList<Schedule> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Initialise variables for the Row and View Holder
        View row = convertView;
        ViewHolder holder = null;

        // Create a new list item using the data passed into the mTasksAdapter
        final Schedule schedule = data.get(position);

        // If the row hasn't been used by the mTasksAdapter before
        // create a new row
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            // Get references to the View Holder's views
            // by searching the row for the UI element
            holder = new ViewHolder();
            if (schedule.scheduleIcon != null && !schedule.scheduleIcon.contains("art_"))
                holder.icon = (ImageView)row.findViewById(R.id.schedule_icon2);
            else holder.icon = (ImageView)row.findViewById(R.id.schedule_icon);
            holder.lesson = (TextView)row.findViewById(R.id.schedule_lesson);
            holder.teacher = (TextView)row.findViewById(R.id.schedule_teacher);
            holder.room = (TextView)row.findViewById(R.id.schedule_room);
            holder.timeIn = (TextView)row.findViewById(R.id.schedule_time_in);

            row.setTag(holder);
        }

        // If the row is simply being recycled
        // Get the tag of the recycled row
        else {
            holder = (ViewHolder)row.getTag();
        }

        int textColor = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.KEY_THEME_TITLE_COLOUR), context.getResources().getColor(R.color.gray_900));
        float alpha = 0.7f;
        if (holder.lesson != null)
            holder.lesson.setTextColor(textColor);

        if (holder.teacher != null) {
            holder.teacher.setTextColor(textColor);
            holder.teacher.setAlpha(alpha);
            holder.room.setTextColor(textColor);
            holder.room.setAlpha(alpha);
            holder.timeIn.setTextColor(textColor);
            holder.timeIn.setAlpha(alpha);
        } else {
            CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkbox);
            if (checkBox != null) checkBox.setButtonTintList(ColorStateList.valueOf(textColor));
        }

        // Set the UI elements contained in the View Holder
        // using data constructed in the Schedule class object
        if (schedule.scheduleIcon != null) {
            Uri uri = Uri.parse(schedule.scheduleIcon);
            holder.icon.setImageURI(uri);
        }

        holder.lesson.setTypeface(Typeface.createFromAsset(context.getAssets(), "roboto_slab_bold.ttf"));
        if (schedule.scheduleLesson.contains("%0513%")) {
            if (schedule.scheduleLesson.split("%0513%")[1].equals("cross")) {
                holder.lesson.setText(schedule.scheduleLesson.split("%0513%")[0]);
                row.setAlpha(0.5f);
                row.findViewById(R.id.checkbox).setClickable(true);
                row.findViewById(R.id.checkbox).setEnabled(false);
                row.setEnabled(false);
                row.setClickable(true);
            }}
        else holder.lesson.setText(schedule.scheduleLesson);

        // If the teacher view returns null, an alternate layout is being used
        if (holder.teacher != null) {
            holder.teacher.setText(schedule.scheduleTeacher);
            holder.room.setText(schedule.scheduleRoom);

            if (schedule.scheduleTimeOut.equals("period")) {
                // Period based format
                holder.timeIn.setText(context.getString(R.string.format_period,
                        schedule.scheduleTimeIn));
            } else if (schedule.scheduleTimeIn.equals("") || schedule.scheduleTimeIn.equals(" "))
                holder.timeIn.setText("");
            else {
                // Time based format
                holder.timeIn.setText(context.getString(R.string.format_time,
                        schedule.scheduleTimeIn, schedule.scheduleTimeOut));
            }

        }

        // Set the activated state of the list item if it's selected
        if(mItemSelected==position){
            row.setActivated(true);
        } else{
            row.setActivated(false);
        }

        // If a layout with a menu is used, add a popup menu to that view
        holder.menuIcon = (ImageView) row.findViewById(R.id.menu);
        if (holder.menuIcon != null) {
            final PopupMenu menu = new PopupMenu(context, holder.menuIcon);
            menu.inflate(R.menu.menu_schedule_item);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_remove:
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(context.getString(R.string.dialog_remove_class, "Chardo", schedule.scheduleLesson) )
                                    .setNegativeButton(context.getString(R.string.cancel), null)
                                    .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // Remove the class from Firebase on both users' peer
                                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                            String userId = firebaseUser.getUid();
                                            String profileUserId = ((String) schedule.extra);

                                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                                                    .child("users");
                                            usersRef.child(userId).child("peers").child(profileUserId)
                                                    .child(schedule.scheduleLesson).removeValue();
                                            usersRef.child(profileUserId).child("peers").child(userId)
                                                    .child(schedule.scheduleLesson).removeValue();
                                            notifyDataSetChanged();
                                        }
                                    }).show();
                            return true;
                    }
                    return false;
                }
            });

            holder.menuIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menu.show();
                }
            });
        }

        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView lesson;
        TextView teacher;
        TextView room;
        TextView timeIn;
        ImageView menuIcon;
    }

}
