package com.pdt.plume;


import android.util.Log;

import java.util.Comparator;

public class ScheduleComparator implements Comparator<Schedule> {

    String LOG_TAG = ScheduleComparator.class.getSimpleName();

    @Override
    public int compare(Schedule o1, Schedule o2) {
        String[] oA = o1.scheduleTimeIn.split(":");
        String[] oB = o2.scheduleTimeIn.split(":");

        if (oA.length == 2)
            if (oA[0].length() < oB[0].length())
                return -1;
            else if (oA[0].length() > oB[0].length())
                return 1;

        return o1.scheduleTimeIn.compareTo(o2.scheduleTimeIn);
    }

}
