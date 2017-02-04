package com.pdt.plume;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class BuiltInProfileIconsAdapter extends BaseAdapter {
    private Context mContext;

    public BuiltInProfileIconsAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            int wh = (int) mContext.getResources().getDimension(R.dimen.def_icon_size);
            imageView.setLayoutParams(new GridView.LayoutParams(wh, wh));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.art_profile_default,
            R.drawable.art_profile_uniform,
            R.drawable.art_profile_blazer,
            R.drawable.art_profile_mustache,
            R.drawable.art_profile_blazerpanda,
            R.drawable.art_profile_alien
    };
}
