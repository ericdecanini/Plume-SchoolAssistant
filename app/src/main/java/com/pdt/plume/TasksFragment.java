package com.pdt.plume;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * A simple {@link Fragment} subclass.
 */
public class TasksFragment extends Fragment {


    public TasksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.tasks_list);
        TaskAdapter mAdapter = new TaskAdapter(getContext(), R.layout.list_item_task, generateDummyTaskArray());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Replace fragment if device is a tablet
                if (getResources().getBoolean(R.bool.isTablet)){
                    TasksDetailFragment fragment = new TasksDetailFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, fragment)
                            .commit();
                } 
                //Start a new activity if device is a phone
                else{
                    Intent intent = new Intent(getContext(), TasksDetailActivity.class);
                    startActivity(intent);
                }
            }
        });
        if (getResources().getBoolean(R.bool.isTablet))
            listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());

        //Initialise the fab
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewTaskActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private Task generateDummyTask(int count){
        Resources resources = getResources();
        int[] taskIcons = {
                R.drawable.placeholder_sixtyfour,
                R.drawable.placeholder_sixtyfour,
                R.drawable.placeholder_sixtyfour
        };

        long[] taskDates = {
                1,
                2,
                3
        };

        return new Task(
                taskIcons[count],
                resources.getStringArray(R.array.tasks_titles)[count],
                resources.getStringArray(R.array.tasks_shareds)[count],
                resources.getStringArray(R.array.tasks_descriptions)[count],
                resources.getStringArray(R.array.tasks_attachments)[count],
                taskDates[count]);
    }

    private Task[] generateDummyTaskArray(){
        return new Task[]{
                generateDummyTask(0),
                generateDummyTask(1),
                generateDummyTask(2)
        };
    }

}
