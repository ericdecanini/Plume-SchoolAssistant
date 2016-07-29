package com.pdt.plume;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        ListView mListView = (ListView) rootView.findViewById(R.id.tasks_list);
        TaskAdapter mAdapter = new TaskAdapter(getContext(), R.layout.list_item_task, generateDummyTaskArray());
        mListView.setAdapter(mAdapter);

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
