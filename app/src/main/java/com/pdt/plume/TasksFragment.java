package com.pdt.plume;


import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
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
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TasksFragment extends Fragment {
    // Constantly used variables
    String LOG_TAG = TasksFragment.class.getSimpleName();
    Utility utility = new Utility();
    DbHelper dbHelper;

    // UI Elements
    ListView listView;
    private Menu mActionMenu;
    private int mOptionsMenuCount;
    FloatingActionButton fab;
    View splash;
    TextView headerTextview;
    ProgressBar spinner;

    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // Flags
    boolean isTablet = false;
    boolean isLandscape;
    public static boolean noItems = false;

    // List Variables
    ArrayList<Task> mTasksList;
    TaskAdapter mTasksAdapter;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    ArrayList<String> FirebaseIdList = new ArrayList<>();
    DatabaseReference tasksRef;
    ChildEventListener childEventListener = null;

    // Required empty public constructor
    public TasksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
        isLandscape = getResources().getBoolean(R.bool.isLandscape);

        // Initialise Firebase and SQLite
        mTasksList = new ArrayList<>();
        dbHelper = new DbHelper(getActivity());
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();


        // Get references to the views
        splash = rootView.findViewById(R.id.splash);
        headerTextview = (TextView) rootView.findViewById(R.id.textView1);
        listView = (ListView) rootView.findViewById(R.id.tasks_list);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        // Check if the used device is a tablet
        isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) fab.setAlpha(1f);

        mTasksAdapter = new TaskAdapter(getContext(), R.layout.list_item_task, mTasksList);

        // Set the mTasksAdapter and listeners of the listview
        if (listView != null) {
            listView.setAdapter(mTasksAdapter);
            listView.setOnItemClickListener(ItemClickListener());
            if (!isTablet) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                listView.setMultiChoiceModeListener(new ModeCallback());
            } else listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

            if (isTablet && mTasksList.size() > 0)
                listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());

            if (isTablet && mTasksList.size() == 0)
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new BlankFragment())
                        .commit();

        }

        // Set the action of the FAB
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewTaskActivity.class);
                startActivity(intent);
            }
        });

        if (isTablet)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, new BlankFragment())
                    .commit();

        int textColor = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(getString(R.string.KEY_THEME_TITLE_COLOUR), getResources().getColor(R.color.gray_900));
        ((TextView) rootView.findViewById(R.id.textView1)).setTextColor(textColor);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

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

        queryTasks();
        mTasksAdapter.notifyDataSetChanged();
    }

    private void queryTasks() {
        mTasksList.clear();
        FirebaseIdList.clear();
        // Get a reference to the list view and create its mTasksAdapter
        // using the current day schedule data
        if (mFirebaseUser != null) {
            // Get the data from Firebase
            spinner.setVisibility(View.VISIBLE);
            tasksRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("tasks");
            // Check if the reference exists
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                    final String icon = dataSnapshot.child("icon").getValue(String.class);
                    final String title = dataSnapshot.child("title").getValue(String.class);
                    final String sharer = dataSnapshot.child("sharer").getValue(String.class);
                    final String taskClass = dataSnapshot.child("class").getValue(String.class);
                    final String tasktType = dataSnapshot.child("type").getValue(String.class);
                    final String description = dataSnapshot.child("description").getValue(String.class);
                    final Float duedate = dataSnapshot.child("duedate").getValue(Float.class);
                    Boolean completed = dataSnapshot.child("completed").getValue(Boolean.class);
                    if (completed == null)
                        completed = false;
                    final Bitmap[] bitmap = {null};

                    // Debug function
                    if (title == null)
                        return;

                    // Check if icon URI is valid
                    final File file = new File(getContext().getFilesDir(), dataSnapshot.getKey());
                    if (file.exists() || icon.contains("art_")) {
                        if (!completed && duedate != null) {
                            Log.v(LOG_TAG, "(OnFileExists) Task " + title + " added with icon " + icon);
                            mTasksList.add(new Task(icon, title, sharer, taskClass, tasktType, description, "", duedate, -1f, bitmap[0]));
                            FirebaseIdList.add(dataSnapshot.getKey());
                            mTasksAdapter.notifyDataSetChanged();
                            spinner.setVisibility(View.GONE);

                            // The header text view will only be visible if there is no items in the task mClassAdapter
                            if (mTasksAdapter.getCount() == 0) {
                                splash.setVisibility(View.VISIBLE);
                                noItems = true;
                            } else {
                                splash.setVisibility(View.GONE);
                                noItems = false;
                                if (isTablet && mTasksList.size() > 0)
                                    listView.performItemClick(listView.getChildAt(0), 0, listView.getFirstVisiblePosition());
                            }

                            int selectedPosition = listView.getSelectedItemPosition();
                            if (isTablet && mTasksList.size() > 0 && selectedPosition == -1)
                                listView.performItemClick(mTasksAdapter.getView(0, null, null), 0, mTasksAdapter.getItemId(0));
                        }
                    } else {
                        // File is non existent, download from storage
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference iconsRef = storageRef.child(mUserId)
                                .child("tasks").child(dataSnapshot.getKey());
                        Log.v(LOG_TAG, "IconsRef: " + iconsRef.getPath());

                        final Boolean finalCompleted = completed;
                        long ONE_MEGABYTE = 1024 * 1024;
                        Log.v(LOG_TAG, "Attempting to download icon for " + title);
                        iconsRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Log.v(LOG_TAG, "Download successful " + title);
                                if (!finalCompleted && duedate != null) {
                                    saveInternalFile(bytes, file.getName());
                                    String iconUri = file.getPath();
                                    FirebaseDatabase.getInstance().getReference().child("users").child(mUserId).child("tasks")
                                            .child(dataSnapshot.getKey()).child("icon").setValue(iconUri);
                                    mTasksList.add(new Task(iconUri, title, sharer, taskClass, tasktType, description, "", duedate, -1f, bitmap[0]));
                                    FirebaseIdList.add(dataSnapshot.getKey());
                                    mTasksAdapter.notifyDataSetChanged();
                                    spinner.setVisibility(View.GONE);

                                    // The header text view will only be visible if there is no items in the task mClassAdapter
                                    if (mTasksAdapter.getCount() == 0) {
                                        splash.setVisibility(View.VISIBLE);
                                        noItems = true;
                                    } else {
                                        splash.setVisibility(View.GONE);
                                        noItems = false;
                                    }

                                    int selectedPosition = listView.getSelectedItemPosition();
                                    if (isTablet && mTasksList.size() > 0 && selectedPosition == -1)
                                        listView.performItemClick(mTasksAdapter.getView(0, null, null), 0, mTasksAdapter.getItemId(0));
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.v(LOG_TAG, "Download unsuccessful " + title);
                            }
                        });
                    }
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
                    headerTextview.setText(getString(R.string.check_internet));
                }
            };
            tasksRef.addChildEventListener(childEventListener);

            // Check if the tasks ref doesn't exist
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("tasks").getChildrenCount() == 0) {
                        spinner.setVisibility(View.GONE);
                        splash.setVisibility(View.VISIBLE);
                        noItems = true;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            // Get the data from SQLite
            mTasksList.addAll(dbHelper.getUncompletedTaskArray());
            spinner.setVisibility(View.GONE);
        }

        mTasksAdapter.notifyDataSetChanged();

        // The header text view will only be visible if there is no items in the task mClassAdapter
        if (mTasksList.size() == 0) {
            splash.setVisibility(View.VISIBLE);
            noItems = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int selectedPosition = listView.getSelectedItemPosition();
        if (isTablet && mTasksList.size() > 0 && selectedPosition == -1)
            listView.performItemClick(mTasksAdapter.getView(0, null, null), 0, mTasksAdapter.getItemId(0));
    }

    private void saveInternalFile(byte[] bytes, String filepath) {
        FileOutputStream fos = null;
        try {
            fos = getContext().openFileOutput(filepath, getContext().MODE_PRIVATE);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (tasksRef != null)
            tasksRef.removeEventListener(childEventListener);
    }

    public AdapterView.OnItemClickListener ItemClickListener() {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                // If the used device is a tablet, replace the
                // right-hand side fragment with a TasksDetailFragment
                // passing the data of the clicked row to the fragment
                if (isTablet) {
                    TasksDetailFragment fragment = new TasksDetailFragment();
                    Bundle args = new Bundle();
                    if (mFirebaseUser != null) {
                        args.putString("id", FirebaseIdList.get(position));
                    } else {
                        args.putInt(getString(R.string.INTENT_EXTRA_ID), position);
                    }
                    args.putString("icon", mTasksList.get(position).taskIcon);
                    args.putInt(getString(R.string.INTENT_EXTRA_POSITION), position);
                    fragment.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_container, fragment)
                            .commit();
                }

                // If the used device is a phone, start a new TasksDetailActivity
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
                    final Intent intent = new Intent(getActivity(), TasksDetailActivity.class);
                    if (mFirebaseUser != null) {
                        intent.putExtra("id", FirebaseIdList.get(position));
//                        tasksRef.removeEventListener(tasksListener);
                    } else {
                        intent.putExtra(getString(R.string.INTENT_EXTRA_ID), position);
                    }
                    intent.putExtra("icon", mTasksList.get(position).taskIcon);
                    View icon;
                    if (view != null) {
                        icon = view.findViewById(R.id.task_icon2);
                        if (icon.getTag() == null) icon = view.findViewById(R.id.task_icon);


                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
                                && ((String) icon.getTag()).contains("com.pdt.plume") && !isLandscape) {
                            // First check if a shared element transition is appropriate
                            // Shared element transition
                            Uri iconUri = Uri.parse(mTasksList.get(position).taskIcon);
                            boolean transition = false;

                            if (iconUri.toString().contains("art_"))
                                transition = true;
                            else {
                                transition = false;
                            }

                            if (transition) {
                                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation
                                        (getActivity(), icon, icon.getTransitionName()).toBundle();
                                startActivity(intent, bundle);
                            } else startActivity(intent);
                        } else startActivity(intent);
                    }
                }
            }
        };
    }

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
                    mOptionsMenuCount = 0;
                    mode.setSubtitle("One item selected");
                    break;

                default:
                    mOptionsMenuCount = 1;
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

            // Set the title and colour of the contextual action bar
            mode.setTitle("Select Items");

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
            if (mOptionsMenuCount == 0)
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
                    return true;

                case R.id.action_edit:
                    editSelectedItem(CAMselectedItemsList.get(0));
                    return true;
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

        private void deleteSelectedItems() {
            if (mFirebaseUser != null) {
                // Delete data from Firebase
                final DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("tasks");

                final ArrayList<Integer> indexes = new ArrayList<>();
                for (int i = CAMselectedItemsList.size() - 1; i > -1; i--)
                    indexes.add(CAMselectedItemsList.get(i));

                Collections.sort(indexes);
                for (int i = indexes.size() - 1; i > -1; i--) {
                    final String firebaseId = FirebaseIdList.get(indexes.get(i));
                    // Delete stored icon and photos if applicable
                    final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference iconRef = storageRef.child(mUserId).child("tasks").child(firebaseId).child("icon");
                    iconRef.delete();
                    tasksRef.child(FirebaseIdList.get(indexes.get(i))).removeValue();
                    FirebaseIdList.remove(((int) indexes.get(i)));
                    mTasksList.remove(((int) indexes.get(i)));
                }

                // Refresh the list mTasksAdapter
                mTasksAdapter.notifyDataSetChanged();
                if (mTasksAdapter.getCount() == 0) {
                    splash.setVisibility(View.VISIBLE);
                    noItems = true;
                } else {
                    splash.setVisibility(View.GONE);
                    noItems = false;
                }
            } else {
                // Delete data from SQLite
                DbHelper db = new DbHelper(getActivity());
                Cursor cursor = db.getUncompletedTaskData();

                // Delete all the selected items based on the itemIDs
                // Stored in the array list
                for (int i = 0; i < CAMselectedItemsList.size(); i++) {
                    if (cursor.moveToPosition(CAMselectedItemsList.get(i))) {
                        db.deleteTaskItem(cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID)));
                    }
                }
                cursor.close();

                // Query all the tasks data again from SQLite
                mTasksList.clear();
                mTasksList.addAll(db.getTaskDataArray());

                // Refresh the mTasksAdapter
                mTasksAdapter.notifyDataSetChanged();
                if (mTasksList.size() == 0) {
                    splash.setVisibility(View.VISIBLE);
                    noItems = true;
                } else {
                    splash.setVisibility(View.GONE);
                    noItems = false;
                }
            }

            // Then clear the selected items array list and emulate
            // a back button press to exit the Action Mode
            CAMselectedItemsList.clear();
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        }

        private void editSelectedItem(final int position) {
            // Ensure that only one item is selected
            if (CAMselectedItemsList.size() == 1) {
                // Initialise intent data variables
                int id;
                final String[] icon = new String[1];
                final String[] title = new String[1];
                final String[] classTitle = new String[1];
                final String[] classType = new String[1];
                final String[] description = new String[1];
                final String[] attachment = new String[1];
                final float[] dueDate = new float[1];
                final float[] reminderDate = new float[1];
                final float[] reminderTime = new float[1];
                final Intent intent = new Intent(getActivity(), NewTaskActivity.class);

                if (mFirebaseUser != null) {
                    // Get the data from Firebase
                    final DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("tasks").child(FirebaseIdList.get(CAMselectedItemsList.get(0)));
                    taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String id = dataSnapshot.getKey();
                            icon[0] = dataSnapshot.child("icon").getValue(String.class);
                            title[0] = dataSnapshot.child("title").getValue(String.class);
                            classTitle[0] = dataSnapshot.child("class").getValue(String.class);
                            classType[0] = dataSnapshot.child("type").getValue(String.class);
                            description[0] = dataSnapshot.child("description").getValue(String.class);
                            attachment[0] = dataSnapshot.child("attachment").getValue(String.class);
                            dueDate[0] = dataSnapshot.child("duedate").getValue(long.class);

                            intent.putExtra("id", id);
                            intent.putExtra("icon", icon[0]);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE), title[0]);
                            intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), classTitle[0]);
                            intent.putExtra(getString(R.string.INTENT_EXTRA_TYPE), classType[0]);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DESCRIPTION), description[0]);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ATTACHMENT), attachment[0]);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DUEDATE), dueDate[0]);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ALARM_DATE), reminderDate[0]);
                            intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ALARM_TIME), reminderTime[0]);

                            // Create an intent to NewScheduleActivity and include the selected
                            // item's id, title, and an edit flag as extras
                            intent.putExtra("position", position);
                            intent.putExtra(getResources().getString(R.string.INTENT_FLAG_EDIT), true);

                            // Clear the selected items list, exit the CAM and launch the activity
                            CAMselectedItemsList.clear();
                            taskRef.removeEventListener(this);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    while (title.equals("")) {
                        // Sleep until intent is sent by the value added listener
                    }
                } else {
                    // Get the data from SQLite
                    // Get a reference to the database and
                    // Get a cursor of the Task Data
                    DbHelper db = new DbHelper(getActivity());
                    Cursor cursor = db.getUncompletedTaskData();

                    // Move the cursor to the position of the selected item
                    if (cursor.moveToPosition(CAMselectedItemsList.get(0))) {
                        // Get its Data
                        id = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID));
                        icon[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));
                        title[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                        classTitle[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS));
                        classType[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE));
                        description[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION));
                        attachment[0] = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT));
                        dueDate[0] = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE));
                        reminderDate[0] = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
                        reminderTime[0] = cursor.getFloat(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
                        cursor.close();

                        intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ID), id);
                        intent.putExtra("icon", icon[0]);
                        intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_TITLE), title[0]);
                        intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), classTitle[0]);
                        intent.putExtra(getString(R.string.INTENT_EXTRA_TYPE), classType[0]);
                        intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DESCRIPTION), description[0]);
                        intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ATTACHMENT), attachment[0]);
                        intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_DUEDATE), dueDate[0]);
                        intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ALARM_DATE), reminderDate[0]);
                        intent.putExtra(getResources().getString(R.string.INTENT_EXTRA_ALARM_TIME), reminderTime[0]);

                        // Create an intent to NewScheduleActivity and include the selected
                        // item's id, title, and an edit flag as extras
                        intent.putExtra("position", position);
                        intent.putExtra(getResources().getString(R.string.INTENT_FLAG_EDIT), true);

                        // Clear the selected items list, exit the CAM and launch the activity
                        CAMselectedItemsList.clear();
                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                        startActivity(intent);
                    }

                }
            }

            // If delete than one item was selected, throw a warning log
            else {
                Log.w(LOG_TAG, "Cancelling event due to delete than one item selected");
            }
        }

    }

}
