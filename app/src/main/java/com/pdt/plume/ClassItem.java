package com.pdt.plume;

import java.util.ArrayList;

import static android.R.attr.id;
import static com.pdt.plume.R.id.flavour;

/**
 * Created by user on 03/12/2016.
 */

public class ClassItem {

    public String icon;
    public String title;
    public String teacher;
    public String room;
    public ArrayList<String> occurrences;
    public ArrayList<Long> timeIns;
    public ArrayList<Long> timeOuts;
    public ArrayList<Long> timeInAlts;
    public ArrayList<Long> timeOutAlts;
    public ArrayList<String> periods;

    public ClassItem() {
        super();
    }

    public ClassItem(String icon, String title, String teacher, String room, ArrayList<String> occurrences,
                     ArrayList<Long> timeIns, ArrayList<Long> timeInAlts,
                     ArrayList<Long> timeOuts, ArrayList<Long> timeOutAlts,
                     ArrayList<String> periods) {
        super();
        this.icon = icon;
        this.title = title;
        this.teacher = teacher;
        this.room = room;
        this.occurrences = occurrences;
        this.timeIns = timeIns;
        this.timeInAlts = timeInAlts;
        this.timeOuts = timeOuts;
        this.timeOutAlts = timeOutAlts;
        this.periods = periods;
    }

}
