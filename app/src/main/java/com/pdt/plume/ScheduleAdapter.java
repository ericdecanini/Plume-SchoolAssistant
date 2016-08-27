package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class ScheduleAdapter extends ArrayAdapter {

    // Staple adapter variables
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

        // If the row hasn't been used by the adapter before
        // create a new row
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            // Get references to the View Holder's views
            // by searching the row for the UI element
            holder = new ViewHolder();
            holder.icon = (ImageView)row.findViewById(R.id.schedule_icon);
            holder.lesson = (TextView)row.findViewById(R.id.schedule_lesson);
            holder.teacher = (TextView)row.findViewById(R.id.schedule_teacher);
            holder.room = (TextView)row.findViewById(R.id.schedule_room);
            holder.timeIn = (TextView)row.findViewById(R.id.schedule_time_in);
            holder.timeOut = (TextView)row.findViewById(R.id.schedule_time_out);

            row.setTag(holder);
        }

        // If the row is simply being recycled
        // Get the tag of the recycled row
        else {
            holder = (ViewHolder)row.getTag();
        }

        // Create a new list item using the data passed into the adapter
        Schedule schedule = data.get(position);

        // Set the UI elements contained in the View Holder
        // using data constructed in the Schedule class object
        Bitmap setImageBitmap = null;
        try {
            setImageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(schedule.scheduleIcon));
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.icon.setImageBitmap(setImageBitmap);
        holder.lesson.setText(schedule.scheduleLesson);
        holder.teacher.setText(schedule.scheduleTeacher);
        holder.room.setText(schedule.scheduleRoom);
        holder.timeIn.setText(schedule.scheduleTimeIn);
        holder.timeOut.setText(schedule.scheduleTimeOut);

        // Set the activated state of the list item if it's selected
        if(mItemSelected==position){
            row.setActivated(true);
        } else{
            row.setActivated(false);
        }

        return row;
    }

    public void setItemSelected(int position){
        mItemSelected=position;
    }

    static class ViewHolder {
        ImageView icon;
        TextView lesson;
        TextView teacher;
        TextView room;
        TextView timeIn;
        TextView timeOut;
    }

}
