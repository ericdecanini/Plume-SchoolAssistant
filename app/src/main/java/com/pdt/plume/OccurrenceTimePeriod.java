package com.pdt.plume;


public class OccurrenceTimePeriod {
    public String time_period;
    public String sunday;
    public String monday;
    public String tuesday;
    public String wednesday;
    public String thursday;
    public String friday;
    public String saturday;

    public String time_period_alt;
    public String sunday_alt;
    public String monday_alt;
    public String tuesday_alt;
    public String wednesday_alt;
    public String thursday_alt;
    public String friday_alt;
    public String saturday_alt;

    public OccurrenceTimePeriod(String time_period, String time_period_alt, String periods, String occurrence){
        super();
        this.time_period = time_period;
        String[] days = occurrence.split(":");

        if (days[0].equals("0")){
            sunday = "0";
            sunday_alt = "0";
        } else if (days[0].equals("1")){
            sunday = "1";
            sunday_alt = "0";
        } else if (days[0].equals("2")){
            sunday = "0";
            sunday_alt = "1";
        } else if (days[0].equals("3")){
            sunday = "1";
            sunday_alt = "1";
        }

        if (days[1].equals("0")){
            monday = "0";
            monday_alt = "0";
        } else if (days[1].equals("1")){
            monday = "1";
            monday_alt = "0";
        } else if (days[1].equals("2")){
            monday = "0";
            monday_alt = "1";
        } else if (days[1].equals("3")){
            monday = "1";
            monday_alt = "1";
        }

        if (days[2].equals("0")){
            tuesday = "0";
            tuesday_alt = "0";
        } else if (days[2].equals("1")){
            tuesday = "1";
            tuesday_alt = "0";
        } else if (days[2].equals("2")){
            tuesday = "0";
            tuesday_alt = "1";
        } else if (days[2].equals("3")){
            tuesday = "1";
            tuesday_alt = "1";
        }

        if (days[3].equals("0")){
            wednesday = "0";
            wednesday_alt = "0";
        } else if (days[3].equals("1")){
            wednesday = "1";
            wednesday_alt = "0";
        } else if (days[3].equals("2")){
            wednesday = "0";
            wednesday_alt = "1";
        } else if (days[3].equals("3")){
            wednesday = "1";
            wednesday_alt = "1";
        }

        if (days[4].equals("0")){
            thursday = "0";
            thursday_alt = "0";
        } else if (days[4].equals("1")){
            thursday = "1";
            thursday_alt = "0";
        } else if (days[4].equals("2")){
            thursday = "0";
            thursday_alt = "1";
        } else if (days[4].equals("3")){
            thursday = "1";
            thursday_alt = "1";
        }

        if (days[5].equals("0")){
            friday = "0";
            friday_alt = "0";
        } else if (days[5].equals("1")){
            friday = "1";
            friday_alt = "0";
        } else if (days[5].equals("2")){
            friday = "0";
            friday_alt = "1";
        } else if (days[5].equals("3")){
            friday = "1";
            friday_alt = "1";
        }

        if (days[6].equals("0")){
            saturday = "0";
            saturday_alt = "0";
        } else if (days[6].equals("1")){
            saturday = "1";
            saturday_alt = "0";
        } else if (days[6].equals("2")){
            saturday = "0";
            saturday_alt = "1";
        } else if (days[6].equals("3")){
            saturday = "1";
            saturday_alt = "1";
        }

    }
}
