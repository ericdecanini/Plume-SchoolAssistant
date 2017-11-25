package com.pdt.plume;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import static com.pdt.plume.R.id.imageView;

public class BuiltInProfileIconsAdapter extends BaseAdapter {

    private Context mContext;
    private int selection;

    public BuiltInProfileIconsAdapter(Context c, int selection) {
        mContext = c;
        this.selection = selection;

    }

    public int getCount() {
        switch (selection) {
            case 0:
                return mThumbIds.length;
            case 1:
                return mThumbIdsHalloween.length;
            default:
                return 0;
        }
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

        switch (selection) {
            case 0:
                imageView.setImageResource(mThumbIds[position]);
                break;
            case 1:
                imageView.setImageResource(mThumbIdsHalloween[position]);
                break;
        }

        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.art_profile_default,
            R.drawable.art_profile_uniform,
            R.drawable.art_profile_uniform_female,
            R.drawable.art_profile_blazer,
            R.drawable.art_profile_blazer_female,
            R.drawable.art_profile_mustache,
            R.drawable.art_profile_pandakun
    };

    private Integer[] mThumbIdsHalloween = {
            R.drawable.art_profile_catgirl,
            R.drawable.art_profile_jason,
            R.drawable.art_profile_morty,
            R.drawable.art_profile_pennywise,
            R.drawable.art_profile_pumpkin,
            R.drawable.art_profile_skull,
            R.drawable.art_profile_vampire,
            R.drawable.art_profile_witch,
            R.drawable.art_profile_zombie
    };

}
