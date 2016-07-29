package com.pdt.plume;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleFragment extends Fragment {
    String LOG_TAG = ScheduleFragment.class.getSimpleName();


    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        Log.v(LOG_TAG, "Schedule Fragment Initialised");

        //Initialise Schedule List
        ListView listView = (ListView) rootView.findViewById(R.id.schedule_list);
        ScheduleAdapter mScheduleAdapter = new ScheduleAdapter(getContext(), R.layout.list_item_schedule, generateDummyScheduleArray());
        if (listView != null){
            Log.v(LOG_TAG, "ListView not null");
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ScheduleDetailActivity.class);
                }
            });
        } else Log.v(LOG_TAG, "ListView is null");


        // Inflate the layout for this fragment
        return rootView;
    }

    private Schedule generateDummySchedule(int count){
        Resources resources = getResources();
        int[] scheduleIcons = {
                R.drawable.placeholder_sixtyfour,
                R.drawable.placeholder_sixtyfour,
                R.drawable.placeholder_sixtyfour
        };

        return new Schedule(
                scheduleIcons[count],
                resources.getStringArray(R.array.schedule_lessons)[count],
                resources.getStringArray(R.array.schedule_teachers)[count],
                resources.getStringArray(R.array.schedule_rooms)[count],
                resources.getStringArray(R.array.schedule_time_ins)[count],
                resources.getStringArray(R.array.schedule_time_outs)[count]);
    }

    public Schedule[] generateDummyScheduleArray(){
        return new Schedule[]{
                generateDummySchedule(0),
                generateDummySchedule(1),
                generateDummySchedule(2)
        };
    }

}
