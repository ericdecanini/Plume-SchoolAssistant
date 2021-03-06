package com.pdt.plume;


import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static android.media.CamcorderProfile.get;
import static android.support.v7.graphics.Palette.generate;

public class ScheduleFragment extends Fragment {

    // Constantly used variables
    String LOG_TAG = ScheduleFragment.class.getSimpleName();
    Utility utility = new Utility();

    // CAM Variables
    private Menu mActionMenu;
    private int mOptionMenuCount;

    // UI Elements
    ListView listView;
    View splash;
    TextView headerTextView;
    FloatingActionButton fab;
    ProgressBar spinner;

    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // UI Data
    ScheduleAdapter mScheduleAdapter;
    ArrayList<Schedule> mScheduleList = new ArrayList<>();
    ArrayList<Long> timeInList = new ArrayList<>();
    ArrayList<String> mOccurrenceList = new ArrayList<>();
    ArrayList<String> mOccurrenceIndexes = new ArrayList<>();

    // Flags
    boolean isTablet;
    public static boolean noItems = false;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    DatabaseReference classesRef;
    ChildEventListener childEventListener = null;

    // Date Variables
    int year = -1;
    int month = -1;
    int day = -1;

    // Required empty public constructor
    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        // If it's the first time running the app, launch this method
        boolean firstLaunch = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true);
        if (firstLaunch) {
            init();
            return rootView;
        }

        // Initialise the date
        Calendar c = Calendar.getInstance();
        Bundle args = getArguments();
        if (args != null && args.containsKey("year")) {
            year = args.getInt("year");
            month = args.getInt("month");
            day = args.getInt("day");
        } else {
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day  = c.get(Calendar.DAY_OF_MONTH);

        }

        // Get references to the views
        splash = rootView.findViewById(R.id.splash);
        headerTextView = (TextView) rootView.findViewById(R.id.textView1);
        listView = (ListView) rootView.findViewById(R.id.schedule_list);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        // Check if the used device is a tablet
        isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            fab.setAlpha(1f);
        }

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Apply the list data to the listview mTasksAdapter
        mScheduleAdapter = new ScheduleAdapter(getContext(),
                R.layout.list_item_schedule, mScheduleList);

        // Set the mTasksAdapter and listeners of the list view
        if (listView != null) {
            listView.setAdapter(mScheduleAdapter);
            listView.setOnItemClickListener(ItemClickListener());
            if (!isTablet) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                listView.setMultiChoiceModeListener(new ModeCallback());
            } else listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            if (isTablet && mScheduleList.size() == 0)
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new BlankFragment())
                        .commit();

            if (getArguments() != null) {
                int position = getArguments().getInt(getString(R.string.INTENT_EXTRA_POSITION), 0);
                String RETURN_TO_SCHEDULE = getArguments().getString(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE));
                if (isTablet && RETURN_TO_SCHEDULE != null && position > -1 && mScheduleList.size() > 0) {
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

        if (isTablet)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, new BlankFragment())
                    .commit();

        int textColor = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), getResources().getColor(R.color.gray_900));
        ((TextView) rootView.findViewById(R.id.textView1)).setTextColor(textColor);

        // Inflate the layout for this fragment
        return rootView;
    }

    void querySchedule() {
        mScheduleList.clear();
        if (mFirebaseUser != null) {
            getCurrentDayScheduleFromFirebase();

            // Check if the classes ref doesn't exist
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("classes").getChildrenCount() == 0) {
                        spinner.setVisibility(View.GONE);
                        splash.setVisibility(View.VISIBLE);
                        noItems = true;
                    }

                    if (mScheduleList.size() == 0) {
                        splash.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        noItems = true;
                    } else {
                        splash.setVisibility(View.GONE);
                        noItems = false;
                        if (isTablet && mScheduleList.size() > 0)
                            listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
                    }

                    mScheduleAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        } else {
            // Get the schedule data from SQLite
            DbHelper dbHelper = new DbHelper(getContext());
            try {
                ArrayList<Schedule> newSchedules = dbHelper.getCurrentDayScheduleArray(getContext(), year, month, day);
                mScheduleList.clear();
                mScheduleList.addAll(newSchedules);
                if (spinner != null)
                    spinner.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set the splash text if there's no classes queried and update the Adapter
        if (splash != null) {
            if (mScheduleList.size() == 0) {
                splash.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                noItems = true;
            } else {
                splash.setVisibility(View.GONE);
                noItems = false;
            }
        }

        if (mScheduleAdapter != null) {
            mScheduleAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (childEventListener != null)
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("classes")
                    .removeEventListener(childEventListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        mScheduleAdapter = new ScheduleAdapter(getContext(),
                R.layout.list_item_schedule, mScheduleList);
        querySchedule();

        // Click the first list item
        if (isTablet && mScheduleList.size() > 0)
            listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), R.color.colorPrimary);
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent);
        if (fab != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
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
                    listView.setEnabled(false);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listView.setEnabled(true);
                        }
                    }, 1000);
                    Intent intent = new Intent(getActivity(), ScheduleDetailActivity.class);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title);
                    intent.putExtra("icon", icon);

                    // Add a transition if the device is Lollipop or above
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && view != null) {
                        // First check if a shared element transition is appropriate
                        // Shared element transition
                        ImageView iconView = (ImageView) view.findViewById(R.id.schedule_icon);
                        Uri iconUri = Uri.parse(mScheduleList.get(position).scheduleIcon);
                        boolean transition = iconUri.toString().contains("art_");

                        if (transition) {
                            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                    (getActivity(), iconView, iconView.getTransitionName()).toBundle();
                            startActivity(intent, bundle);
                        } else startActivity(intent);
                    } else startActivity(intent);
                }
            }
        };
    }

    // This method is called when the app has been launched for the first time
    private void init() {
        if (splash != null) {
            headerTextView.setText(getString(R.string.activity_classes_splash_no_classes));
            splash.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
            noItems = true;
        }
    }

    private void getCurrentDayScheduleFromFirebase() {
        // Get the calendar data for the week number
        mScheduleList.clear();
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        final String weekNumber = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(getString(R.string.KEY_WEEK_NUMBER), "0");
        final int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            // Get data from Firebase
            spinner.setVisibility(View.VISIBLE);
            String userId = firebaseUser.getUid();
            classesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("classes");

            // Create the event listener to download the data from online storage
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot classSnapshot, String s) {
                    // Temporarily set 12 hours to false to correct sorting
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                    final String title = classSnapshot.getKey();
                    final String teacher = classSnapshot.child("teacher").getValue(String.class);
                    final String room = classSnapshot.child("room").getValue(String.class);
                    final String iconUri = classSnapshot.child("icon").getValue(String.class);

                    // Array data, get and add to array lists
                    final ArrayList<String> occurrences = new ArrayList<>();
                    final ArrayList<String> occurrenceIndexes = new ArrayList<>();
                    for (DataSnapshot occurrenceSnapshot : classSnapshot.child("occurrence").getChildren()) {
                        occurrences.add(occurrenceSnapshot.getValue(String.class));
                        occurrenceIndexes.add(occurrenceSnapshot.getKey());
                    }

                    final ArrayList<Integer> timeins = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timein").getChildren())
                        timeins.add(timeinSnapshot.getValue(int.class));
                    final ArrayList<Integer> timeouts = new ArrayList<>();
                    for (DataSnapshot timeoutSnapshot : classSnapshot.child("timeout").getChildren())
                        timeouts.add(timeoutSnapshot.getValue(int.class));
                    final ArrayList<Integer> timeinalts = new ArrayList<>();
                    for (DataSnapshot timeinaltSnapshot : classSnapshot.child("timeinalt").getChildren())
                        timeinalts.add(timeinaltSnapshot.getValue(int.class));
                    final ArrayList<Integer> timeoutalts = new ArrayList<>();
                    for (DataSnapshot timeoutaltSnapshot : classSnapshot.child("timeoutalt").getChildren())
                        timeoutalts.add(timeoutaltSnapshot.getValue(int.class));
                    final ArrayList<String> periods = new ArrayList<>();
                    for (DataSnapshot periodsSnapshot : classSnapshot.child("periods").getChildren())
                        periods.add(periodsSnapshot.getValue(String.class));

                    // For each of the subject's occurrences, check if that matches the current day
                    // If it does, add it to the lists of occurrences to be added by the listview adapter
                    for (int occurrenceIndex = 0; occurrenceIndex < occurrences.size(); occurrenceIndex++) {
                        // Check if the iconUri points to an existing file
                        if (iconUri == null) return;

                        if (utility.occurrenceMatchesCurrentDay(getContext(), occurrences.get(occurrenceIndex),
                                periods.get(periods.size() - 1), weekNumber, dayOfWeek)) {
                            mOccurrenceList.add(occurrences.get(occurrenceIndex));
                            mOccurrenceIndexes.add(occurrenceIndexes.get(occurrenceIndex));

                            // If it is a period based item, check if it has more than one period per day
                            ArrayList<String> periodsList = new ArrayList<>();
                            if (periods.size() < occurrenceIndex)
                                periodsList.addAll(utility.createSetPeriodsArrayList(periods.get(occurrenceIndex), weekNumber,
                                        occurrences.get(occurrenceIndex).split(":")[1]));

                            File file = new File(getContext().getFilesDir(), title + ".jpg");
                            if (file.exists() || iconUri.contains("art_")) {
                                if (periodsList.size() != 0) {
                                    for (int i = 0; i < periodsList.size(); i++) {
                                        if (weekNumber.equals("0") || occurrences.get(i).split(":")[1].equals("0")) {
                                            mScheduleList.add(new Schedule(getContext(), iconUri, title, teacher, room,
                                                    utility.millisToHourTime(getContext(), timeins.get(i)),
                                                    utility.millisToHourTime(getContext(), timeouts.get(i)),
                                                    periodsList.get(i)));
                                        }
                                        else {
                                            mScheduleList.add(new Schedule(getContext(), iconUri, title, teacher, room,
                                                    utility.millisToHourTime(getContext(), timeinalts.get(i)),
                                                    utility.millisToHourTime(getContext(), timeoutalts.get(i)),
                                                    periodsList.get(i)));
                                        }

                                        Collections.sort(mScheduleList, new ScheduleComparator());
                                        mScheduleAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    if (weekNumber.equals("0") || occurrences.get(occurrenceIndex).split(":")[1].equals("0")) {
                                        mScheduleList.add(new Schedule(getContext(), iconUri, title, teacher, room,
                                                utility.millisToHourTime(getContext(), timeins.get(occurrenceIndex)),
                                                utility.millisToHourTime(getContext(), timeouts.get(occurrenceIndex)),
                                                ""));
                                    }
                                    else {
                                        mScheduleList.add(new Schedule(getContext(), iconUri, title, teacher, room,
                                                utility.millisToHourTime(getContext(), timeinalts.get(occurrenceIndex)),
                                                utility.millisToHourTime(getContext(), timeoutalts.get(occurrenceIndex)),
                                                ""));
                                    }
                                    Collections.sort(mScheduleList, new ScheduleComparator());
                                    mScheduleAdapter.notifyDataSetChanged();
                                }
                            } else {
                                // File is non existent, download from storage
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();
                                StorageReference iconsRef = storageRef.child(mUserId + "/classes/" + title);

                                iconsRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        for (int i = 0; i < occurrences.size(); i++) {
                                            if (weekNumber.equals("0") || occurrences.get(i).split(":")[1].equals("0"))
                                                mScheduleList.add(new Schedule(getContext(), iconUri, title, teacher, room,
                                                        utility.millisToHourTime(getContext(), timeins.get(i)),
                                                        utility.millisToHourTime(getContext(), timeouts.get(i)),
                                                        periods.get(i)));
                                            else
                                                mScheduleList.add(new Schedule(getContext(), iconUri, title, teacher, room,
                                                        utility.millisToHourTime(getContext(), timeinalts.get(i)),
                                                        utility.millisToHourTime(getContext(), timeoutalts.get(i)),
                                                        periods.get(i)));
                                            Collections.sort(mScheduleList, new ScheduleComparator());
                                            mScheduleAdapter.notifyDataSetChanged();
                                        }
                                        querySchedule();
                                    }
                                });
                            }
                        }
                    }

                    if (mScheduleList.size() == 0) {
                        splash.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        noItems = true;
                    } else {
                        splash.setVisibility(View.GONE);
                        spinner.setVisibility(View.GONE);
                        noItems = false;
                    }

                    mScheduleAdapter.notifyDataSetChanged();
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
                    splash.setVisibility(View.VISIBLE);
                    noItems = true;
                    headerTextView.setText(getString(R.string.check_internet));
                }
            };

            classesRef.addChildEventListener(childEventListener);
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
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            // Inflate the action menu and set the global menu variable
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_single, menu);
            mActionMenu = menu;

            // Set the category and colour of the contextual action bar
            mode.setTitle(getContext().getString(R.string.select_items));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.gray_900));

            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            // Checks the count of items selected.
            // If it is one, show the edit menu action.
            // If it is delete than one, hide the edit menu action.
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
                for (int i = 0; i < CAMselectedItemsList.size(); i++)
                    indexes.add(CAMselectedItemsList.get(i));

                Collections.sort(indexes);
                for (int i = 0; i < indexes.size(); i++) {
                    // Delete the selected occurrence of the subject
                    final DatabaseReference classRef =  classesRef.child(mScheduleList.get(indexes.get(i)).scheduleLesson);
                    classesRef.child(mScheduleList.get(indexes.get(i)).scheduleLesson).child("occurrence")
                            .child(mOccurrenceIndexes.get(indexes.get(i))).removeValue();

                    // If the subject has no more occurrences, delete the subject altogether
                    classesRef.child(mScheduleList.get(indexes.get(i)).scheduleLesson)
                            .child("occurrence").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() == 0)
                                classRef.removeValue();
                        }

                        @Override public void onCancelled(DatabaseError databaseError) {}});

                    // Remove the class from the array list and adapter
                    mScheduleList.remove((int) indexes.get(i));
                }

                // Set the splash text if there's no classes queried
                if (mScheduleList.size() == 0) {
                    splash.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                    noItems = true;
                } else {
                    splash.setVisibility(View.GONE);
                    noItems = false;
                }
                mScheduleAdapter.notifyDataSetChanged();

            } else {
                // Delete the data from SQLite
                DbHelper db = new DbHelper(getActivity());
                Cursor cursor = db.getCurrentDayScheduleDataFromSQLite(getActivity(), year, month, day);

                // Delete all the selected items based on the itemIDs
                // Stored in the array list
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    if (cursor.moveToPosition(CAMselectedItemsList.get(i))) {
                        db.deleteScheduleItem(cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID)));
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
                    Cursor cursor = db.getCurrentDayScheduleDataFromSQLite(getActivity(), year, month, day);

                    // Move the cursor to the position of the selected item
                    if (cursor.moveToPosition(CAMselectedItemsList.get(0))) {
                        // Get its Id and Title
                        title = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TITLE));
                        cursor.close();
                    }
                }


                // Create an intent to NewScheduleActivity and include the selected
                // item's id, category, and an edit flag as extras
                intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE), title);
                intent.putExtra(getResources().getString(R.string.INTENT_FLAG_EDIT), true);

                // Clear the selected items list, exit the CAM and launch the activity
                CAMselectedItemsList.clear();
                getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                startActivity(intent);
            }

            // If delete than one item was selected, throw a warning log
            else {
                Log.w(LOG_TAG, "Cancelling event due to delete than one item selected");
            }
        }
    }
}


