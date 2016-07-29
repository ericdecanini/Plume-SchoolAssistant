package com.pdt.plume;

public class Task {
    public int taskIcon;
    public String taskTitle;
    public String taskShared;
    public String taskDescription;
    public long taskDate;
    public Task(){
        super();
    }

    public Task(int icon, String title, String shared, String description, long date) {
        super();
        this.taskIcon = icon;
        this.taskTitle = title;
        this.taskShared = shared;
        this.taskDescription = description;
        this.taskDate = date;
    }
}