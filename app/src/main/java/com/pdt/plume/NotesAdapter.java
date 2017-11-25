package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class NotesAdapter extends ArrayAdapter {

    Context context;
    ArrayList<String> data;
    private int layoutResourceId;

    public NotesAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<String> data) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
        this.layoutResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        String text = data.get(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.textView = ((TextView) row.findViewById(android.R.id.text1));
            row.setTag(holder);
        } else holder = ((ViewHolder) row.getTag());

        int textColor = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.KEY_THEME_TEXT_COLOUR),
                        getContext().getResources().getColor(R.color.gray_900));
        if (holder.textView != null) {
            holder.textView.setTextColor(textColor);
            holder.textView.setText(text);
        }

        return row;
    }

    static class ViewHolder {
        TextView textView;
    }

}
