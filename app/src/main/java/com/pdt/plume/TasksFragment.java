package com.pdt.plume;


import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TasksFragment extends Fragment {
    String LOG_TAG = TasksFragment.class.getSimpleName();
    boolean isTablet;

    ListView listView;
    private int mOptionMenu;
    private Menu mActionMenu;

    // TODO: Implement automatic switch to Tasks tab on returning from NewTaskActivity
    public TasksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        DbHelper dbHelper = new DbHelper(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
        listView = (ListView) rootView.findViewById(R.id.tasks_list);
        TaskAdapter mAdapter = new TaskAdapter(getContext(), R.layout.list_item_task, dbHelper.getTaskDataArray());
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(clickListener());
        listView.setMultiChoiceModeListener(new ModeCallback());
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

//    private Task generateDummyTask(int count){
//        Resources resources = getResources();
//        int[] taskIcons = {
//                R.drawable.placeholder_sixtyfour,
//                R.drawable.placeholder_sixtyfour,
//                R.drawable.placeholder_sixtyfour
//        };
//
//        long[] taskDates = {
//                1,
//                2,
//                3
//        };
//
//        return new Task(
//                taskIcons[count],
//                resources.getStringArray(R.array.tasks_titles)[count],
//                resources.getStringArray(R.array.tasks_shareds)[count],
//                resources.getStringArray(R.array.tasks_descriptions)[count],
//                resources.getStringArray(R.array.tasks_attachments)[count],
//                taskDates[count]);
//    }
//
//    private Task[] generateDummyTaskArray(){
//        return new Task[]{
//                generateDummyTask(0),
//                generateDummyTask(1),
//                generateDummyTask(2)
//        };
//    }


    public AdapterView.OnItemClickListener clickListener() {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Replace fragment if device is a tablet
                if (isTablet) {
                    TasksDetailFragment fragment = new TasksDetailFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, fragment)
                            .commit();
                }
                //Start a new activity if device is a phone
                else {
                    Intent intent = new Intent(getActivity(), TasksDetailActivity.class);
                    startActivity(intent);
                }
            }
        };
    }


    private class ModeCallback implements ListView.MultiChoiceModeListener {

        List<Integer> positionsList = new ArrayList<>();

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = listView.getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                    mOptionMenu = 0;
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mOptionMenu = 1;
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;
            }
            if (checked)
                positionsList.add(position);
            else {
                int itemId = -1;
                for (int i = 0; i < positionsList.size(); i++) {
                    if (position == positionsList.get(i)) {
                        itemId = i;
                    }
                }
                if (itemId != -1)
                    positionsList.remove(itemId);
            }
            mode.invalidate();
        }


        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_single, menu);
            mActionMenu = menu;
            mode.setTitle("Select Items");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.gray_700));
            getActivity().findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.gray_500));
            if (!isTablet)
                getActivity().findViewById(R.id.tabs).setBackgroundColor(getResources().getColor(R.color.gray_500));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            MenuItem menuItem = mActionMenu.findItem(R.id.action_edit);
            if (mOptionMenu == 0)
                menuItem.setVisible(true);
            else
                menuItem.setVisible(false);
            return true;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    break;
                case R.id.action_edit:
                    editSelectedItem();
                    break;
                default:
                    Toast.makeText(getActivity(), "Clicked " + item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getActivity().findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            if (!isTablet)
                getActivity().findViewById(R.id.tabs).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        private void deleteSelectedItems() {

            DbHelper db = new DbHelper(getActivity());
            Cursor cursor = db.getTaskData();
            for(int i = 0; i < positionsList.size(); i++) {
                if (cursor.moveToPosition(positionsList.get(i))) {
                    db.deleteTaskItem(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
                }
            }
            cursor.close();
            TaskAdapter adapter = (TaskAdapter) listView.getAdapter();
            adapter.clear();
            adapter.addAll(db.getTaskDataArray());
            adapter.notifyDataSetChanged();
            positionsList.clear();
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(){
            if (positionsList.size() == 1){
                int id;
                String title = "";
                String sharer = "";
                String description = "";
                String attachment = "";
                float dueDate = 0f;
                float alarmTime = 0f;
                DbHelper db = new DbHelper(getActivity());
                Cursor cursor = db.getTaskData();
                if (cursor.moveToPosition(positionsList.get(0))){
                    id = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                    title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                    sharer = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_SHARER));
                    description = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                    attachment = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                    dueDate = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE));
                    alarmTime = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ALARMTIME));
                    cursor.close();
                    Intent intent = new Intent(getActivity(), NewTaskActivity.class);
                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ID), id);
                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_TITLE),title);
                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_SHARER), sharer);
                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_DESCRIPTION), description);
                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ATTACHMENT), attachment);
                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_DUEDATE), dueDate);
                    intent.putExtra(getResources().getString(R.string.TASKS_EXTRA_ALARMTIME), alarmTime);
                    intent.putExtra(getResources().getString(R.string.TASKS_FLAG_EDIT), true);
                    startActivity(intent);
                }
            } else {
                Log.w(LOG_TAG, "Cancelling event due to more than one item selected");
            }

        }
    }


}
