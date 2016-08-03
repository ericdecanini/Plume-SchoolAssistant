package com.pdt.plume;


import android.content.Intent;
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
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.util.ArrayList;
import java.util.List;

// TODO: Implement actions to read, insert, update, and delete database data
public class ScheduleFragment extends Fragment {
    String LOG_TAG = ScheduleFragment.class.getSimpleName();
    Utlility utlility = new Utlility();
    boolean isTablet;

    ListView listView;
    private int mOptionMenu;
    private Menu mActionMenu;


    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        isTablet = getResources().getBoolean(R.bool.isTablet);
        // Get Schedule Data
        DbHelper dbHelper = new DbHelper(getContext());

        // Initialise Schedule List
        listView = (ListView) rootView.findViewById(R.id.schedule_list);
        final ScheduleAdapter mScheduleAdapter = new ScheduleAdapter(getContext(), R.layout.list_item_schedule, dbHelper.getCurrentDayScheduleArray());
        if (listView != null) {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(clickListener());
            listView.setMultiChoiceModeListener(new ModeCallback());

            if (isTablet)
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

    public AdapterView.OnItemClickListener clickListener() {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Replace fragment if device is a tablet
                if (isTablet) {
                    ScheduleDetailFragment fragment = new ScheduleDetailFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, fragment)
                            .commit();
                }
                //Start a new activity if device is a phone
                else {
                    Intent intent = new Intent(getActivity(), ScheduleDetailActivity.class);
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
                                Log.v(LOG_TAG, "i = " + positionsList.get(i));
                                itemId = i;
                            }
                        }
                        Log.v(LOG_TAG, "ItemId = " + itemId);
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
            Cursor cursor = db.getCurrentDayScheduleData();
            for(int i = 0; i < positionsList.size(); i++) {
                if (cursor.moveToPosition(positionsList.get(i))) {
                    String tempString = cursor.getString(cursor.getColumnIndex(ScheduleEntry._ID));
                    Log.v(LOG_TAG, "tempString = " + tempString);
                    Log.v(LOG_TAG, "positionsListGetIIndexId = " + cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID)));
                    db.deleteScheduleItem(cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID)));
                }
            }
            cursor.close();
            ScheduleAdapter adapter = (ScheduleAdapter) listView.getAdapter();
            adapter.clear();
            adapter.addAll(db.getCurrentDayScheduleArray());
            adapter.notifyDataSetChanged();
            positionsList.clear();
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(){
            if (positionsList.size() == 1){
                int id;
                String title = "";
                String teacher = "";
                String room = "";
                float timeIn = 0f;
                float timeOut = 0f;
                DbHelper db = new DbHelper(getActivity());
                Cursor cursor = db.getCurrentDayScheduleData();
                if (cursor.moveToPosition(positionsList.get(0))){
                    id = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                    title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
                    teacher = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER));
                    room = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM));
                    timeIn = cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN));
                    timeOut = cursor.getFloat(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT));
                    cursor.close();
                    Intent intent = new Intent(getActivity(), NewScheduleActivity.class);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_ID), id);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_TITLE),title);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_TEACHER), teacher);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_ROOM), room);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_TIMEIN), timeIn);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_TIMEOUT), timeOut);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_FLAG_EDIT), true);
                    startActivity(intent);
                }
            } else {
                Log.w(LOG_TAG, "Cancelling event due to more than one item selected");
            }

        }
    }








}
