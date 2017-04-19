package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class TaskAdapter extends ArrayAdapter {

    // Constantly used variables
    String LOG_TAG = TaskAdapter.class.getSimpleName();
    Utility utility = new Utility();

    // Staple mScheduleAdapter variables
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

        // Create a new list item using the data passed into the mScheduleAdapter
        Task task = data.get(position);

        // If the row hasn't been used by the mScheduleAdapter before
        // create a new row
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            // Get references to the View Holder's views
            // by searching the row for the UI element
            holder = new ViewHolder();
            if (task.taskIcon.equals("storage") || !task.taskIcon.contains("/art_"))
                holder.icon = (ImageView) row.findViewById(R.id.task_icon2);
            else holder.icon = (ImageView)row.findViewById(R.id.task_icon);
            holder.icon.setTag(task.taskIcon);
            holder.title = (TextView)row.findViewById(R.id.task_title);
            holder.taskClass = (TextView) row.findViewById(R.id.task_class);
            holder.taskType = (TextView) row.findViewById(R.id.task_type);
            holder.date = (TextView)row.findViewById(R.id.task_date);

            row.setTag(holder);
        }

        // If the row is simply being recycled
        // Get the tag of the recycled row
        else {
            holder = (ViewHolder)row.getTag();
        }

        // Set the UI elements contained in the View Holder
        // using data constructed in the Task class object
        Bitmap setImageBitmap = null;
        if (task.customIcon == null)
        try {
            Uri uri = Uri.parse(task.taskIcon);
            File file = new File(uri.getPath());
            setImageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        } else {
            setImageBitmap = task.customIcon;
        }
        holder.icon.setImageBitmap(setImageBitmap);
        holder.title.setText(task.taskTitle);
        holder.title.setTypeface(Typeface.createFromAsset(context.getAssets(), "roboto_slab_bold.ttf"));

        // If this returns null, list_item_task2 is being used which only has icon and title
        if (holder.taskClass != null) {
            holder.taskClass.setText(task.taskClass);
            holder.taskType.setText(task.taskType);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(((long) task.taskDueDate));
            holder.date.setText(context.getString(R.string.due_date) + " "
                    + utility.formatDateString(context, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
        }

        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView taskClass;
        TextView taskType;
        TextView date;
    }


}