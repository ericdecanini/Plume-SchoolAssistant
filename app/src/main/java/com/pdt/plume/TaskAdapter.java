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
import java.util.Calendar;

public class TaskAdapter extends ArrayAdapter {

    // Constantly used variables
    Utility utility = new Utility();

    // Staple adapter variables
    Context context;
    int layoutResourceId;
    ArrayList<Task> data = null;

    // Default public constructor
    public TaskAdapter(Context context, int layoutResourceId, ArrayList<Task> data) {
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
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            // Get references to the View Holder's views
            // by searching the row for the UI element
            holder = new ViewHolder();
            holder.icon = (ImageView)row.findViewById(R.id.task_icon);
            holder.title = (TextView)row.findViewById(R.id.task_title);
            holder.shared = (TextView)row.findViewById(R.id.task_shared);
            holder.description = (TextView)row.findViewById(R.id.task_description);
            holder.attachment = (TextView)row.findViewById(R.id.task_attachment);
            holder.date = (TextView)row.findViewById(R.id.task_date);

            row.setTag(holder);
        }

        // If the row is simply being recycled
        // Get the tag of the recycled row
        else {
            holder = (ViewHolder)row.getTag();
        }

        // Create a new list item using the data passed into the adapter
        Task task = data.get(position);

        // Set the UI elements contained in the View Holder
        // using data constructed in the Task class object
        Bitmap setImageBitmap = null;
        try {
            setImageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(task.taskIcon));
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.icon.setImageBitmap(setImageBitmap);
        holder.title.setText(task.taskTitle);
        holder.shared.setText(task.taskShared);
        if (task.taskShared.equals(""))
            holder.shared.setVisibility(View.GONE);
        holder.description.setText(task.taskDescription);
        holder.attachment.setText(task.taskAttachment);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(((long)task.taskDueDate));
        holder.date.setText(context.getString(R.string.due_date) + " "
               + utility.formatDateString(context, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));

        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView shared;
        TextView description;
        TextView attachment;
        TextView date;
    }


}
