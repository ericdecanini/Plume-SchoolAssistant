package com.pdt.plume;


import java.util.Comparator;

public class ScheduleComparator implements Comparator<Schedule> {

    @Override
    public int compare(Schedule o1, Schedule o2) {
        return o1.scheduleTimeIn.compareTo(o2.scheduleTimeIn);
    }

}
