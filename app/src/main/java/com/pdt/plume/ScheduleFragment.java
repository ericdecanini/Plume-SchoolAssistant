package com.pdt.plume;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import com.google.firebase.auth.api.model.StringList;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static android.media.CamcorderProfile.get;
import static com.pdt.plume.R.bool.isTablet;

public class ScheduleFragment extends Fragment {
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
    public static boolean showBlockHeaderA = false;
    public static boolean showBlockHeaderB = false;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    DatabaseReference classesRef;
    ChildEventListener classesRefListener;

    // Required empty public constructor
    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Get references to the views
        headerTextView = (TextView) rootView.findViewById(R.id.header_textview);
        listView = (ListView) rootView.findViewById(R.id.schedule_list);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        // Check if the used device is a tablet
        // Currently this does nothing, but will later on be used
        // to transfer the code to a tablet layout when possible
        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Inflate the listview
        // First check if the user is logged into an account
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        if (mFirebaseUser != null) {
            // Get the schedule data from Firebase
            getCurrentDayScheduleFromFirebase();
        } else {
            // Get the schedule data from SQLite
            DbHelper dbHelper = new DbHelper(getContext());
            try {
                mScheduleList = dbHelper.getCurrentDayScheduleArray(getContext());
                spinner.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Check if the tasks ref doesn't exist
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("classes").getChildrenCount() == 0) {
                    spinner.setVisibility(View.GONE);
                    headerTextView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // Apply the list data to the listview mScheduleAdapter
        mScheduleAdapter = new ScheduleAdapter(getContext(),
                R.layout.list_item_schedule, mScheduleList);

        // Set the mScheduleAdapter and listeners of the list view
        if (listView != null) {
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(ItemClickListener());
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new ModeCallback());

            if (isTablet)
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
        }

        // Set the action for the FAB
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

    @Override
    public void onStart() {
        super.onStart();

        // Set the splash text if there's no classes queried
        if (mScheduleAdapter.getCount() == 0)
            headerTextView.setVisibility(View.VISIBLE);
        else headerTextView.setVisibility(View.GONE);

        mScheduleAdapter.notifyDataSetChanged();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), R.color.colorPrimary);
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
        fab.setBackgroundTintList((ColorStateList.valueOf(mSecondaryColor)));

