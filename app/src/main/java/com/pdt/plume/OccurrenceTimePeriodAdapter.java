package com.pdt.plume;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class OccurrenceTimePeriodAdapter extends ArrayAdapter {
    String LOG_TAG = OccurrenceTimePeriodAdapter.class.getSimpleName();

    // Staple mScheduleAdapter variables
    Context context;
    int layoutResourceId;
    ArrayList<OccurrenceTimePeriod> objects = null;

    // Default mScheduleAdapter constructor
    public OccurrenceTimePeriodAdapter(Context context, int resource, ArrayList<OccurrenceTimePeriod> occurrences) {
        super(context, resource, occurrences);
        this.context = context;
        this.layoutResourceId = resource;
        this.objects = occurrences;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Initialise variables for the Row and View Holder
        View row = convertView;
        ViewHolder holder;

        // If the row hasn't been used by the mScheduleAdapter before
        // create a new row
        if (row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            // Get references to the View Holder's views
            // by searching the row for the UI element
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

        // If the row is simply being recycled
        // Get the tag of the recycled row
        else holder = (ViewHolder) row.getTag();

        // Create a new list item using the data passed into the mScheduleAdapter
        OccurrenceTimePeriod occurrenceTimePeriod = objects.get(position);

        // Set the text of the main header
        holder.period.setText(occurrenceTimePeriod.time_period);

        // If the week type is the same each week, remove the week one label
        if (occurrenceTimePeriod.weekType.equals("0"))
            ((TextView)row.findViewById(R.id.list_item_time_period_week)).setText(context.getString(R.string.week_every));
        else ((TextView)row.findViewById(R.id.list_item_time_period_week)).setText(context.getString(R.string.week_one));

        // If the item's basis is Time or Period based, highlight the background colour
        // Of the day buttons corresponding to the activated day binary code
        if (occurrenceTimePeriod.basis.equals("0") || occurrenceTimePeriod.basis.equals("1")) {
            if (occurrenceTimePeriod.sunday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.sun.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.sun.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.sun.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
            } else holder.sun.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));

            if (occurrenceTimePeriod.monday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.mon.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.mon.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mon.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
            } else holder.mon.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));


            if (occurrenceTimePeriod.tuesday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.tue.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.tue.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.tue.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
            } else holder.tue.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));


            if (occurrenceTimePeriod.wednesday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.wed.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.wed.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.wed.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
            } else holder.wed.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));


            if (occurrenceTimePeriod.thursday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.thu.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.thu.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.thu.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
            } else holder.thu.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));


            if (occurrenceTimePeriod.friday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.fri.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.fri.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.fri.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
            } else holder.fri.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));


            if (occurrenceTimePeriod.saturday.equals("1"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.sat.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                } else holder.sat.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.sat.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
            } else holder.sat.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));

            // Hide the alt layout if the weekType is 0, set the colours of the alternate days layout if it is 1
            if (occurrenceTimePeriod.weekType.equals("0")) {
                row.findViewById(R.id.list_item_time_period_alt_layout).setVisibility(View.GONE);
            }
            else {
                row.findViewById(R.id.list_item_time_period_alt_layout).setVisibility(View.VISIBLE);
                holder.period_alt.setText(occurrenceTimePeriod.time_period_alt);

                // Set the background colour of the day buttons
                if (occurrenceTimePeriod.sunday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.sun_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.sun_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.sun_alt.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
                } else holder.sun_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));

                if (occurrenceTimePeriod.monday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.mon_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.mon_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.mon_alt.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
                } else holder.mon_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));

                if (occurrenceTimePeriod.tuesday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.tue_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.tue_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.tue_alt.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
                } else holder.tue_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));

                if (occurrenceTimePeriod.wednesday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.wed_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.wed_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.wed_alt.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
                } else holder.wed_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));

                if (occurrenceTimePeriod.thursday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.thu_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.thu_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.thu_alt.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
                } else holder.thu_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));

                if (occurrenceTimePeriod.friday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.fri_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.fri_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.fri_alt.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
                } else holder.fri_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));

                if (occurrenceTimePeriod.saturday_alt.equals("1"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.sat_alt.setBackgroundColor(((Activity) context).getColor(R.color.colorAccent));
                    } else holder.sat_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.colorAccent));
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.sat_alt.setBackgroundColor(((Activity) context).getColor(R.color.gray_500));
                } else holder.sat_alt.setBackgroundColor(((Activity) context).getResources().getColor(R.color.gray_500));
            }
        }

        // If the item's basis is Block based, change the week text to Day A and Day B
        // and hide the row of day buttons
        else {
            holder.period_alt.setText(occurrenceTimePeriod.time_period_alt);
            ((TextView) row.findViewById(R.id.list_item_time_period_week)).setText(context.getString(R.string.a_day));
            ((TextView) row.findViewById(R.id.list_item_time_period_week_alt)).setText(context.getString(R.string.b_day));
            row.findViewById(R.id.list_item_time_period_days).setVisibility(View.GONE);
            row.findViewById(R.id.list_item_time_period_days_alt).setVisibility(View.GONE);
        }

        return row;
    }

    // A View Holder containing variables that reference to
    // the UI elements of the given row
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
