package com.pdt.plume;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class ImageAdapter extends ArrayAdapter {

    Context context;
    ArrayList<Uri> data;
    int layoutResourceId;

    public ImageAdapter(Context context, int layoutResourceId, ArrayList<Uri> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        Uri uri = data.get(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.photo = (ImageView) row.findViewById(R.id.photo);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        holder.photo.setImageURI(uri);
        return row;
    }

    static class ViewHolder {
        ImageView photo;
    }

}
