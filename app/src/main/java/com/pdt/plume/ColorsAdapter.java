package com.pdt.plume;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ColorsAdapter extends BaseAdapter {
    private Context mContext;

    public ColorsAdapter(Context c) {
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
        imageView.setImageDrawable(mThumbIds[position]);
        int color = ((ColorDrawable)mThumbIds[position]).getColor();
        int scale = (int) mContext.getResources().getDisplayMetrics().density;
        Bitmap colorBitmap = Bitmap.createBitmap(40 * scale, 40 * scale, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(colorBitmap);
        canvas.drawColor(color);
        Bitmap bitmap = getRoundedCornerBitmap(colorBitmap,
                (int) mContext.getResources().getDimension(R.dimen.def_icon_size));
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    // references to our images
    private Drawable[] mThumbIds = {
            new ColorDrawable(Color.parseColor("#F44336")),
            new ColorDrawable(Color.parseColor("#E91E63")),
            new ColorDrawable(Color.parseColor("#9C27B0")),
            new ColorDrawable(Color.parseColor("#673AB7")),
            new ColorDrawable(Color.parseColor("#3F51B5")),
            new ColorDrawable(Color.parseColor("#2196F3")),
            new ColorDrawable(Color.parseColor("#03A9F4")),
            new ColorDrawable(Color.parseColor("#00BCD4")),
            new ColorDrawable(Color.parseColor("#009688")),
            new ColorDrawable(Color.parseColor("#4CAF50")),
            new ColorDrawable(Color.parseColor("#8BC34A")),
            new ColorDrawable(Color.parseColor("#C0CA33")),
            new ColorDrawable(Color.parseColor("#FBC02D")),
            new ColorDrawable(Color.parseColor("#FFC107")),
            new ColorDrawable(Color.parseColor("#FF9800")),
            new ColorDrawable(Color.parseColor("#FF5722")),
            new ColorDrawable(Color.parseColor("#795548")),
            new ColorDrawable(Color.parseColor("#9E9E9E")),
            new ColorDrawable(Color.parseColor("#607D8B")),
            new ColorDrawable(Color.parseColor("#212121"))
    };
}
