package com.pdt.plume;

public class Schedule {
    public int scheduleIcon;
    public String scheduleLesson;
    public String scheduleTeacher;
    public String scheduleRoom;
    public String scheduleTimeIn;
    public String scheduleTimeOut;
    public Schedule(){
        super();
    }

    // The global variables are accessed by the adapter
    // Set them using the input parameters
    public Schedule(int icon, String lesson, String teacher, String room, String timeIn, String timeOut) {
        super();
        this.scheduleIcon = icon;
        this.scheduleLesson = lesson;
        this.scheduleTeacher = teacher;
        this.scheduleRoom = room;
        this.scheduleTimeIn = timeIn;
        this.scheduleTimeOut = timeOut;
    }
}