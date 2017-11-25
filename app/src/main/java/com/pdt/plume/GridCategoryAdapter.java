package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GridCategoryAdapter extends ArrayAdapter<GridCategory> {

    Context c;
    int resource;
    ArrayList<GridCategory> objects;

    public GridCategoryAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<GridCategory> objects) {
        super(context, resource, objects);
        this.c = context;
        this.resource = resource;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        GridCategory item = objects.get(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity)c).getLayoutInflater();
            row = inflater.inflate(resource, parent, false);

            holder = new ViewHolder();
            holder.category = (TextView) row.findViewById(R.id.category);
            holder.gridview = (ExpandableHeightGridView) row.findViewById(R.id.gridView);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        holder.category.setText(item.getCategory());
        holder.gridview.setAdapter(item.getAdapter());
        holder.gridview.setOnItemClickListener(item.getListener());

        holder.gridview.setExpanded(true);
        holder.gridview.setNumColumns(4);
        holder.gridview.setPadding(0, 16, 0, 16);
        holder.gridview.setGravity(Gravity.CENTER);

        // Apply the theme variables
        int primaryColour = PreferenceManager.getDefaultSharedPreferences(c)
                .getInt(c.getString(R.string.KEY_THEME_PRIMARY_COLOR), c.getResources().getColor(R.color.colorPrimary));
        holder.category.setTextColor(primaryColour);

        return row;
    }

    static class ViewHolder {
        TextView category;
        ExpandableHeightGridView gridview;
    }

}
