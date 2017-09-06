package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class CheckScheduleAdapter extends ArrayAdapter {

    String LOG_TAG = CheckScheduleAdapter.class.getSimpleName();

    Context context;
    int layoutResourceId;
    ArrayList<MatchingClass> data = null;

    public CheckScheduleAdapter(Context context, int layoutResourceId, ArrayList<MatchingClass> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.icon = (ImageView) row.findViewById(R.id.icon);
            holder.title = (TextView) row.findViewById(R.id.title);
            holder.check = (CheckBox) row.findViewById(R.id.check);

            row.setTag(holder);
        }

        else {
            holder = (ViewHolder) row.getTag();
        }

        int textColor = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.KEY_THEME_TITLE_COLOUR), context.getResources().getColor(R.color.gray_900));
        float[] hsv = new float[3];
        Color.colorToHSV(textColor, hsv);
        hsv[1] *= 0.8;
        int saturatedTextColor = Color.HSVToColor(hsv);

        MatchingClass classItem = data.get(position);
        holder.title.setText(classItem.title);
        holder.title.setTextColor(textColor);
        holder.icon.setImageURI(Uri.parse(classItem.icon));

        holder.check.setButtonTintList(ColorStateList.valueOf(textColor));

        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        CheckBox check;
    }

}
