package com.pdt.plume;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;

// TODO: Implement actions to read, insert, update, and delete database data
public class ScheduleFragment extends Fragment {
    String LOG_TAG = ScheduleFragment.class.getSimpleName();
    Utlility utlility = new Utlility();

    ArrayList<Integer> highlightedItems = new ArrayList<>();


    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        // Get Schedule Data
        DbHelper dbHelper = new DbHelper(getContext());

        // Initialise Schedule List
        final ListView listView = (ListView) rootView.findViewById(R.id.schedule_list);
        final ScheduleAdapter mScheduleAdapter = new ScheduleAdapter(getContext(), R.layout.list_item_schedule, dbHelper.getCurrentDayScheduleArray());
        if (listView != null){
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(clickListener());
            listView.setOnItemLongClickListener(longClickListener());

            if (getResources().getBoolean(R.bool.isTablet))
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
        }

        //Initialise fab
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewScheduleActivity.class);
                startActivity(intent);
            }
        });



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

    private void highlight(View view, int position){
        //If the selected item is already highlighted, deselect it
        ImageView scheduleIcon = (ImageView) view.findViewById(R.id.schedule_icon);
        if (highlightedItems.contains(position)){
            DbHelper dbHelper = new DbHelper(getContext());
            dbHelper.getCurrentDayScheduleData();
            Cursor cursor = dbHelper.getCurrentDayScheduleData();
            if (cursor.moveToPosition(position)){
                int imageId = cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
                scheduleIcon.setImageResource(imageId);
            }
            Log.v(LOG_TAG, "" + position);
            highlightedItems.remove(position);
            highlightedItems.add(position, -1);
        }
        else {
            Log.v(LOG_TAG, "" + position);
            highlightedItems.add(position, position);
            scheduleIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_checked));
        }
    }

    public AdapterView.OnItemLongClickListener longClickListener(){
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                highlight(view, position);
                return true;
            }
        };
    }

    public AdapterView.OnItemClickListener clickListener(){

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // If no other items are highlighted, open fragment as normal
                int nullCounter = -1;
                for (int i = 0; i < highlightedItems.size(); i++){
                    int integer = highlightedItems.get(i);
                    if (i != -1)
                        nullCounter++;
                }
                Log.v(LOG_TAG, "Null Counter: " + nullCounter);
                if (nullCounter < 0){
                    //Replace fragment if device is a tablet
                    if (getResources().getBoolean(R.bool.isTablet)){
                        ScheduleDetailFragment fragment = new ScheduleDetailFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.detail_container, fragment)
                                .commit();
                    }
                    //Start a new activity if device is a phone
                    else{
                        Intent intent = new Intent(getActivity(), ScheduleDetailActivity.class);
                        startActivity(intent);
                    }
                }
                else {
                    highlight(view, position);
                }
            }
        };
    }

}
