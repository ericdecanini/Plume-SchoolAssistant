package com.pdt.plume;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class OccurrenceTimePeriodAdapter extends ArrayAdapter {

    Context context;
    int layoutResourceId;
    ArrayList<OccurrenceTimePeriod> objects = null;

    public OccurrenceTimePeriodAdapter(Context context, int resource, ArrayList<OccurrenceTimePeriod> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutResourceId = resource;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.period = (TextView) row.findViewById(R.id.list_item_time_period_textview);
            holder.sun = (TextView) row.findViewById(R.id.list_item_time_period_sun);
            holder.mon = (TextView) row.findViewById(R.id.list_item_time_period_mon);
            holder.tue = (TextView) row.findViewById(R.id.list_item_time_period_tue);
            holder.wed = (TextView) row.findViewById(R.id.list_item_time_period_wed);
            holder.thu = (TextView) row.findViewById(R.id.list_item_time_period_thu);
            holder.fri = (TextView) row.findViewById(R.id.list_item_time_period_fri);
            holder.sat = (TextView) row.findViewById(R.id.list_item_time_period_sat);

            holder.period_alt = (TextView) row.findViewById(R.id.list_item_time_period_textview_alt);
            holder.sun_alt = (TextView) row.findViewById(R.id.list_item_time_period_sun_alt);
            holder.mon_alt = (TextView) row.findViewById(R.id.list_item_time_period_mon_alt);
            holder.tue_alt = (TextView) row.findViewById(R.id.list_item_time_period_tue_alt);
            holder.wed_alt = (TextView) row.findViewById(R.id.list_item_time_period_wed_alt);
            holder.thu_alt = (TextView) row.findViewById(R.id.list_item_time_period_thu_alt);
            holder.fri_alt = (TextView) row.findViewById(R.id.list_item_time_period_fri_alt);
            holder.sat_alt = (TextView) row.findViewById(R.id.list_item_time_period_sat_alt);

            row.setTag(holder);
        }
        else holder = (ViewHolder) row.getTag();

        OccurrenceTimePeriod occurrenceTimePeriod = objects.get(position);
        holder.period.setText(occurrenceTimePeriod.time_period);

        if (occurrenceTimePeriod.basis.equals("0") || occurrenceTimePeriod.basis.equals("1")) {
            //Set the colours of the days layout
            if (occurrenceTimePeriod.sunday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.sun.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.sun.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

            if (occurrenceTimePeriod.monday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.mon.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.mon.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

            if (occurrenceTimePeriod.tuesday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.tue.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.tue.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

            if (occurrenceTimePeriod.wednesday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.wed.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.wed.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

            if (occurrenceTimePeriod.thursday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.thu.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.thu.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

            if (occurrenceTimePeriod.friday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.fri.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.fri.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

            if (occurrenceTimePeriod.saturday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.sat.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.sat.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

            //Hide the alt layout if the weekType is 0, set the colours of the alternate days layout if it is 1
            if (occurrenceTimePeriod.weekType.equals("0"))
                row.findViewById(R.id.list_item_time_period_alt_layout).setVisibility(View.GONE);
            else {
                holder.period_alt.setText(occurrenceTimePeriod.time_period_alt);

                if (occurrenceTimePeriod.sunday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.sun_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.sun_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

                if (occurrenceTimePeriod.monday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.mon_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.mon_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

                if (occurrenceTimePeriod.tuesday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.tue_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.tue_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

                if (occurrenceTimePeriod.wednesday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.wed_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.wed_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

                if (occurrenceTimePeriod.thursday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.thu_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.thu_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

                if (occurrenceTimePeriod.friday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.fri_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.fri_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));

                if (occurrenceTimePeriod.saturday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.sat_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.sat_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
            }
        } else { // If the basis is 2
            ((TextView) row.findViewById(R.id.list_item_time_period_week)).setText(context.getString(R.string.class_time_block_day_a));
            ((TextView) row.findViewById(R.id.list_item_time_period_week_alt)).setText(context.getString(R.string.class_time_block_day_b));
            row.findViewById(R.id.list_item_time_period_days).setVisibility(View.GONE);
            row.findViewById(R.id.list_item_time_period_days_alt).setVisibility(View.GONE);
        }



        return row;
    }

    static class ViewHolder
    {
        TextView period;
        TextView sun;
        TextView mon;
        TextView tue;
        TextView wed;
        TextView thu;
        TextView fri;
        TextView sat;

        TextView period_alt;
        TextView sun_alt;
        TextView mon_alt;
        TextView tue_alt;
        TextView wed_alt;
        TextView thu_alt;
        TextView fri_alt;
        TextView sat_alt;
    }
}
