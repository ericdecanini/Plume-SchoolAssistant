package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ScheduleAdapter extends ArrayAdapter {

    Context context;
    int layoutResourceId;
    Schedule data[] = null;
    private int mItemSelected = -1 ;

    public ScheduleAdapter(Context context, int layoutResourceId, Schedule[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.icon = (ImageView)row.findViewById(R.id.schedule_icon);
            holder.lesson = (TextView)row.findViewById(R.id.schedule_lesson);
            holder.teacher = (TextView)row.findViewById(R.id.schedule_teacher);
            holder.room = (TextView)row.findViewById(R.id.schedule_room);
            holder.timeIn = (TextView)row.findViewById(R.id.schedule_time_in);
            holder.timeOut = (TextView)row.findViewById(R.id.schedule_time_out);

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        Schedule schedule = data[position];
        holder.icon.setImageResource(schedule.scheduleIcon);
        holder.lesson.setText(schedule.scheduleLesson);
        holder.teacher.setText(schedule.scheduleTeacher);
        holder.room.setText(schedule.scheduleRoom);
        holder.timeIn.setText(schedule.scheduleTimeIn);
        holder.timeOut.setText(schedule.scheduleTimeOut);

        if(mItemSelected==position){
            row.setActivated(true);
        }else{
            row.setActivated(false);
        }

        return row;
    }

    public void setItemSelected(int position){
        mItemSelected=position;
    }

    static class ViewHolder
    {
        ImageView icon;
        TextView lesson;
        TextView teacher;
        TextView room;
        TextView timeIn;
        TextView timeOut;
    }


}
