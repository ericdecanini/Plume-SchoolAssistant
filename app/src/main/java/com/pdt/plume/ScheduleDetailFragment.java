package com.pdt.plume;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;

import static com.pdt.plume.R.string.tasks;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleDetailFragment extends Fragment {

    // Constantly used variables
    String LOG_TAG = ScheduleDetailActivity.class.getSimpleName();
    Utility utility = new Utility();

    // UI Variables
    private Menu mActionMenu;
    private int mOptionsMenuCount;
    String title;
    String teacher;
    String room;
    ListView listView;
    ListView notesList;
    Uri iconUri;

    // Theme Variables
    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    // Child listeners
    ChildEventListener childListener1;
    ChildEventListener childListener2;

    // List Arrays
    ArrayList<Integer> taskIDs = new ArrayList<>();
    ArrayList<String> taskFirebaseIDs = new ArrayList<>();
    ArrayList<Integer> notesIDs = new ArrayList<>();
    ArrayList<Task> mTasksList = new ArrayList<>();
    ArrayList<PeriodItem> mPeriodsList = new ArrayList<>();


    ArrayAdapter<String> mNotesAdapter;
    TaskAdapter mTasksAdapter;
    PeriodAdapter mPeriodsAdapter;

    // Required empty public constructor
    public ScheduleDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            classDeleteListener = (OnClassDeleteListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onTaskActionListener");
        }
    }

    OnClassDeleteListener classDeleteListener;
    public interface OnClassDeleteListener {
        void OnClassDelete(String title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_schedule_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.schedule_detail_dialog_delete_confirm))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mFirebaseUser != null) {
                                    // Delete data from Firebase
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(mUserId).child("classes")
                                            .child(title).removeValue();
                                } else {
                                    // Delete data from SQLite
                                    DbHelper dbHelper = new DbHelper(getContext());
                                    dbHelper.deleteScheduleItemByTitle(title);
                                }

                                // Interface here
                                classDeleteListener.OnClassDelete(title);

                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(getActivity(), NewScheduleActivity.class);
                intent.putExtra(getString(R.string.INTENT_EXTRA_TITLE), title);
                intent.putExtra(getString(R.string.INTENT_FLAG_EDIT), true);
                startActivity(intent);
                return true;

            default: return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootview =  inflater.inflate(R.layout.fragment_schedule_detail, container, false);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Initialise the theme variables
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        TextView notesTextview = (TextView) rootview.findViewById(R.id.schedule_detail_notes_textview);
        notesTextview.setTextColor(mPrimaryColor);

        int backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));
        float[] hsv = new float[3];
        Color.colorToHSV(backgroundColor, hsv);
        hsv[2] *= 0.9f;
        int darkenedBackgroundColor = Color.HSVToColor(hsv);
        rootview.findViewById(R.id.container).setBackgroundColor(darkenedBackgroundColor);
        rootview.findViewById(R.id.schedule_detail_periods_layout).setBackgroundColor(backgroundColor);
        rootview.findViewById(R.id.schedule_detail_notes_layout).setBackgroundColor(backgroundColor);
        rootview.findViewById(R.id.schedule_detail_tasks_layout).setBackgroundColor(backgroundColor);

        int textColor = preferences.getInt(getString(R.string.KEY_THEME_TITLE_COLOUR), getResources().getColor(R.color.gray_900));
        Color.colorToHSV(textColor, hsv);
        hsv[2] *= 0.8f;
        int darkTextColor = Color.HSVToColor(hsv);
        ((TextView)rootview.findViewById(R.id.title)).setTextColor(textColor);
        ((TextView)rootview.findViewById(R.id.teacher)).setTextColor(darkTextColor);
        ((TextView)rootview.findViewById(R.id.room)).setTextColor(darkTextColor);
        ((TextView)rootview.findViewById(R.id.textView2)).setTextColor(darkTextColor);
        ((TextView)rootview.findViewById(R.id.periods_textview)).setTextColor(darkTextColor);
        ((TextView)rootview.findViewById(R.id.notes_textview)).setTextColor(darkTextColor);

        // Get references to the UI elements`
        final ImageView icon = (ImageView) rootview.findViewById(R.id.icon);
        final ImageView icon2 = (ImageView) rootview.findViewById(R.id.icon2);
        final TextView titleTextview = (TextView) rootview.findViewById(R.id.title);
        final TextView teacherTextview = (TextView) rootview.findViewById(R.id.teacher);
        final TextView roomTextview = (TextView) rootview.findViewById(R.id.room);
        TextView addNoteTextview = (TextView) rootview.findViewById(R.id.schedule_detail_notes_textview);
        addNoteTextview.setOnClickListener(addNoteListener());

        // Get the class's data based on the title and fill in the fields
        // Also inflate the tasks list
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(getString(R.string.INTENT_EXTRA_CLASS));
            titleTextview.setText(title);
            final int position = args.getInt(getString(R.string.INTENT_EXTRA_POSITION));

            // Initialise the tasks list
            listView = (ListView) rootview.findViewById(R.id.schedule_detail_tasks_list);
            mTasksAdapter = new TaskAdapter(getContext(), R.layout.list_item_task2, mTasksList);
            TextView addTaskTextview = (TextView) rootview.findViewById(R.id.add_task);
            addTaskTextview.setTextColor(mPrimaryColor);

            listView.setAdapter(mTasksAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getContext(), TasksDetailActivity.class);
                    if (mFirebaseUser != null)
                        intent.putExtra("id", taskFirebaseIDs.get(position));
                    else intent.putExtra("_ID", taskIDs.get(position));
                    intent.putExtra("icon", mTasksList.get(position).taskIcon);
                    startActivity(intent);
                }
            });
            addTaskTextview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), NewTaskActivity.class);
                    intent.putExtra(getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE), getString(R.string.INTENT_FLAG_RETURN_TO_SCHEDULE));
                    intent.putExtra(getString(R.string.INTENT_EXTRA_POSITION), position);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title);
                    startActivity(intent);
                }
            });

            // Initialise the periods list
            boolean isTablet = getResources().getBoolean(R.bool.isTablet);
            boolean isLandscape = getResources().getBoolean(R.bool.isLandscape);
            if (isTablet && isLandscape)
            mPeriodsAdapter = new PeriodAdapter(getContext(), R.layout.list_item_new_period2, mPeriodsList);
            else
            mPeriodsAdapter = new PeriodAdapter(getContext(), R.layout.list_item_new_period, mPeriodsList);
            ListView periodListview = (ListView) rootview.findViewById(R.id.schedule_detail_periods_list);
            periodListview.setAdapter(mPeriodsAdapter);

            if (mFirebaseUser != null) {
                // Get the key data from Firebase
                DatabaseReference classRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("classes").child(title);
                classRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        teacher = dataSnapshot.child("teacher").getValue(String.class);
                        room = dataSnapshot.child("room").getValue(String.class);
                        iconUri = Uri.parse(dataSnapshot.child("icon").getValue(String.class));

                        // Apply data to the UI
                        if (iconUri != null)
                            if (iconUri.toString().contains("android.resource://com.pdt.plume"))
                            icon.setImageURI(iconUri);
                        else icon2.setImageURI(iconUri);
                        teacherTextview.setText(teacher);
                        roomTextview.setText(room);
                        if ((teacher == null || teacher.equals("")) && (room == null || room.equals("")))
                            rootview.findViewById(R.id.schedule_detail_keys_layout).setVisibility(View.GONE);

                        // Intialise the tasks list
                        final View tasksSplash = rootview.findViewById(R.id.add_task);
                        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(mUserId).child("tasks");

                        childListener1 = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                String classTitle = dataSnapshot.child("class").getValue(String.class);
                                if (classTitle.equals(title)) {
                                    tasksSplash.setVisibility(View.GONE);
                                    String id = dataSnapshot.getKey();
                                    String icon = dataSnapshot.child("icon").getValue(String.class);
                                    String title = dataSnapshot.child("title").getValue(String.class);
                                    String sharer = dataSnapshot.child("sharer").getValue(String.class);
                                    String taskClass = dataSnapshot.child("class").getValue(String.class);
                                    String tasktType = dataSnapshot.child("type").getValue(String.class);
                                    String description = dataSnapshot.child("description").getValue(String.class);
                                    float duedate = dataSnapshot.child("duedate").getValue(float.class);

                                    taskFirebaseIDs.add(id);
                                    mTasksList.add(new Task(null, icon, title, sharer, taskClass, tasktType, description, "", duedate, -1, null));
                                    mTasksAdapter.notifyDataSetChanged();
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
                            }
                        };
                        tasksRef.addChildEventListener(childListener1);

                        // Initialise the periods list
                        final DatabaseReference classRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(mUserId).child("classes")
                                .child(title);
                        final ArrayList<String> occurrences = new ArrayList<>();
                        final ArrayList<Integer> timeins = new ArrayList<>();
                        final ArrayList<Integer> timeouts = new ArrayList<>();
                        final ArrayList<Integer> timeinalts = new ArrayList<>();
                        final ArrayList<Integer> timeoutalts = new ArrayList<>();
                        final ArrayList<String> periods = new ArrayList<>();

                        classRef.child("occurrence").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    occurrences.add(occurrenceSnapshot.getValue(String.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++) {
                                        mPeriodsList.add(new PeriodItem(getContext(),
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));
                                    }

                                mPeriodsAdapter.notifyDataSetChanged();

                                // Hide/Unhide the splash text
                                if (mPeriodsList.size() > 0) {
                                    rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.GONE);
                                } else rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        classRef.child("timein").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    timeins.add(occurrenceSnapshot.getValue(int.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++) {
                                        mPeriodsList.add(new PeriodItem(getContext(),
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));
                                    }

                                mPeriodsAdapter.notifyDataSetChanged();

                                // Hide/Unhide the splash text
                                if (mPeriodsList.size() > 0) {
                                    rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.GONE);
                                } else rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        classRef.child("timeout").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    timeouts.add(occurrenceSnapshot.getValue(int.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++) {
                                        mPeriodsList.add(new PeriodItem(getContext(),
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));
                                    }

                                mPeriodsAdapter.notifyDataSetChanged();

                                // Hide/Unhide the splash text
                                if (mPeriodsList.size() > 0) {
                                    rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.GONE);
                                } else rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        classRef.child("timeinalt").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    timeinalts.add(occurrenceSnapshot.getValue(int.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++) {
                                        mPeriodsList.add(new PeriodItem(getContext(),
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));
                                    }

                                mPeriodsAdapter.notifyDataSetChanged();

                                // Hide/Unhide the splash text
                                if (mPeriodsList.size() > 0) {
                                    rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.GONE);
                                } else rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        classRef.child("timeoutalt").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    timeoutalts.add(occurrenceSnapshot.getValue(int.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++)
                                        mPeriodsList.add(new PeriodItem(getContext(),
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));

                                mPeriodsAdapter.notifyDataSetChanged();

                                // Hide/Unhide the splash text
                                if (mPeriodsList.size() > 0) {
                                    rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.GONE);
                                } else rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        classRef.child("periods").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long snapshotCount = dataSnapshot.getChildrenCount();
                                for (DataSnapshot occurrenceSnapshot : dataSnapshot.getChildren()) {
                                    periods.add(occurrenceSnapshot.getValue(String.class));
                                }
                                if (occurrences.size() >= snapshotCount && timeins.size() >= snapshotCount
                                        && timeouts.size() >= snapshotCount && timeinalts.size() >= snapshotCount
                                        && timeoutalts.size() >= snapshotCount && periods.size() >= snapshotCount)
                                    for (int i = 0; i < snapshotCount; i++)
                                        mPeriodsList.add(new PeriodItem(getContext(),
                                                timeins.get(i), timeouts.get(i), timeinalts.get(i), timeoutalts.get(i),
                                                periods.get(i), occurrences.get(i)));

                                mPeriodsAdapter.notifyDataSetChanged();

                                // Hide/Unhide the splash text
                                if (mPeriodsList.size() > 0) {
                                    rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.GONE);
                                } else rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.VISIBLE);

                                classRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } else {
                // Get the data from SQLite
                DbHelper dbHelper = new DbHelper(getContext());
                Cursor cursor = dbHelper.getScheduleDataByTitle(title);

                if (cursor.moveToFirst()) {
                    teacher = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TEACHER));
                    room = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ROOM));
                    String iconUriString = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
                    iconUri = Uri.parse(iconUriString);

                    // Apply data to the UI
                    if (iconUri != null)
                        if (iconUri.toString().contains("android.resource://com.pdt.plume"))
                            icon.setImageURI(iconUri);
                        else icon2.setImageURI(iconUri);
                    titleTextview.setText(title);
                    teacherTextview.setText(teacher);
                    roomTextview.setText(room);

                    if ((teacher == null || teacher.equals("")) && (room == null || room.equals("")))
                        rootview.findViewById(R.id.schedule_detail_keys_layout).setVisibility(View.GONE);

                    // Initialise the Tasks List
                    Cursor tasksCursor = dbHelper.getTaskDataByClass(title);

                    if (tasksCursor.moveToFirst()) {
                        for (int i = 0; i < tasksCursor.getCount(); i++) {
                            mTasksList.add(new Task(null,
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON)),
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE)),
                                    "",
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_CLASS)),
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TYPE)),
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DESCRIPTION)),
                                    tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ATTACHMENT)),
                                    tasksCursor.getFloat(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE)),
                                    tasksCursor.getFloat(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE))
                                            + tasksCursor.getFloat(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME)),
                                    null
                            ));
                            taskIDs.add(tasksCursor.getInt(tasksCursor.getColumnIndex(DbContract.TasksEntry._ID)));
                            tasksCursor.moveToNext();
                        }
                        if (mTasksList.size() > 0)
                            rootview.findViewById(R.id.add_task).setVisibility(View.GONE);
                    } else {
                        rootview.findViewById(R.id.add_task).setVisibility(View.VISIBLE);
                    }

                    // Inflate the listview of periods
                    for (int i = 0; i < cursor.getCount(); i++) {
                        String occurrence = cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE));
                        if (!occurrence.equals("-1"))
                            mPeriodsList.add(new PeriodItem(getContext(),
                                    cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN)),
                                    cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEOUT)),
                                    cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN_ALT)),
                                    cursor.getInt(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEOUT_ALT)),
                                    cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_PERIODS)),
                                    cursor.getString(cursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_OCCURRENCE))));
                        cursor.moveToNext();
                    }

                    // Hide/Unhide the splash text
                    if (mPeriodsList.size() > 0) {
                        rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.GONE);
                    } else rootview.findViewById(R.id.schedule_detail_splash_no_periods).setVisibility(View.VISIBLE);
                }
            }
        }

        return rootview;
    }

    private View.OnClickListener addNoteListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NoteActivity.class);
                intent.putExtra(getString(R.string.INTENT_EXTRA_TITLE), title);
                startActivity(intent);
            }
        };
    }

    private AdapterView.OnItemClickListener addNoteItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DbHelper dbHelper = new DbHelper(getContext());
                Cursor cursor = dbHelper.getNoteByScheduleTitle(title);
                if (cursor.moveToPosition(i)) {
                    int _ID = cursor.getInt(cursor.getColumnIndex(DbContract.NotesEntry._ID));
                    Intent intent = new Intent(getContext(), NoteActivity.class);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_ID), _ID);
                    intent.putExtra(getString(R.string.INTENT_EXTRA_TITLE), title);
                    startActivity(intent);
                }
            }
        };
    }

}
