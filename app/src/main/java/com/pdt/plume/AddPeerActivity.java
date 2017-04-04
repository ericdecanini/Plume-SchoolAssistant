package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.pdt.plume.R.id.appbar;

public class AddPeerActivity extends AppCompatActivity {

    String LOG_TAG = AddPeerActivity.class.getSimpleName();

    // UI Elements
    ListView listView;

    // UI Data
    String iconUri;
    String name;
    String flavour;
    ProgressBar spinner;

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    String targetUserId;

    // Arrays and Adapters
    ScheduleAdapter mScheduleAdapter;
    ArrayList<Schedule> mScheduleList = new ArrayList<>();
    ArrayList<Bundle> classList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_peer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialise the Progress Bar
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor  = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);
        ((Button) findViewById(R.id.button)).setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Initialise the data
        if (mFirebaseUser != null) {
            final DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("classes");
            classesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    spinner.setVisibility(View.GONE);
                    // For listed values, arrayLists must be stored in the bundle
                    ArrayList<String> occurrenceList = new ArrayList<>();
                    for (DataSnapshot occurrenceSnapshot: dataSnapshot.child("occurrence").getChildren()) {
                        occurrenceList.add(occurrenceSnapshot.getKey());
                    }
                    ArrayList<Integer> timeInList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot: dataSnapshot.child("timein").getChildren()) {
                        timeInList.add(timeinSnapshot.getValue(int.class));
                    }
                    ArrayList<Integer> timeOutList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot: dataSnapshot.child("timeout").getChildren()) {
                        timeOutList.add(timeinSnapshot.getValue(int.class));
                    }
                    ArrayList<Integer> timeInAltList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot: dataSnapshot.child("timeinalt").getChildren()) {
                        timeInAltList.add(timeinSnapshot.getValue(int.class));
                    }
                    ArrayList<Integer> timeOutAltList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot: dataSnapshot.child("timeoutalt").getChildren()) {
                        timeOutAltList.add(timeinSnapshot.getValue(int.class));
                    }
                    ArrayList<String> periodsList = new ArrayList<>();
                    for (DataSnapshot periodSnapshot: dataSnapshot.child("periods").getChildren()) {
                        periodsList.add(periodSnapshot.getKey());
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("icon", dataSnapshot.child("icon").getValue(String.class));
                    bundle.putString("title", dataSnapshot.getKey());
                    bundle.putString("teacher", dataSnapshot.child("teacher").getValue(String.class));
                    bundle.putString("room", dataSnapshot.child("room").getValue(String.class));
                    bundle.putStringArrayList("occurrences", occurrenceList);
                    bundle.putIntegerArrayList("timeins", timeInList);
                    bundle.putIntegerArrayList("timeouts", timeOutList);
                    bundle.putIntegerArrayList("timeinalts", timeInAltList);
                    Log.v(LOG_TAG, "Putting in timeinaltlist of size " + timeInList.size());
                    bundle.putIntegerArrayList("timeoutalts", timeOutAltList);
                    bundle.putStringArrayList("periods", periodsList);

                    classList.add(bundle);
                }
                @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override public void onCancelled(DatabaseError databaseError) {
                    spinner.setVisibility(View.GONE);
                }});
        }



        // Reference all the views
        final ImageView iconView = (ImageView) findViewById(R.id.icon);
        final TextView nameView = (TextView) findViewById(R.id.name);
        final TextView flavourView = (TextView) findViewById(R.id.flavour);
        listView = (ListView) findViewById(R.id.listView);
        Button button = (Button) findViewById(R.id.button);

        // Set the listeners for the views
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
                if (checkbox.isChecked())
                    checkbox.setChecked(false);
                else checkbox.setChecked(true);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPeerRequest();
                Intent intent = new Intent(AddPeerActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Initialise the data for the peer's profile
        Intent intent = getIntent();
        targetUserId = intent.getStringExtra("id");
        Log.v(LOG_TAG, "UserId: " + targetUserId);
        DatabaseReference peerProfileRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(targetUserId);
        if (peerProfileRef != null) {
            peerProfileRef.child("nickname").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name = (String) dataSnapshot.getValue();
                    nameView.setText(name);
                    ((TextView) findViewById(R.id.whichClasses)).setText
                            (getString(R.string.whichClasses, name));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            peerProfileRef.child("icon").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    iconUri = (String) dataSnapshot.getValue();
                    iconView.setImageURI(Uri.parse(iconUri));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            peerProfileRef.child("flavour").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    flavour = (String) dataSnapshot.getValue();
                    flavourView.setText(flavour);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // Set the listview adapter
        mScheduleAdapter = new ScheduleAdapter(this, R.layout.list_item_schedule_with_checkbox, mScheduleList);
        listView.setAdapter(mScheduleAdapter);
        getAllClassesArray();
    }

    private void sendPeerRequest() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        // Get the ref for the location where the request will be stored
        DatabaseReference requestsRef = rootRef.child("users")
                .child(targetUserId).child("requests").child(mUserId);

        // Get the current user's data
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selfName = preferences.getString(getString(R.string.KEY_PREFERENCES_SELF_NAME), "");
        String selfIcon = preferences.getString(getString(R.string.KEY_PREFERENCES_SELF_ICON), "");

        // Set the key data of the request
        requestsRef.child("nickname").setValue(selfName);
        requestsRef.child("icon").setValue(selfIcon);

        // Get the ref of the classes list in the cloud database
        DatabaseReference classesRef = requestsRef.child("classes");

        // Run through the list for every checked item and create an arrayList
        ArrayList<String> classTitles = new ArrayList<>();
        ArrayList<String> classTeachers = new ArrayList<>();
        ArrayList<String> classRooms = new ArrayList<>();
        ArrayList<ArrayList<String>> classOccurrences = new ArrayList<>();
        ArrayList<ArrayList<Integer>> classTimeIns = new ArrayList<>();
        ArrayList<ArrayList<Integer>> classTimeOuts = new ArrayList<>();
        ArrayList<ArrayList<Integer>> classTimeInAlts = new ArrayList<>();
        ArrayList<ArrayList<Integer>> classTimeOutAlts = new ArrayList<>();
        ArrayList<ArrayList<String>> classPeriods = new ArrayList<>();
        ArrayList<String> classIcons = new ArrayList<>();


        for (int i = 0; i < listView.getCount(); i++)
            if (((CheckBox) getViewByPosition(i, listView).findViewById(R.id.checkbox)).isChecked()) {
                classTitles.add(classList.get(i).getString("title"));
                classTeachers.add(classList.get(i).getString("teacher"));
                classRooms.add(classList.get(i).getString("room"));
                classOccurrences.add(classList.get(i).getStringArrayList("occurrences"));
                classTimeIns.add(classList.get(i).getIntegerArrayList("timeins"));
                classTimeOuts.add(classList.get(i).getIntegerArrayList("timeouts"));
                classTimeInAlts.add(classList.get(i).getIntegerArrayList("timeinalts"));
                Log.v(LOG_TAG, "Getting timeinaltlist of size " + classList.get(i).getIntegerArrayList("timeinalts").size());
                classTimeOutAlts.add(classList.get(i).getIntegerArrayList("timeoutalts"));
                classPeriods.add(classList.get(i).getStringArrayList("periods"));
                classIcons.add(classList.get(i).getString("icon"));
            }

        // Finally, add the classes to the cloud
        for (int i = 0; i < classTitles.size(); i++) {
            DatabaseReference classTitleRef = classesRef.child(classTitles.get(i));
            // Set the key values to the cloud
            classTitleRef.child("icon").setValue(classIcons.get(i));
            classTitleRef.child("teacher").setValue(classTeachers.get(i));
            classTitleRef.child("room").setValue(classRooms.get(i));

            // Set the listed values to the cloud
            if (classOccurrences.size() != 0)
                for (int l = 0; l < classOccurrences.get(0).size(); l++) {
                    Log.v(LOG_TAG, "l = " + l);
                    classTitleRef.child("occurrence").child(classOccurrences.get(i).get(l)).setValue("");
                    classTitleRef.child("timein").child(String.valueOf(l)).setValue(classTimeIns.get(i).get(l));
                    classTitleRef.child("timeout").child(String.valueOf(l)).setValue(classTimeOuts.get(i).get(l));
                    classTitleRef.child("timeinalt").child(String.valueOf(l)).setValue(classTimeInAlts.get(i).get(l));
                    Log.v(LOG_TAG, "ClassTimeOutAltsSize: " + classTimeOutAlts.size());
                    Log.v(LOG_TAG, "ClassTimeOutAlts" + i + "Size: " + classTimeOutAlts.get(i).size());
                    classTitleRef.child("timeoutalt").child(String.valueOf(l)).setValue(classTimeOutAlts.get(i).get(l));
                    classTitleRef.child("periods").child(classPeriods.get(i).get(l)).setValue("");
                }
        }

        Toast.makeText(this, getString(R.string.requestSent, name), Toast.LENGTH_SHORT).show();

    }

    private void getAllClassesArray() {
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("classes");
        classesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String icon = dataSnapshot.child("icon").getValue(String.class);
                String title = dataSnapshot.getKey();
                String teacher = dataSnapshot.child("teacher").getValue(String.class);
                String room = dataSnapshot.child("room").getValue(String.class);

                mScheduleList.add(new Schedule(AddPeerActivity.this, icon, title, teacher, room, "", "", "", null));
                mScheduleAdapter.notifyDataSetChanged();
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {}});
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

}
