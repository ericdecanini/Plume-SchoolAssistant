package com.pdt.plume;


import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pdt.plume.R.id.timein;

public class ClassFragment extends Fragment{

    // Constantly used variables
    String LOG_TAG = ScheduleFragment.class.getSimpleName();
    Utility utility = new Utility();

    // CAM Variables
    private Menu mActionMenu;
    private int mOptionMenuCount;

    // UI Elements
    ListView listView;
    TextView headerTextView;
    FloatingActionButton fab;
    ProgressBar spinner;

    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // UI Data
    ScheduleAdapter mScheduleAdapter;
    ArrayList<Schedule> mScheduleList = new ArrayList<>();

    // Flags
    boolean isTablet;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    DatabaseReference classesRef;

    // Required empty public constructor
    public ClassFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_class, container, false);

        // Get references to the views
        headerTextView = (TextView) rootView.findViewById(R.id.header_textview);
        listView = (ListView) rootView.findViewById(R.id.schedule_list);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        // Check if the used device is a tablet
        isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) fab.setAlpha(1f);

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Apply the list data to the listview mTasksAdapter
        querySchedule();
        mScheduleAdapter = new ScheduleAdapter(getContext(),
                R.layout.list_item_schedule, mScheduleList);

        // Set the mTasksAdapter and listeners of the list view
        if (listView != null) {
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(ItemClickListener());
            if (!isTablet) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                listView.setMultiChoiceModeListener(new ModeCallback());
            }
            else listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            if (isTablet && mScheduleList.size() > 0)
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());

            if (isTablet && mScheduleList.size() == 0)
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new BlankFragment())
                        .commit();

            if (getArguments() != null) {
                int position = getArguments().getInt(getString(R.string.INTENT_EXTRA_POSITION), 0);
                String RETURN_TO_SCHEDULE = getArguments().getString(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE));
                if (isTablet && RETURN_TO_SCHEDULE != null && position > -1) {
                    listView.performItemClick(listView.getChildAt(position), position, listView.getItemIdAtPosition(position));
                }
            }

        }

        // Set the action for the FAB
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewScheduleActivity.class);
                startActivity(intent);
            }
        });

        int backgroundColor = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));
        rootView.setBackgroundColor(backgroundColor);

        if (isTablet)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, new BlankFragment())
                    .commit();

        // Inflate the layout for this fragment
        return rootView;
    }

    void querySchedule() {
        if (mFirebaseUser != null) {
            getClassesFromFirebase();
            // Check if the classes ref doesn't exist
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("classes").getChildrenCount() == 0) {
                        spinner.setVisibility(View.GONE);
                        headerTextView.setVisibility(View.VISIBLE);
                    } else {
                        listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            // Get the schedule data from SQLite
            DbHelper dbHelper = new DbHelper(getContext());
            ArrayList<Schedule> newSchedules = dbHelper.getAllClassesArray(getContext());
            mScheduleList.clear();
            mScheduleList.addAll(newSchedules);
            mScheduleAdapter.notifyDataSetChanged();
            spinner.setVisibility(View.GONE);
        }

        // Set the splash text if there's no classes queried and update the Adapter
        if (mScheduleList.size() == 0)
            headerTextView.setVisibility(View.VISIBLE);
        else headerTextView.setVisibility(View.GONE);

        if (mScheduleAdapter != null) {
            mScheduleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        querySchedule();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), R.color.colorPrimary);
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            fab.setBackgroundTintList((ColorStateList.valueOf(mSecondaryColor)));
    }

    public AdapterView.OnItemClickListener ItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the data to pass on
                String title = mScheduleList.get(position).scheduleLesson;
                String icon = mScheduleList.get(position).scheduleIcon;

                // If the used device is a tablet, replace the
                // right-hand side fragment with a ScheduleDetailFragment
                // passing the data of the clicked row to the fragment
                if (isTablet) {
                    Bundle args = new Bundle();
                    args.putString(getString(R.string.INTENT_EXTRA_CLASS), title);
                    args.putString("icon", icon);
                    args.putInt(getString(R.string.INTENT_EXTRA_POSITION), position);

                    ScheduleDetailFragment fragment = new ScheduleDetailFragment();
                    fragment.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, fragment)
                            .commit();
                }

                // If the used device is a phone, start a new ScheduleDetailActivity
                // passing the data of the clicked row to the fragment
                else {
                    Intent intent = new Intent(getActivity(), ScheduleDetailActivity.class);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title);
                    intent.putExtra("icon", icon);

                    // Add a transition if the device is Lollipop or above
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        // Shared element transition
                        View iconView = view.findViewById(R.id.schedule_icon);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                (getActivity(), iconView, iconView.getTransitionName()).toBundle();
                        startActivity(intent, bundle);
                    } else startActivity(intent);
                }
            }
        };
    }

    void getClassesFromFirebase() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String weekNumber = preferences.getString(getString(R.string.KEY_WEEK_NUMBER), "0");
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("classes");
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long snapshotCount = dataSnapshot.getChildrenCount();
                if (snapshotCount > 0)
                    headerTextView.setVisibility(View.GONE);
                for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                    String title = classSnapshot.getKey();
                    String icon = classSnapshot.child("icon").getValue(String.class);
                    String teacher = classSnapshot.child("teacher").getValue(String.class);
                    String room = classSnapshot.child("room").getValue(String.class);
                    String timein;
                    String timeout;


                    mScheduleList.add(new Schedule(getContext(), icon, title, teacher, room, "", "", ""));
                    mScheduleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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
            if (checked) {
                CAMselectedItemsList.add(position);
            }

            // If the clicked item became deselected, get its item id
            // and remove it from the array list
            else {
                int itemId = -1;
                // Scan through the array list until the
                // item's value matches its position
                // When it does, set the itemId to the matched position
                // and then remove the item in that array list
                // matching that position
                ArrayList<Integer> indexes = new ArrayList<>();
                for (int i = CAMselectedItemsList.size() - 1; i > -1; i--)
                    indexes.add(CAMselectedItemsList.get(i));

                Collections.sort(indexes);
                for (int i = indexes.size() - 1; i > -1; i--)
                    if (position == CAMselectedItemsList.get(i)) {
                        itemId = i;
                    }

                if (itemId != -1)
                    CAMselectedItemsList.remove(itemId);
            }

            // Invalidating the Action Mode calls onPrepareActionMode
            // which will show or hide the edit menu action based on
            // the number of items selected
            mode.invalidate();
            Log.d(LOG_TAG, "CAM Count: " + CAMselectedItemsList.size());
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
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.gray_900));

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
                    try {
                        deleteSelectedItems();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                getActivity().getWindow().setStatusBarColor(mDarkColor);
        }

        private void deleteSelectedItems() throws IOException {
            if (mFirebaseUser != null) {
                // Delete the data from Firebase
                classesRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("classes");

                ArrayList<Integer> indexes = new ArrayList<>();
                for (int i = CAMselectedItemsList.size() - 1; i > -1; i--)
                    indexes.add(CAMselectedItemsList.get(i));

                Collections.sort(indexes);
                for (int i = indexes.size() - 1; i > -1; i--) {
                    classesRef.child(mScheduleList.get(indexes.get(i)).scheduleLesson).removeValue();
                    mScheduleList.remove((int) indexes.get(i));
                }

                // Set the splash text if there's no classes queried
                if (mScheduleList.size() == 0)
                    headerTextView.setVisibility(View.VISIBLE);
                else headerTextView.setVisibility(View.GONE);
                mScheduleAdapter.notifyDataSetChanged();

            } else {
                // Delete the data from SQLite
                DbHelper db = new DbHelper(getActivity());
                Cursor cursor = db.getAllClassesData();

                // Delete all the selected items based on the itemIDs
                // Stored in the array list
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    if (cursor.moveToPosition(CAMselectedItemsList.get(i))) {
                        db.deleteScheduleItem(cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry._ID)));
                    }
                }

                cursor.close();
            }

            querySchedule();
            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem() {
            // Ensure that only one item is selected
            if (CAMselectedItemsList.size() == 1) {
                // Initialise Id and Title variables
                String title = "";

                Intent intent = new Intent(getActivity(), NewScheduleActivity.class);

                if (mFirebaseUser != null) {
                    // Get the data from Firebase
                    title = mScheduleList.get(CAMselectedItemsList.get(0)).scheduleLesson;
                } else {
                    // Get the data from SQLite
                    // Get a cursor of the current day schedule data
                    DbHelper db = new DbHelper(getActivity());
                    Cursor cursor = db.getAllClassesData();

                    // Move the cursor to the position of the selected item
                    if (cursor.moveToPosition(CAMselectedItemsList.get(0))) {
                        // Get its Id and Title
                        title = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
                        cursor.close();
                    }
                }


                // Create an intent to NewScheduleActivity and include the selected
                // item's id, title, and an edit flag as extras
                intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE), title);
                intent.putExtra(getResources().getString(R.string.INTENT_FLAG_EDIT), true);

                // Clear the selected items list, exit the CAM and launch the activity
                CAMselectedItemsList.clear();
                getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                startActivity(intent);
            }

            // If more than one item was selected, throw a warning log
            else {
                Log.w(LOG_TAG, "Cancelling event due to more than one item selected");
            }
        }
    }

}
