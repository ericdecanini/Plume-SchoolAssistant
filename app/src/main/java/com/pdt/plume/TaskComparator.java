package com.pdt.plume;


import java.util.Comparator;

public class TaskComparator implements Comparator<Task> {

    String LOG_TAG = TaskComparator.class.getSimpleName();

    @Override
    public int compare(Task o1, Task o2) {
        if (o1.taskDueDate > o2.taskDueDate)
            return 1;
        else return -1;
    }

}
