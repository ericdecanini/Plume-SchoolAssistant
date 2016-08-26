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
import android.widget.TextView;
import android.widget.Toast;

import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleFragment extends Fragment {
    // Constantly used variables
    String LOG_TAG = ScheduleFragment.class.getSimpleName();
    Utility utility = new Utility();

    // CAM Variables
    private Menu mActionMenu;
    private int mOptionMenuCount;

    // UI Elements
    ListView listView;

    // Flags
    boolean isTablet;
    public static boolean showBlockHeaderA = false;
    public static boolean showBlockHeaderB = false;

    // Required empty public constructor
    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Check if the used device is a tablet
        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Get a reference to the database
        DbHelper dbHelper = new DbHelper(getContext());

        // Get a reference to the list view and create its adapter
        // using the current day schedule data
        listView = (ListView) rootView.findViewById(R.id.schedule_list);
        final ScheduleAdapter mScheduleAdapter = new ScheduleAdapter(getContext(),
                R.layout.list_item_schedule, dbHelper.getCurrentDayScheduleArray(getContext()));
        TextView headerTextView = (TextView) rootView.findViewById(R.id.header_textview);
        if (showBlockHeaderA){
            headerTextView.setText(getString(R.string.class_time_block_day_a));
        } else if (showBlockHeaderB){
            headerTextView.setText(getString(R.string.class_time_block_day_b));
        } else {
            Calendar c = Calendar.getInstance();
            headerTextView.setText(utility.formatDateString(getContext(), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
        }

        // Set the adapter and listeners of the list view
        if (listView != null) {
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(listener());
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new ModeCallback());

            if (isTablet)
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
        }

        // Get a reference to the FAB and set its OnClickListener
        // which is an intent to add a new schedule
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

    public AdapterView.OnItemClickListener listener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // If the used device is a tablet, replace the
                // right-hand side fragment with a ScheduleDetailFragment
                // passing the data of the clicked row to the fragment
                if (isTablet) {
                    ScheduleDetailFragment fragment = new ScheduleDetailFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, fragment)
                            .commit();
                }

                // If the used device is a phone, start a new ScheduleDetailActivity
                // passing the data of the clicked row to the fragment
                else {
                    Intent intent = new Intent(getActivity(), ScheduleDetailActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    // Subclass for the Contextual Action Mode
    private class ModeCallback implements ListView.MultiChoiceModeListener {

        List<Integer> CAMselectedItemsList = new ArrayList<>();

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            // Get the number of list items selected
            // and set the window subtitle based on that
            final int checkedCount = listView.getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                    mOptionMenuCount = 0;
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mOptionMenuCount = 1;
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;
            }

            // If the clicked item became selected, add it to
            // an array list of selected items
            if (checked)
                CAMselectedItemsList.add(position);

            // If the clicked item became deselected, get its item id
            // and remove it from the array list
            else {
                int itemId = -1;
                // Scan through the array list until the
                // item's value matches its position
                // When it does, set the itemId to the matched position
                // and then remove the item in that array list
                // matching that position
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    if (position == CAMselectedItemsList.get(i)) {
                        itemId = i;
                    }
                }
                if (itemId != -1)
                    CAMselectedItemsList.remove(itemId);
            }

            // Invalidating the Action Mode calls onPrepareActionMode
            // which will show or hide the edit menu action based on
            // the number of items selected
            mode.invalidate();
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            // Inflate the action menu and set the global menu variable
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_single, menu);
            mActionMenu = menu;

            // Set the title and colour of the contextual action bar
            mode.setTitle(getContext().getString(R.string.select_items));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.gray_700));
            getActivity().findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.gray_500));
            if (!isTablet)
                getActivity().findViewById(R.id.tabs).setBackgroundColor(getResources().getColor(R.color.gray_500));

            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            // Checks the count of items selected.
            // If it is one, show the edit menu action.
            // If it is more than one, hide the edit menu action.
            MenuItem menuItem = mActionMenu.findItem(R.id.action_edit);
            if (mOptionMenuCount == 0)
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
            // Clear the array list of selected items and revert the window colour back to normal
            CAMselectedItemsList.clear();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getActivity().findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            if (!isTablet)
                getActivity().findViewById(R.id.tabs).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        private void deleteSelectedItems() {
            // Get a reference to the database
            DbHelper db = new DbHelper(getActivity());

            // Get a cursor by getting the currentDayScheduleData
            // Which should match the list view of the ScheduleFragment
            Cursor cursor = db.getCurrentDayScheduleData();

            // Delete all the selected items based on the itemIDs
            // Stored in the array list
            for(int i = 0; i < CAMselectedItemsList.size(); i++) {
                if (cursor.moveToPosition(CAMselectedItemsList.get(i))) {
                    db.deleteScheduleItem(cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID)));
                }
            }

            cursor.close();

            // Get the list view's current adapter, clear it,
            // and query the database again for the current day
            // data, then notify the adapter for the changes
            ScheduleAdapter adapter = (ScheduleAdapter) listView.getAdapter();
            adapter.clear();
            adapter.addAll(db.getCurrentDayScheduleArray(getContext()));
            adapter.notifyDataSetChanged();

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(){
            // Ensure that only one item is selected
            if (CAMselectedItemsList.size() == 1){
                // Initialise Id and Title variables
                int id;
                String title;

                // Get a reference to the database and
                // Get a cursor of the current day schedule data
                DbHelper db = new DbHelper(getActivity());
                Cursor cursor = db.getCurrentDayScheduleData();

                // Move the cursor to the position of the selected item
                if (cursor.moveToPosition(CAMselectedItemsList.get(0))){
                    // Get its Id and Title
                    id = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                    title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
                    cursor.close();

                    // Create an intent to NewScheduleActivity and include the selected
                    // item's id, title, and an edit flag as extras
                    Intent intent = new Intent(getActivity(), NewScheduleActivity.class);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_ID), id);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_TITLE),title);
                    intent.putExtra(getResources().getString(R.string.SCHEDULE_FLAG_EDIT), true);

                    // Clear the selected items list, exit the CAM and launch the activity
                    CAMselectedItemsList.clear();
                    getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                    startActivity(intent);
                }
            }

            // If more than one item was selected, throw a warning log
            else {
                Log.w(LOG_TAG, "Cancelling event due to more than one item selected");
            }
        }
    }

}
