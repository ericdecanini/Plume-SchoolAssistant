package com.pdt.plume;

public class Task {
    public int taskIcon;
    public String taskTitle;
    public String taskShared;
    public String taskDescription;
    public String taskAttachment;
    public float taskDueDate;
    public float alarmTime;
    public Task(){
        super();
    }

    public Task(int icon, String title, String shared, String description, String attachment, float dueDate, float alarmTime) {
        super();
        this.taskIcon = icon;
        this.taskTitle = title;
        this.taskShared = shared;
        this.taskDescription = description;
        this.taskAttachment = attachment;
        this.taskDueDate = dueDate;
        this.alarmTime = alarmTime;
    }
}