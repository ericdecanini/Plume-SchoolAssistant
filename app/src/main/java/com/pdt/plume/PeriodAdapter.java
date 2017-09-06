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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.fragment;
import static com.pdt.plume.R.id.timein;
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

    public PeriodAdapter(Context context, int resource, ArrayList<PeriodItem> occurrences) {
        super(context, resource, occurrences);
        this.context = context;
        this.layoutResourceId = resource;
        this.objects = occurrences;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;
        final PeriodItem item = objects.get(position);
        item.position = position;
        String weekType = item.weekType;
        String basis = item.occurrence.split(":")[0];


        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPrimaryColor = preferences.getInt(context.getString(R.string.KEY_THEME_PRIMARY_COLOR),
                context.getResources().getColor(R.color.colorPrimary));
        final int backgroundColor = preferences.getInt(context.getString(R.string.KEY_THEME_BACKGROUND_COLOUR),
                context.getResources().getColor(R.color.backgroundColor));
        final int textColor = preferences.getInt(context.getString(R.string.KEY_THEME_TITLE_COLOUR),
                context.getResources().getColor(R.color.gray_900));


        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            row.setTag(holder);
        } else holder = (ViewHolder) row.getTag();


        if (basis.equals("0")) {
            row.findViewById(R.id.time_layout).setVisibility(View.VISIBLE);
            row.findViewById(R.id.period_layout).setVisibility(View.GONE);
            row.findViewById(R.id.block_layout).setVisibility(View.GONE);
            holder.weekOne = (TextView) row.findViewById(R.id.week_one_time);
            holder.weekTwo = (TextView) row.findViewById(R.id.week_two_time);
            holder.weekTwoLayout = row.findViewById(R.id.week_two_layout_time);

            holder.days = (TextView) row.findViewById(R.id.days_time);
            holder.daysAlt = (TextView) row.findViewById(R.id.days_alt_time);

            holder.days.setTextColor(textColor);
            holder.daysAlt.setTextColor(textColor);

            holder.timein = (TextView) row.findViewById(R.id.timein);
            holder.timeout = (TextView) row.findViewById(R.id.timeout);
            holder.timeinAlt = (TextView) row.findViewById(R.id.timein_alt);
            holder.timeoutAlt = (TextView) row.findViewById(R.id.timeout_alt);

            holder.timein.setText(item.timein);
            holder.timeout.setText(item.timeout);
            holder.timeinAlt.setText(item.timeinalt);
            holder.timeoutAlt.setText(item.timeoutalt);

            if (weekType.equals("0")) {
                holder.weekOne.setVisibility(View.GONE);
                holder.weekTwo.setVisibility(View.GONE);
                holder.weekTwoLayout.setVisibility(View.GONE);
            }

            if (getContext() instanceof NewScheduleActivity) {
                holder.days.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View parentRow = (View) view.getParent().getParent().getParent();
                        ListView listView = (ListView) parentRow.getParent();
                        int position = listView.getPositionForView(parentRow);
                        PeriodItem item = objects.get(position);
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
                        View parentRow = (View) view.getParent().getParent().getParent();
                        ListView listView = (ListView) parentRow.getParent();
                        int position = listView.getPositionForView(parentRow);
                        PeriodItem item = objects.get(position);
                        DialogFragment fragment = DaysDialog.newInstance();
                        Bundle args = new Bundle();
                        args.putString(context.getString(R.string.ARGUMENT_DAYS), item.days_alt);
                        args.putBoolean(context.getString(R.string.ARGUMENT_ALTERNATE), true);
                        args.putInt(context.getString(R.string.ARGUMENT_POSITION), position);
                        fragment.setArguments(args);
                        fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                    }
                });

                holder.timein.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View parentRow = (View) view.getParent().getParent().getParent();
                        ListView listView = (ListView) parentRow.getParent();
                        int position = listView.getPositionForView(parentRow);
                        final PeriodItem item = objects.get(position);
                        Calendar c = Calendar.getInstance();

                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);
                        if (minute >= 15 && minute < 45)
                            minute = 30;
                        if (minute >= 45) {
                            minute = 0;
                            hour++;
                        }

                        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                item.timeinValue = utility.timeToMillis(i, i1);
                                item.timein = utility.millisToHourTime(item.timeinValue);
                                holder.timein.setText(item.timein);
                            }
                        }, hour, minute, false);
                        timePickerDialog.show();
                    }
                });

                holder.timeout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View parentRow = (View) view.getParent().getParent().getParent();
                        ListView listView = (ListView) parentRow.getParent();
                        int position = listView.getPositionForView(parentRow);
                        final PeriodItem item = objects.get(position);
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);

                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);
                        if (minute >= 15 && minute < 45)
                            minute = 30;
                        if (minute >= 45) {
                            minute = 0;
                            hour++;
                        }

                        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                item.timeoutValue = utility.timeToMillis(i, i1);
                                item.timeout = utility.millisToHourTime(item.timeoutValue);
                                holder.timeout.setText(item.timeout);
                            }
                        }, hour, minute, false);
                        timePickerDialog.show();
                    }
                });

                holder.timeinAlt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View parentRow = (View) view.getParent().getParent().getParent();
                        ListView listView = (ListView) parentRow.getParent();
                        int position = listView.getPositionForView(parentRow);
                        final PeriodItem item = objects.get(position);
                        Calendar c = Calendar.getInstance();

                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);
                        if (minute >= 15 && minute < 45)
                            minute = 30;
                        if (minute >= 45) {
                            minute = 0;
                            hour++;
                        }

                        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                item.timeinaltValue = utility.timeToMillis(i, i1);
                                item.timeinalt = utility.millisToHourTime(item.timeinaltValue);
                                holder.timeinAlt.setText(item.timeinalt);
                            }
                        }, hour, minute, false);
                        timePickerDialog.show();
                    }
                });

                holder.timeoutAlt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View parentRow = (View) view.getParent().getParent().getParent();
                        ListView listView = (ListView) parentRow.getParent();
                        int position = listView.getPositionForView(parentRow);
                        final PeriodItem item = objects.get(position);
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);

                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);
                        if (minute >= 15 && minute < 45)
                            minute = 30;
                        if (minute >= 45) {
                            minute = 0;
                            hour++;
                        }

                        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                item.timeoutaltValue = utility.timeToMillis(i, i1);
                                item.timeoutalt = utility.millisToHourTime(item.timeoutaltValue);
                                holder.timeoutAlt.setText(item.timeoutalt);
                            }
                        }, hour, minute, false);
                        timePickerDialog.show();
                    }
                });

                row.findViewById(R.id.delete_time).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        remove(item);
                        notifyDataSetChanged();
                    }
                });
                row.findViewById(R.id.delete_period).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        remove(item);
                        notifyDataSetChanged();
                    }
                });
            } else {
                row.findViewById(R.id.delete_time).setVisibility(View.GONE);
                row.findViewById(R.id.delete_period).setVisibility(View.GONE);
            }

        } else if (basis.equals("1")) {
            row.findViewById(R.id.period_layout).setVisibility(View.VISIBLE);
            row.findViewById(R.id.time_layout).setVisibility(View.GONE);
            row.findViewById(R.id.block_layout).setVisibility(View.GONE);
            holder.weekOne = (TextView) row.findViewById(R.id.week_one_period);
            holder.weekTwo = (TextView) row.findViewById(R.id.week_two_period);
            holder.weekTwoLayout = row.findViewById(R.id.week_two_layout_period);

            holder.days = (TextView) row.findViewById(R.id.days_period);
            holder.daysAlt = (TextView) row.findViewById(R.id.days_alt_period);

            holder.days.setTextColor(textColor);
            holder.daysAlt.setTextColor(textColor);

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
                        View parentRow = (View) view.getParent().getParent().getParent();
                        ListView listView = (ListView) parentRow.getParent();
                        int position = listView.getPositionForView(parentRow);
                        PeriodItem item = objects.get(position);
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
                        View parentRow = (View) view.getParent().getParent().getParent();
                        ListView listView = (ListView) parentRow.getParent();
                        int position = listView.getPositionForView(parentRow);
                        PeriodItem item = objects.get(position);
                        DialogFragment fragment = DaysDialog.newInstance();
                        Bundle args = new Bundle();
                        args.putString(context.getString(R.string.ARGUMENT_DAYS), item.days_alt);
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
                            View parentRow = (View) view.getParent().getParent().getParent();
                            ListView listView = (ListView) parentRow.getParent();
                            int position = listView.getPositionForView(parentRow);
                            PeriodItem item = objects.get(position);
                            if (item.days.equals("0:0:0:0:0:0:0")) {
                                return;
                            }
                            if (!item.period[finalI]) {
                                item.period[finalI] = true;
                                view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                    view.setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                ((TextView) view).setTextColor(backgroundColor);
                            } else {
                                item.period[finalI] = false;
                                view.setBackgroundDrawable(null);
                                ((TextView) view).setTextColor(textColor);
                            }
                        }
                    });
                    holder.periodAlt[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View parentRow = (View) view.getParent().getParent().getParent();
                            ListView listView = (ListView) parentRow.getParent();
                            int position = listView.getPositionForView(parentRow);
                            PeriodItem item = objects.get(position);
                            if (item.days_alt.equals("0:0:0:0:0:0:0")) {
                                return;
                            }
                            if (!item.periodAlt[finalI]) {
                                item.periodAlt[finalI] = true;
                                holder.periodAlt[finalI].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                    holder.periodAlt[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                ((TextView) view).setTextColor(backgroundColor);
                            } else {
                                item.periodAlt[finalI] = false;
                                holder.periodAlt[finalI].setBackgroundDrawable(null);
                                ((TextView) view).setTextColor(textColor);
                            }
                        }
                    });
                }

                // Here set apply the data to the UI as if it were edited
                for (int i = 0; i < 3; i++) {
                    final int finalI = i;
                    holder.period[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View parentRow = (View) view.getParent().getParent().getParent();
                            ListView listView = (ListView) parentRow.getParent();
                            int position = listView.getPositionForView(parentRow);
                            PeriodItem item = objects.get(position);
                            if (item.days.equals("0:0:0:0:0:0:0")) {
                                return;
                            }
                            if (!item.period[finalI]) {
                                item.period[finalI] = true;
                                holder.period[finalI].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                    holder.period[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                ((TextView) view).setTextColor(backgroundColor);
                            } else {
                                item.period[finalI] = false;
                                holder.period[finalI].setBackgroundDrawable(null);
                                ((TextView) view).setTextColor(textColor);
                            }
                        }
                    });
                    holder.periodAlt[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View parentRow = (View) view.getParent().getParent().getParent();
                            ListView listView = (ListView) parentRow.getParent();
                            int position = listView.getPositionForView(parentRow);
                            PeriodItem item = objects.get(position);
                            if (item.days_alt.equals("0:0:0:0:0:0:0")) {
                                return;
                            }
                            if (!item.periodAlt[finalI]) {
                                item.periodAlt[finalI] = true;
                                holder.periodAlt[finalI].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                    holder.periodAlt[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                ((TextView) view).setTextColor(backgroundColor);
                            } else {
                                item.periodAlt[finalI] = false;
                                holder.periodAlt[finalI].setBackgroundDrawable(null);
                                ((TextView) view).setTextColor(textColor);
                            }
                        }
                    });
                }

                row.findViewById(R.id.delete_time).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        remove(item);
                        notifyDataSetChanged();
                    }
                });
                row.findViewById(R.id.delete_period).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        remove(item);
                        notifyDataSetChanged();
                    }
                });

            } else {
                row.findViewById(R.id.delete_time).setVisibility(View.GONE);
                row.findViewById(R.id.delete_period).setVisibility(View.GONE);
            }

        } else if (basis.equals("2")) {
            row.findViewById(R.id.block_layout).setVisibility(View.VISIBLE);
            row.findViewById(R.id.period_layout).setVisibility(View.GONE);
            row.findViewById(R.id.time_layout).setVisibility(View.GONE);
            holder.period[0] = (TextView) row.findViewById(R.id.one_block);
            holder.period[1] = (TextView) row.findViewById(R.id.two_block);
            holder.period[2] = (TextView) row.findViewById(R.id.three_block);
            holder.period[3] = (TextView) row.findViewById(R.id.four_block);

            holder.periodAlt[0] = (TextView) row.findViewById(R.id.one_alt_block);
            holder.periodAlt[1] = (TextView) row.findViewById(R.id.two_alt_block);
            holder.periodAlt[2] = (TextView) row.findViewById(R.id.three_alt_block);
            holder.periodAlt[3] = (TextView) row.findViewById(R.id.four_alt_block);

            if (context instanceof NewScheduleActivity) {
//                    if (!basis.equals("2"))
//                    holder.days.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            DialogFragment fragment = DaysDialog.newInstance();
//                            Bundle args = new Bundle();
//                            args.putString(context.getString(R.string.ARGUMENT_DAYS), item.days);
//                            args.putInt(context.getString(R.string.ARGUMENT_POSITION), item.position);
//                            fragment.setArguments(args);
//                            fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
//                        }
//                    });
//
//                    if (!basis.equals("2"))
//                    holder.daysAlt.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            DialogFragment fragment = DaysDialog.newInstance();
//                            Bundle args = new Bundle();
//                            args.putString(context.getString(R.string.ARGUMENT_DAYS), item.days);
//                            args.putBoolean(context.getString(R.string.ARGUMENT_ALTERNATE), true);
//                            args.putInt(context.getString(R.string.ARGUMENT_POSITION), item.position);
//                            fragment.setArguments(args);
//                            fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
//                        }
//                    });

                if (basis.equals("2"))
                    for (int i = 0; i < 4; i++) {
                        final int finalI = i;
                        holder.period[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                View parentRow = (View) view.getParent().getParent().getParent().getParent();
                                ListView listView = (ListView) parentRow.getParent();
                                int position = listView.getPositionForView(parentRow);
                                PeriodItem item = objects.get(position);
                                if (!item.period[finalI]) {
                                    item.period[finalI] = true;
                                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                        view.setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                    ((TextView) view).setTextColor(backgroundColor);
                                } else {
                                    item.period[finalI] = false;
                                    view.setBackgroundDrawable(null);
                                    ((TextView) view).setTextColor(textColor);
                                }
                            }
                        });
                        holder.periodAlt[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                View parentRow = (View) view.getParent().getParent().getParent().getParent();
                                ListView listView = (ListView) parentRow.getParent();
                                int position = listView.getPositionForView(parentRow);
                                PeriodItem item = objects.get(position);
                                if (!item.periodAlt[finalI]) {
                                    item.periodAlt[finalI] = true;
                                    holder.periodAlt[finalI].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                        holder.periodAlt[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                    ((TextView) view).setTextColor(backgroundColor);
                                } else {
                                    item.periodAlt[finalI] = false;
                                    holder.periodAlt[finalI].setBackgroundDrawable(null);
                                    ((TextView) view).setTextColor(textColor);
                                }
                            }
                        });
                    }

                // Here set apply the data to the UI as if it were edited
                for (int i = 0; i < 3; i++) {
                    final int finalI = i;
                    holder.period[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View parentRow = (View) view.getParent().getParent().getParent().getParent();
                            ListView listView = (ListView) parentRow.getParent();
                            int position = listView.getPositionForView(parentRow);
                            PeriodItem item = objects.get(position);
                            if (!item.period[finalI]) {
                                item.period[finalI] = true;
                                holder.period[finalI].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                    holder.period[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                ((TextView) view).setTextColor(backgroundColor);
                            } else {
                                item.period[finalI] = false;
                                holder.period[finalI].setBackgroundDrawable(null);
                                ((TextView) view).setTextColor(textColor);
                            }
                        }
                    });
                    holder.periodAlt[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View parentRow = (View) view.getParent().getParent().getParent().getParent();
                            ListView listView = (ListView) parentRow.getParent();
                            int position = listView.getPositionForView(parentRow);
                            PeriodItem item = objects.get(position);
                            if (!item.periodAlt[finalI]) {
                                item.periodAlt[finalI] = true;
                                holder.periodAlt[finalI].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                    holder.periodAlt[finalI].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                                ((TextView) view).setTextColor(backgroundColor);
                            } else {
                                item.periodAlt[finalI] = false;
                                holder.periodAlt[finalI].setBackgroundDrawable(null);
                                ((TextView) view).setTextColor(textColor);
                            }
                        }
                    });
                }

            }

        }

        // The days
        if (item.days != null) {
            if (!basis.equals("2")) {
                holder.days.setText(getDayString(item.days));
                holder.daysAlt.setText(getDayString(item.days_alt));

                if (basis.equals("0")) {
                    if (item.days.equals("0:0:0:0:0:0:0")) {
                        holder.timein.setEnabled(false);
                        holder.timeout.setEnabled(false);
                        holder.timein.setTextColor(context.getResources().getColor(R.color.gray_500));
                        holder.timeout.setTextColor(context.getResources().getColor(R.color.gray_500));
                    } else {
                        holder.timein.setEnabled(true);
                        holder.timeout.setEnabled(true);
                        holder.timein.setTextColor(textColor);
                        holder.timeout.setTextColor(textColor);
                    }

                    if (item.days_alt.equals("0:0:0:0:0:0:0")) {
                        holder.timeinAlt.setEnabled(false);
                        holder.timeoutAlt.setEnabled(false);
                        holder.timeinAlt.setTextColor(context.getResources().getColor(R.color.gray_500));
                        holder.timeoutAlt.setTextColor(context.getResources().getColor(R.color.gray_500));
                    } else {
                        holder.timeinAlt.setEnabled(true);
                        holder.timeoutAlt.setEnabled(true);
                        holder.timeinAlt.setTextColor(textColor);
                        holder.timeoutAlt.setTextColor(textColor);
                    }
                }
            }
        }

        // The Periods
        if (!basis.equals("0")) {
            int totalPeriods;
            if (basis.equals("1")) totalPeriods = 8;
            else totalPeriods = 4;
            if (item.period != null)
                for (int i = 0; i < totalPeriods; i++) {
                    if (item.period[i]) {
                        item.period[i] = true;
                        holder.period[i].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                            holder.period[i].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                        holder.period[i].setTextColor(backgroundColor);
                    } else {
                        item.period[i] = false;
                        holder.period[i].setBackgroundDrawable(null);
                        holder.period[i].setTextColor(textColor);
                    }

                    if (item.periodAlt[i]) {
                        item.periodAlt[i] = true;
                        holder.periodAlt[i].setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_period_button));
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                            holder.periodAlt[i].setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
                        holder.periodAlt[i].setTextColor(backgroundColor);
                    } else {
                        item.periodAlt[i] = false;
                        holder.periodAlt[i].setBackgroundDrawable(null);
                        holder.periodAlt[i].setTextColor(textColor);
                    }
                }
        }

        if (basis.equals("1")) {
            if (item.days.equals("0:0:0:0:0:0:0"))
                for (int i = 0; i < holder.period.length; i++)
                    holder.period[i].setTextColor(context.getResources().getColor(R.color.gray_500));

            if (item.days_alt.equals("0:0:0:0:0:0:0"))
                for (int i = 0; i < holder.periodAlt.length; i++)
                    holder.periodAlt[i].setTextColor(context.getResources().getColor(R.color.gray_500));
        }

        return row;
    }


    static class ViewHolder {
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
        for (int i = 1; i < 6; i++)
            weekdayList.add(i);
        ArrayList<Integer> selectedDays = new ArrayList<>();
        String[] daysArray = days.split(":");
        for (int i = 0; i < daysArray.length; i++) {
            if (daysArray[i].equals("1")) selectedDays.add(i);
        }

        if (selectedDays.size() == 0)
            return context.getString(R.string.none);
        else if (selectedDays.size() == 1) {
            int day = selectedDays.get(0);
            return dayNames[day];
        }
        if (selectedDays.size() == 7)
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

}