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

import java.io.File;
import java.util.ArrayList;

import static com.pdt.plume.R.id.appbar;
import static com.pdt.plume.R.id.view;

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
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        if (isTablet) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } else {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialise the Progress Bar
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        ((Button) findViewById(R.id.button)).setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));

        if (isTablet) {
            findViewById(R.id.gradient_overlay).setBackgroundColor(mPrimaryColor);
            findViewById(R.id.extended_appbar).setBackgroundColor(mPrimaryColor);
        } else {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
            findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);
        }

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null)
            return;


        mUserId = mFirebaseUser.getUid();



        // Initialise the data
        final DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("classes");
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                spinner.setVisibility(View.GONE);
                for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                    // For listed values, arrayLists must be stored in the bundle
                    ArrayList<String> occurrenceList = new ArrayList<>();
                    for (DataSnapshot occurrenceSnapshot : classSnapshot.child("occurrence").getChildren()) {
                        occurrenceList.add(occurrenceSnapshot.getKey());
                    }
                    ArrayList<Integer> timeInList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timein").getChildren()) {
                        timeInList.add(timeinSnapshot.getValue(int.class));
                    }
                    ArrayList<Integer> timeOutList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timeout").getChildren()) {
                        timeOutList.add(timeinSnapshot.getValue(int.class));
                    }
                    ArrayList<Integer> timeInAltList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timeinalt").getChildren()) {
                        timeInAltList.add(timeinSnapshot.getValue(int.class));
                    }
                    ArrayList<Integer> timeOutAltList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timeoutalt").getChildren()) {
                        timeOutAltList.add(timeinSnapshot.getValue(int.class));
                    }
                    ArrayList<String> periodsList = new ArrayList<>();
                    for (DataSnapshot periodSnapshot : classSnapshot.child("periods").getChildren()) {
                        periodsList.add(periodSnapshot.getKey());
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("icon", classSnapshot.child("icon").getValue(String.class));
                    bundle.putString("title", classSnapshot.getKey());
                    bundle.putString("teacher", classSnapshot.child("teacher").getValue(String.class));
                    bundle.putString("room", classSnapshot.child("room").getValue(String.class));
                    bundle.putStringArrayList("occurrences", occurrenceList);
                    bundle.putIntegerArrayList("timeins", timeInList);
                    bundle.putIntegerArrayList("timeouts", timeOutList);
                    bundle.putIntegerArrayList("timeinalts", timeInAltList);
                    bundle.putIntegerArrayList("timeoutalts", timeOutAltList);
                    bundle.putStringArrayList("periods", periodsList);

                    classList.add(bundle);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                spinner.setVisibility(View.GONE);
            }
        });


        // Reference all the views
        final ImageView iconView = (ImageView) findViewById(R.id.icon);
        final TextView nameView = (TextView) findViewById(R.id.name);
        final TextView flavourView = (TextView) findViewById(R.id.flavour);
        listView = (ListView) findViewById(R.id.listView);
        final Button button = (Button) findViewById(R.id.button);

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
                button.setEnabled(false);
            }
        });

        // Initialise the data for the peer's profile
        Intent intent = getIntent();
        targetUserId = intent.getStringExtra("id");

        DatabaseReference peersRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("peers");
        peersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot peerSnapshot: dataSnapshot.getChildren()) {
                    if (peerSnapshot.getKey().equals(targetUserId)) {
                        Log.v(LOG_TAG, "Peer: " + peerSnapshot.getKey() + ", " + targetUserId);
                        Intent intent = new Intent(AddPeerActivity.this, PeerProfileActivity.class);
                        intent.putExtra("uid", peerSnapshot.getKey());
                        intent.putExtra("name", peerSnapshot.child("nickname").getValue(String.class));
                        intent.putExtra("icon", peerSnapshot.child("icon").getValue(String.class));
                        intent.putExtra("flavour", peerSnapshot.child("flavour").getValue(String.class));
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference peerProfileRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(targetUserId);
        if (peerProfileRef != null) {
            peerProfileRef.child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
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
            peerProfileRef.child("icon").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    iconUri = dataSnapshot.getValue(String.class).replace("icon", targetUserId);
                    // Check if the icon points to an existing file
                    // First check if the icon uses a default drawable or from the storage
                    if (!iconUri.contains("android.resource://com.pdt.plume")) {
                        String[] iconUriSplit = iconUri.split("/");
                        File file = new File(getFilesDir(), iconUriSplit[iconUriSplit.length - 1]);
                        iconView.setImageURI(Uri.parse(iconUri));
                        if (file.exists()) {
                            iconView.setImageURI(Uri.parse(iconUri));
                        } else {
                            // File doesn't exist: Download from storage
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();
                            StorageReference iconRef = storageRef.child(mUserId).child("/icon");

                            file = new File(getFilesDir(), "icon.jpg");
                            iconUri = Uri.fromFile(file).toString();
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(targetUserId).child("icon")
                                    .setValue(iconUri);

                            iconRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    iconView.setImageURI(Uri.parse(iconUri));
                                }
                            });
                        }
                    } else {
                        iconView.setImageURI(Uri.parse(iconUri));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            peerProfileRef.child("flavour").addListenerForSingleValueEvent(new ValueEventListener() {
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

        // Check if a request is still pending
        final DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(targetUserId).child("requests").child(mUserId);
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long snapshotCount = dataSnapshot.getChildrenCount();
                if (snapshotCount > 0) {
                    final TextView cancelButton = (TextView) findViewById(R.id.cancel_button);
                    listView.setVisibility(View.GONE);
                    TextView headerTextview = (TextView) findViewById(R.id.whichClasses);
                    headerTextview.setText(getString(R.string.request_pending));
                    headerTextview.setTextColor(mPrimaryColor);
                    final int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
                    float density = getResources().getDisplayMetrics().density;
                    int buttonWidth = cancelButton.getWidth();
                    headerTextview.setMaxWidth(((int) (screenWidth - buttonWidth - (16 * density))));
                    cancelButton.setVisibility(View.VISIBLE);
                    button.setVisibility(View.GONE);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestsRef.removeValue();
                            // Set the activity back to default
                            listView.setVisibility(View.VISIBLE);
                            TextView headerTextview = (TextView) findViewById(R.id.whichClasses);
                            headerTextview.setMaxWidth(screenWidth);
                            headerTextview.setText(getString(R.string.whichClasses, name));
                            headerTextview.setTextColor(getResources().getColor(R.color.gray_700));
                            cancelButton.setVisibility(View.GONE);
                            button.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // Set the listview mScheduleAdapter
        mScheduleAdapter = new ScheduleAdapter(this, R.layout.list_item_schedule_with_checkbox, mScheduleList);
        listView.setAdapter(mScheduleAdapter);
        getAllClassesArray();
    }

    private void sendPeerRequest() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        // Get the ref for the location where the request will be stored
        final DatabaseReference requestsRef = rootRef.child("users")
                .child(targetUserId).child("requests").child(mUserId);

        // Get the current user's data
        FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String selfName = dataSnapshot.child("nickname").getValue(String.class);
                        String selfIcon = dataSnapshot.child("icon").getValue(String.class);
                        requestsRef.child("nickname").setValue(selfName);
                        requestsRef.child("icon").setValue(selfIcon);
                        Toast.makeText(AddPeerActivity.this, getString(R.string.requestSent, name), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddPeerActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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
                    classTitleRef.child("occurrence").child(classOccurrences.get(i).get(l)).setValue("");
                    classTitleRef.child("timein").child(String.valueOf(l)).setValue(classTimeIns.get(i).get(l));
                    classTitleRef.child("timeout").child(String.valueOf(l)).setValue(classTimeOuts.get(i).get(l));
                    classTitleRef.child("timeinalt").child(String.valueOf(l)).setValue(classTimeInAlts.get(i).get(l));
                    classTitleRef.child("timeoutalt").child(String.valueOf(l)).setValue(classTimeOutAlts.get(i).get(l));
                    classTitleRef.child("periods").child(classPeriods.get(i).get(l)).setValue("");
                }
        }
    }

    private void getAllClassesArray() {
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("classes");

        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                findViewById(R.id.whichClasses).setVisibility(View.VISIBLE);
                if (dataSnapshot.getChildrenCount() == 0)
                    ((TextView) findViewById(R.id.whichClasses)).setText(getString(R.string.whichClassesNone1, name));

                for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                    String icon = classSnapshot.child("icon").getValue(String.class);
                    String title = classSnapshot.getKey();
                    String teacher = classSnapshot.child("teacher").getValue(String.class);
                    String room = classSnapshot.child("room").getValue(String.class);

                    mScheduleList.add(new Schedule(AddPeerActivity.this, icon, title, teacher, room, "", "", ""));
                    mScheduleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ((TextView) findViewById(R.id.whichClasses)).setText(getString(R.string.check_internet));
                findViewById(R.id.whichClasses).setVisibility(View.VISIBLE);
            }
        });
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

}
