package com.pdt.plume;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class BuiltInSubjectIconsAdapter extends BaseAdapter {
    private Context mContext;

    public BuiltInSubjectIconsAdapter(Context c) {
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
            R.drawable.art_arts_64dp,
            R.drawable.art_biology_64dp,
            R.drawable.art_business_64dp,
            R.drawable.art_chemistry_64dp,
            R.drawable.art_childdevelopment_64dp,
            R.drawable.art_class_64dp,
            R.drawable.art_computing_64dp,
            R.drawable.art_cooking_64dp,
            R.drawable.art_creativestudies_64dp,
            R.drawable.art_drama_64dp,
            R.drawable.art_engineering_64dp,
            R.drawable.art_english_64dp,
            R.drawable.art_french_64dp,
            R.drawable.art_geography_64dp,
            R.drawable.art_graphics_64dp,
            R.drawable.art_hospitality_64dp,
            R.drawable.art_ict_64dp,
            R.drawable.art_maths_64dp,
            R.drawable.art_media_64dp,
            R.drawable.art_music_64dp,
            R.drawable.art_pe_64dp,
            R.drawable.art_physics_64dp,
            R.drawable.art_psychology_64dp,
            R.drawable.art_re_64dp,
            R.drawable.art_science_64dp,
            R.drawable.art_spanish_64dp,
            R.drawable.art_task_64dp,
            R.drawable.art_woodwork_64dp
    };
}
