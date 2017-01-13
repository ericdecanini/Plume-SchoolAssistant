package com.pdt.plume;

import static android.R.attr.description;

public class Task {
    public String taskIcon;
    public String taskTitle;
    public String taskShared;
    public String taskClass;
    public String taskType;
    public String taskDescription;
    public String taskAttachment;
    public float taskDueDate;
    public float alarmTime;
    public Task(){
        super();
    }

    // The global variables are accessed by the mScheduleAdapter
    // Set them using the input parameters
    public Task(String icon, String title, String sharer, String taskClass, String taskType, String description, String attachment, float dueDate, float alarmTime) {
        super();
        this.taskIcon = icon;
        this.taskTitle = title;
        this.taskShared = sharer;
        this.taskClass = taskClass;
        this.taskType = taskType;
        this.taskDescription = description;
        this.taskAttachment = attachment;
        this.taskDueDate = dueDate;
        this.alarmTime = alarmTime;
    }
}