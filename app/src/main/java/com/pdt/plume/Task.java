package com.pdt.plume;

public class Task {
    public int taskIcon;
    public String taskTitle;
    public String taskShared;
    public String taskDescription;
    public String taskAttachment;
    public long taskDate;
    public Task(){
        super();
    }

    public Task(int icon, String title, String shared, String description, String attachment, long date) {
        super();
        this.taskIcon = icon;
        this.taskTitle = title;
        this.taskShared = shared;
        this.taskDescription = description;
        this.taskAttachment = attachment;
        this.taskDate = date;
    }
}