package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.textColor;
import static com.pdt.plume.R.string.due;
import static com.pdt.plume.R.string.schedule;

public class TaskAdapter extends ArrayAdapter {

    // Constantly used variables
    String LOG_TAG = TaskAdapter.class.getSimpleName();
    Utility utility = new Utility();

    // Staple mTasksAdapter variables
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

        // Create a new list item using the data passed into the mTasksAdapter
        Task task = data.get(position);

        // If the row hasn't been used by the mTasksAdapter before
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
            holder.sharer = (TextView)row.findViewById(R.id.task_sharer);

            row.setTag(holder);
        }

        // If the row is simply being recycled
        // Get the tag of the recycled row
        else {
            holder = (ViewHolder)row.getTag();
            if (task.taskIcon.equals("storage") || !task.taskIcon.contains("/art_")) {
                row.findViewById(R.id.task_icon).setVisibility(View.GONE);
                row.findViewById(R.id.task_icon2).setVisibility(View.VISIBLE);
                holder.icon = (ImageView) row.findViewById(R.id.task_icon2);
            }
            else {
                holder.icon = (ImageView) row.findViewById(R.id.task_icon);
                row.findViewById(R.id.task_icon).setVisibility(View.VISIBLE);
                row.findViewById(R.id.task_icon2).setVisibility(View.GONE);
            }
        }

        if (getContext() instanceof ScheduleDetailActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                holder.icon.setTransitionName(null);
        }

        if (context instanceof MainActivity) {
            View v = row.findViewById(R.id.master_layout);
            float duedate = task.taskDueDate;
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
            Calendar c1 = Calendar.getInstance();
            c1.setTimeInMillis(((long) duedate));
            Log.v(LOG_TAG, "C: " + c.get(Calendar.DAY_OF_MONTH) + " C1: " + c1.get(Calendar.DAY_OF_MONTH));
            if (c1.get(Calendar.DAY_OF_MONTH) < c.get(Calendar.DAY_OF_MONTH))
                v.setBackground(context.getDrawable(R.drawable.touch_selector_red));
            else if (c1.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH))
                v.setBackground(context.getDrawable(R.drawable.touch_selector_yellow));
            else v.setBackground(context.getDrawable(R.drawable.touch_selector));
        }

        int textColor = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.KEY_THEME_TITLE_COLOUR), context.getResources().getColor(R.color.gray_900));
        float alpha = 0.7f;

        holder.title.setTextColor(textColor);
        if (holder.sharer != null) {
            holder.sharer.setTextColor(textColor);
            holder.sharer.setAlpha(alpha);
        }

        holder.taskClass.setTextColor(textColor);
        holder.taskType.setTextColor(textColor);

        if (holder.date != null) {
            holder.date.setTextColor(textColor);
            holder.date.setAlpha(alpha);
        }



        Uri uri = Uri.parse(task.taskIcon);
        holder.icon.setImageURI(uri);
        holder.title.setText(task.taskTitle);
        holder.title.setTypeface(Typeface.createFromAsset(context.getAssets(), "roboto_slab_bold.ttf"));

        // If this returns null, list_item_task2 is being used which only has icon and title
        if (holder.taskClass != null) {
            if (!task.taskShared.equals(""))
                holder.sharer.setText(context.getString(R.string.shared_by, task.taskShared));
            else holder.sharer.setText("");
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
        TextView sharer;
    }


}