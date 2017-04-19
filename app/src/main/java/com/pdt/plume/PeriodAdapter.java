package com.pdt.plume;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

import static com.pdt.plume.R.id.periods;
import static com.pdt.plume.R.id.view;
import static com.pdt.plume.R.id.zxing_viewfinder_view;
import static com.pdt.plume.R.string.re;


public class PeriodAdapter extends ArrayAdapter {

    String LOG_TAG = PeriodAdapter.class.getSimpleName();
    Utility utility = new Utility();

    int mPrimaryColor;

    Context context;
    int layoutResourceId;
    ArrayList<PeriodItem> objects = null;
    PeriodItem item;

    public PeriodAdapter(Context context, int resource, ArrayList<PeriodItem> occurrences) {
        super(context, resource, occurrences);
        this.context = context;
        this.layoutResourceId = resource;
        this.objects = occurrences;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;
        item = objects.get(position);
        String weekType = item.weekType;
        String basis = item.occurrence.split(":")[0];

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPrimaryColor = preferences.getInt(context.getString(R.string.KEY_THEME_PRIMARY_COLOR),
                context.getResources().getColor(R.color.colorPrimary));

        if (row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            if (basis.equals("0")) {
                row.findViewById(R.id.time_layout).setVisibility(View.VISIBLE);
                holder.weekOne = (TextView) row.findViewById(R.id.week_one_time);
                holder.weekTwo = (TextView) row.findViewById(R.id.week_two_time);
                holder.weekTwoLayout = row.findViewById(R.id.week_two_layout_time);

                holder.days = (TextView) row.findViewById(R.id.days_time);
                holder.daysAlt = (TextView) row.findViewById(R.id.days_alt_time);

                holder.timein = (TextView) row.findViewById(R.id.timein);
                holder.timeout = (TextView) row.findViewById(R.id.timeout);
                holder.timeinAlt = (TextView) row.findViewById(R.id.timein_alt);
                holder.timeoutAlt = (TextView) row.findViewById(R.id.timeout_alt);

                if (weekType.equals("0")) {
                    holder.weekOne.setVisibility(View.GONE);
                    holder.weekTwo.setVisibility(View.GONE);
                    holder.weekTwoLayout.setVisibility(View.GONE);
                }

                if (getContext() instanceof NewScheduleActivity) {

                    holder.days.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogFragment fragment = DaysDialog.newInstance();
                            Bundle args = new Bundle();
                            args.putString(context.getString(R.string.ARGUMENT_DAYS), item.days);
                            fragment.setArguments(args);
                            fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        }
                    });

                    holder.daysAlt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogFragment fragment = DaysDialog.newInstance();
                            Bundle args = new Bundle();
                            args.putString(context.getString(R.string.ARGUMENT_DAYS), item.days);
                            args.putBoolean(context.getString(R.string.ARGUMENT_ALTERNATE), true);
                            fragment.setArguments(args);
                            fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        }
                    });

                    holder.timein.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar c = Calendar.getInstance();
                            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                    item.timeinValue = utility.timeToMillis(i, i1);
                                    item.timein = utility.millisToHourTime(item.timeinValue);
                                }
                            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                            timePickerDialog.show();
                        }
                    });

                    holder.timeout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                    item.timeoutValue = utility.timeToMillis(i, i1);
                                    item.timeout = utility.millisToHourTime(item.timeoutValue);
                                }
                            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                            timePickerDialog.show();
                        }
                    });

                    holder.timeinAlt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar c = Calendar.getInstance();
                            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                    item.timeinaltValue = utility.timeToMillis(i, i1);
                                    item.timeinalt = utility.millisToHourTime(item.timeinaltValue);
                                }
                            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                            timePickerDialog.show();
                        }
                    });

                    holder.timeoutAlt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                    item.timeoutaltValue = utility.timeToMillis(i, i1);
                                    item.timeoutalt = utility.millisToHourTime(item.timeoutaltValue);
                                }
                            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                            timePickerDialog.show();
                        }
                    });
                }

            } else if (basis.equals("1")) {
                row.findViewById(R.id.period_layout).setVisibility(View.VISIBLE);
                holder.weekOne = (TextView) row.findViewById(R.id.week_one_period);
                holder.weekTwo = (TextView) row.findViewById(R.id.week_two_period);
                holder.weekTwoLayout = row.findViewById(R.id.week_two_layout_period);

                holder.days = (TextView) row.findViewById(R.id.days_period);
                holder.daysAlt = (TextView) row.findViewById(R.id.days_alt_period);

                holder.period[0] = (TextView) row.findViewById(R.id.one_period);
                holder.period[1] = (TextView) row.findViewById(R.id.two_period);
                holder.period[2] = (TextView) row.findViewById(R.id.three_period);
                holder.period[3] = (TextView) row.findViewById(R.id.four_period);
                holder.period[4] = (TextView) row.findViewById(R.id.five_period);
                holder.period[5] = (TextView) row.findViewById(R.id.six_period);
                holder.period[6] = (TextView) row.findViewById(R.id.seven_period);
                holder.period[7] = (TextView) row.findViewById(R.id.eight_period);

                holder.periodAlt[0] = (TextView) row.findViewById(R.id.one_alt_period);
                holder.periodAlt[1] = (TextView) row.findViewById(R.id.two_alt_period);
                holder.periodAlt[2] = (TextView) row.findViewById(R.id.three_alt_period);
                holder.periodAlt[3] = (TextView) row.findViewById(R.id.four_alt_period);
                holder.periodAlt[4] = (TextView) row.findViewById(R.id.five_alt_period);
                holder.periodAlt[5] = (TextView) row.findViewById(R.id.six_alt_period);
                holder.periodAlt[6] = (TextView) row.findViewById(R.id.seven_alt_period);
                holder.periodAlt[7] = (TextView) row.findViewById(R.id.eight_alt_period);

                if (weekType.equals("0")) {
                    holder.weekOne.setVisibility(View.GONE);
                    holder.weekTwo.setVisibility(View.GONE);
                    holder.weekTwoLayout.setVisibility(View.GONE);
                }

                if (context instanceof NewScheduleActivity) {

                    holder.days.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogFragment fragment = DaysDialog.newInstance();
                            Bundle args = new Bundle();
                            args.putString(context.getString(R.string.ARGUMENT_DAYS), item.days);
                            args.putInt(context.getString(R.string.ARGUMENT_POSITION), position);
                            fragment.setArguments(args);
                            fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        }
                    });

                    holder.daysAlt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogFragment fragment = DaysDialog.newInstance();
                            Bundle args = new Bundle();
                            args.putString(context.getString(R.string.ARGUMENT_DAYS), item.days);
                            args.putBoolean(context.getString(R.string.ARGUMENT_ALTERNATE), true);
                            args.putInt(context.getString(R.string.ARGUMENT_POSITION), position);
                            fragment.setArguments(args);
                            fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        }
                    });

                    for (int i = 0; i < 8; i++) {
                        final int finalI = i;
                        holder.period[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.v(LOG_TAG, "Final I = " + finalI + ", boolean = " + item.period[finalI]);
                                if (!item.period[finalI]) {
                                    item.period[finalI] = true;
                                    view.setBackground(context.getDrawable(R.drawable.bg_period_button));
                                    view.setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                    ((TextView) view).setTextColor(context.getResources().getColor(R.color.white));
                                } else {
                                    item.period[finalI] = false;
                                    view.setBackground(null);
                                    ((TextView) view).setTextColor(context.getResources().getColor(R.color.gray_900));
                                }
                            }
                        });
                        holder.periodAlt[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!item.periodAlt[finalI]) {
                                    item.periodAlt[finalI] = true;
                                    holder.periodAlt[finalI].setBackground(context.getDrawable(R.drawable.bg_period_button));
                                    holder.periodAlt[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                    ((TextView) view).setTextColor(context.getResources().getColor(R.color.white));
                                } else {
                                    item.periodAlt[finalI] = false;
                                    holder.periodAlt[finalI].setBackground(null);
                                    ((TextView) view).setTextColor(context.getResources().getColor(R.color.gray_900));
                                }
                            }
                        });
                    }
                }


            } else if (basis.equals("2")) {
                row.findViewById(R.id.block_layout).setVisibility(View.VISIBLE);
                holder.period[0] = (TextView) row.findViewById(R.id.one_block);
                holder.period[1] = (TextView) row.findViewById(R.id.two_block);
                holder.period[2] = (TextView) row.findViewById(R.id.three_block);
                holder.period[3] = (TextView) row.findViewById(R.id.four_block);

                holder.periodAlt[0] = (TextView) row.findViewById(R.id.one_alt_block);
                holder.periodAlt[1] = (TextView) row.findViewById(R.id.two_alt_block);
                holder.periodAlt[2] = (TextView) row.findViewById(R.id.three_alt_block);
                holder.periodAlt[3] = (TextView) row.findViewById(R.id.four_alt_block);

                for (int i = 0; i < 3; i++) {
                    final int finalI = i;
                    holder.period[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!item.period[finalI]) {
                                item.period[finalI] = true;
                                holder.period[finalI].setBackground(context.getDrawable(R.drawable.bg_period_button));
                                holder.period[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                ((TextView) view).setTextColor(context.getResources().getColor(R.color.white));
                            } else {
                                item.period[finalI] = false;
                                holder.period[finalI].setBackground(null);
                                ((TextView) view).setTextColor(context.getResources().getColor(R.color.gray_900));
                            }
                        }
                    });
                    holder.periodAlt[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!item.periodAlt[finalI]) {
                                item.periodAlt[finalI] = true;
                                holder.periodAlt[finalI].setBackground(context.getDrawable(R.drawable.bg_period_button));
                                holder.periodAlt[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                ((TextView) view).setTextColor(context.getResources().getColor(R.color.white));
                            } else {
                                item.periodAlt[finalI] = false;
                                holder.periodAlt[finalI].setBackground(null);
                                ((TextView) view).setTextColor(context.getResources().getColor(R.color.gray_900));
                            }
                        }
                    });
                }
            }

            row.setTag(holder);
        }

        else holder = (ViewHolder) row.getTag();

        // Here set apply the data to the UI as if it were edited

        // The days
        if (item.days != null) {
            if (item.days.equals("0:0:0:0:0:0:0")) {
                Calendar c = Calendar.getInstance();
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                String[] daysArray = item.days.split(":");
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < daysArray.length; i++) {
                    if (i != 0) builder.append(":");
                    if (i == dayOfWeek - 1) builder.append("1");
                    else builder.append("0");
                }
                item.days = builder.toString();
                updateItemOccurrence();
            }
            holder.days.setText(getDayString(item.days));
            holder.daysAlt.setText(getDayString(item.days_alt));
        }

        // The Periods
        String[] periodsArray = item.periods.split(":");
        for (int i = 0; i < periodsArray.length; i++) {
            if (item.period[i]) {
                item.period[i] = true;
                holder.period[i].setBackground(context.getDrawable(R.drawable.bg_period_button));
                holder.period[i].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                holder.period[i].setTextColor(context.getResources().getColor(R.color.white));
            } else {
                item.periodAlt[i] = false;
                holder.periodAlt[i].setBackground(null);
                holder.period[i].setTextColor(context.getResources().getColor(R.color.gray_900));
            }
        }

        return row;
    }


    static class ViewHolder
    {
        TextView weekOne;
        TextView weekTwo;
        View weekTwoLayout;

        TextView days;
        TextView daysAlt;

        TextView timein;
        TextView timeout;
        TextView timeinAlt;
        TextView timeoutAlt;

        TextView[] period = {null, null, null, null, null, null, null, null};
        TextView[] periodAlt = {null, null, null, null, null, null, null, null};
    }

    private String getDayString(String days) {
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] dayAbbr = {"S", "M", "T", "W", "T", "F", "S"};
        ArrayList<Integer> weekdayList = new ArrayList<>();
        for (int i = 1; i < 5; i++)
            weekdayList.add(i);
        ArrayList<Integer> selectedDays = new ArrayList<>();
        String[] daysArray = days.split(":");
        for (int i = 0; i < daysArray.length; i++) {
            if (daysArray[i].equals("1")) selectedDays.add(i);
        }

        if (selectedDays.size() == 0)
            return context.getString(R.string.days);
        else if (selectedDays.size() == 1) {
            int day = selectedDays.get(0);
            return dayNames[day];
        } if (selectedDays.size() == 7)
            return context.getString(R.string.everyday);
        else if (selectedDays.containsAll(weekdayList) && selectedDays.size() == 5)
            return context.getString(R.string.weekdays);
        else if (selectedDays.contains(0) && selectedDays.contains(6) && selectedDays.size() == 2)
            return context.getString(R.string.weekends);
        else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < selectedDays.size(); i++) {
                if (i != 0)
                    builder.append(", ");
                builder.append(dayAbbr[selectedDays.get(i)]);
            }
            return builder.toString();
        }
    }

    private void updateItemOccurrence() {
        String[] daysArray = item.days.split(":");
        String[] daysAltArray = item.days_alt.split(":");
        String[] daysCombinedArray = {"0", "0", "0", "0", "0", "0", "0"};
        for (int i = 0; i < daysCombinedArray.length; i++) {
            if (daysArray[i].equals("0") && daysAltArray[i].equals("0"))
                daysCombinedArray[i] = "0";
            else if (daysArray[i].equals("1") && daysAltArray[i].equals("0"))
                daysCombinedArray[i] = "1";
            else if (daysArray[i].equals("0") && daysAltArray[i].equals("1"))
                daysCombinedArray[i] = "2";
            else daysCombinedArray[i] = "3";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(item.basis);
        builder.append(":");
        builder.append(item.weekType);
        builder.append(":");
        for (int i = 0; i < daysCombinedArray.length; i++) {
            builder.append(daysCombinedArray[i]);
            if (i != daysCombinedArray.length - 1)
                builder.append(":");
        }

        item.occurrence = builder.toString();
    }

}
