package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;



public class MismatchListAdapter extends ArrayAdapter {

    String LOG_TAG = MismatchListAdapter.class.getSimpleName();

    Context context;
    int layoutResourceId;
    ArrayList<Bundle> data = null;

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
        final DbHelper dbHelper = new DbHelper(context);

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
            ArrayList<Schedule> classesList = dbHelper.getAllClassesArray(context);
            for (int i = 0; i < classesList.size(); i++) {
                popupMenu.getMenu().add(classesList.get(i).scheduleLesson);
            }
            popupMenu.getMenu().add(context.getString(R.string.add_new_class));

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Bundle bundle = data.get(position);
                    if (menuItem.getTitle().equals(context.getString(R.string.add_new_class))) {
                        // Get the data of the class and add it as a new class
                        dbHelper.insertSchedule(
                                bundle.getString("peers"),
                                bundle.getString("title"),
                                bundle.getString("teacher"),
                                bundle.getString("room"),
                                bundle.getString("occurrence"),
                                bundle.getInt("timein"),
                                bundle.getInt("timeout"),
                                bundle.getInt("timeinalt"),
                                bundle.getInt("timeoutalt"),
                                bundle.getString("periods"),
                                bundle.getString("icon")
                        );
                        Toast.makeText(context, bundle.getString("title") + " " + context.getString(R.string.new_schedule_toast_class_inserted), Toast.LENGTH_SHORT).show();
                        holder.dropdownText.setText(bundle.getString("title"));
                        holder.dropdownText.setTag(bundle.getString("title"));
                    } else {
                        holder.dropdownText.setText(menuItem.getTitle());
                        holder.dropdownText.setTag(menuItem.getTitle());
                        Cursor cursor = dbHelper.getScheduleDataByTitle(menuItem.getTitle().toString());
                        cursor.moveToFirst();
                        String icon = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
                        holder.dropdown.setTag(icon);
                    }
                    return true;
                }
            });

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