        // If it's the first time running the app, launch this method
        boolean firstLaunch = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true);
        if (firstLaunch)
            init();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (classesRef != null)
            classesRef.removeEventListener(classesRefListener);
    }

    public AdapterView.OnItemClickListener ItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the data to pass on
                String title = mScheduleList.get(position).scheduleLesson;

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
                    intent.putExtra(getString(R.string.KEY_SCHEDULE_DETAIL_TITLE), title);

                    // Add a transition if the device is Lollipop or above
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        // Shared element transition
                        View icon = view.findViewById(R.id.schedule_icon);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), icon, icon.getTransitionName()).toBundle();
                        startActivity(intent, bundle);
                    } else startActivity(intent);
                }
            }
        };
    }

    // This method is called when the app has been launched for the first time
    private void init() {
        headerTextView.setText(getString(R.string.activity_classes_splash_no_classes));
        headerTextView.setVisibility(View.VISIBLE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.edit().putBoolean(getString(R.string.KEY_FIRST_LAUNCH), false).apply();
    }

    private void getCurrentDayScheduleFromFirebase() {
        // Get the calendar data for the week number
        Calendar c = Calendar.getInstance();
        final String weekNumber = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(getString(R.string.KEY_WEEK_NUMBER), "0");
        final int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Get data from Firebase
            String userId = firebaseUser.getUid();
            classesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("classes");
            classesRefListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String title = dataSnapshot.getKey();
                    String teacher = dataSnapshot.child("teacher").getValue(String.class);
                    String room = dataSnapshot.child("room").getValue(String.class);
                    String iconUri = dataSnapshot.child("icon").getValue(String.class);

                    ArrayList<String> occurrences = new ArrayList<>();
                    for (DataSnapshot occurrenceSnapshot: dataSnapshot.child("occurrence").getChildren())
                        occurrences.add(occurrenceSnapshot.getKey());
                    ArrayList<Integer> timeins = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot: dataSnapshot.child("timein").getChildren())
                        timeins.add(timeinSnapshot.getValue(int.class));
                    ArrayList<Integer> timeouts = new ArrayList<>();
                    for (DataSnapshot timeoutSnapshot: dataSnapshot.child("timeout").getChildren())
                        timeouts.add(timeoutSnapshot.getValue(int.class));
                    ArrayList<Integer> timeinalts = new ArrayList<>();
                    for (DataSnapshot timeinaltSnapshot: dataSnapshot.child("timeinalt").getChildren())
                        timeinalts.add(timeinaltSnapshot.getValue(int.class));
                    ArrayList<Integer> timeoutalts = new ArrayList<>();
                    for (DataSnapshot timeoutaltSnapshot: dataSnapshot.child("timeoutalt").getChildren())
                        timeoutalts.add(timeoutaltSnapshot.getValue(int.class));
                    ArrayList<String> periods = new ArrayList<>();
                    for (DataSnapshot periodsSnapshot: dataSnapshot.child("periods").getChildren())
                        periods.add(periodsSnapshot.getKey());

                    for (int i = 0; i < occurrences.size(); i++) {
                        if (utility.occurrenceMatchesCurrentDay(getContext(), occurrences.get(i), periods.get(i), weekNumber, dayOfWeek)) {
                            // Check if occurrence matches, then proceed if true
                            if (weekNumber.equals("0")) {
                                if (!periods.equals("-1")) {
                                    ArrayList<String> periodList = utility.createSetPeriodsArrayList(periods.get(i), weekNumber);
                                    for (int l = 0; l < periodList.size(); l++)
                                        mScheduleList.add(new Schedule(getContext(), iconUri, title, teacher,
                                                room, utility.millisToHourTime(timeins.get(i)),
                                                utility.millisToHourTime(timeouts.get(i)), periodList.get(l)));
                                }
                            } else {
                                // Alternate week data
                                if (!periods.equals("-1")) {
                                    ArrayList<String> periodList = utility.createSetPeriodsArrayList(periods.get(i), weekNumber);
                                    for (int l = 0; l < periodList.size(); l++)
                                        mScheduleList.add(new Schedule(getContext(), iconUri, title, teacher,
                                                room, utility.millisToHourTime(timeinalts.get(i)),
                                                utility.millisToHourTime(timeoutalts.get(i)), periodList.get(l)));
                                }
                            }
                        }
                    }

                    Collections.sort(mScheduleList, new ScheduleComparator());
                    mScheduleAdapter.notifyDataSetChanged();
                    spinner.setVisibility(View.GONE);

                    // Set the splash text if there's no classes queried
                    if (mScheduleAdapter.getCount() == 0)
                        headerTextView.setVisibility(View.VISIBLE);
                    else headerTextView.setVisibility(View.GONE);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    spinner.setVisibility(View.GONE);
                    headerTextView.setVisibility(View.VISIBLE);
                    headerTextView.setText(getString(R.string.check_internet));
                }
            };

            if (classesRef != null)
                classesRef.addChildEventListener(classesRefListener);
        }
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
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    mScheduleList.remove((int)CAMselectedItemsList.get(i));
                    classesRef.child(mScheduleList.get(CAMselectedItemsList.get(i)).scheduleLesson).removeValue();
                }
                mScheduleAdapter.notifyDataSetChanged();

                // Set the splash text if there's no classes queried
                if (mScheduleAdapter.getCount() == 0)
                    headerTextView.setVisibility(View.VISIBLE);
                else headerTextView.setVisibility(View.GONE);
            } else {
                // Delete the data from SQLite
                DbHelper db = new DbHelper(getActivity());
                Cursor cursor = db.getCurrentDayScheduleDataFromSQLite(getActivity());

                // Delete all the selected items based on the itemIDs
                // Stored in the array list
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    if (cursor.moveToPosition(CAMselectedItemsList.get(i))) {
                        db.deleteScheduleItem(cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID)));
                    }
                }

                cursor.close();

                // Requery the current day schedule
                mScheduleAdapter.addAll(db.getCurrentDayScheduleArray(getContext()));
                mScheduleAdapter.notifyDataSetChanged();

                // Set the splash text if there's no classes queried
                if (mScheduleAdapter.getCount() == 0)
                    headerTextView.setVisibility(View.VISIBLE);
                else headerTextView.setVisibility(View.GONE);
            }


            if (mScheduleAdapter.getCount() == 0) {
                headerTextView.setVisibility(View.VISIBLE);
            } else headerTextView.setVisibility(View.GONE);

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
                    Cursor cursor = db.getCurrentDayScheduleDataFromSQLite(getActivity());

                    // Move the cursor to the position of the selected item
                    if (cursor.moveToPosition(CAMselectedItemsList.get(0))) {
                        // Get its Id and Title
                        title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
                        cursor.close();
                    }
                }


                // Create an intent to NewScheduleActivity and include the selected
                // item's id, title, and an edit flag as extras
                intent.putExtra(getResources().getString(R.string.SCHEDULE_EXTRA_TITLE), title);
                intent.putExtra(getResources().getString(R.string.SCHEDULE_FLAG_EDIT), true);

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


