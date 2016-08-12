package com.pdt.plume;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;


public class OccurrenceTimePeriodAdapter extends ArrayAdapter {

    public OccurrenceTimePeriodAdapter(Context context, int resource, ArrayList<OccurrenceTimePeriod> objects) {
        super(context, resource, objects);
    }
}
