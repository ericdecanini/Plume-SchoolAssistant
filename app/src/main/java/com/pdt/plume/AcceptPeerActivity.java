package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;

import com.pdt.plume.data.DbContract.ScheduleEntry;

public class AcceptPeerActivity extends AppCompatActivity
        implements MismatchDialog.MismatchDialogListener {

    String LOG_TAG = AcceptPeerActivity.class.getSimpleName();

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;

    // Target User Profile Variables
    String requestingUserId;
    String iconUri;
    String name;
    String flavour;

    // Listview variables
    ListView listView;
    ScheduleAdapter adapter;

    // Arrays and Lists
    ArrayList<Bundle> usersClassesList = new ArrayList<>();
    ArrayList<Bundle> requestClassesList = new ArrayList<>();
    ArrayList<Bundle> matchedClassesList = new ArrayList<>();
    ArrayList<Schedule> matchedClassesScheduleList = new ArrayList<>();
    ArrayList<Bundle> mismatchedClassesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_peer);
        final DbHelper dbHelper = new DbHelper(this);

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null)
            loadLogInView();
        mUserId = mFirebaseUser.getUid();

        // Get references to the views
        ImageView iconView = (ImageView) findViewById(R.id.icon);
        TextView nameView = (TextView) findViewById(R.id.name);
        TextView flavourView = (TextView) findViewById(R.id.flavour);
        TextView headerView = (TextView) findViewById(R.id.header);
        listView = (ListView) findViewById(R.id.listView);
        Button acceptButton = (Button) findViewById(R.id.accept);
        Button ignoreButton = (Button) findViewById(R.id.ignore);

        // Set the listeners of the views
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
                if (checkbox.isChecked())
                    checkbox.setChecked(false);
                else checkbox.setChecked(true);
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptPeerRequest();
                deletePeerRequest();
            }
        });

        ignoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePeerRequest();
            }
        });

        // Fill in the profile data from the cloud
        Intent intent = getIntent();
        requestingUserId = intent.getStringExtra("requestingUserId");
        iconUri = intent.getStringExtra("icon");
        name = intent.getStringExtra("name");
        flavour = intent.getStringExtra("flavour");

        iconView.setImageURI(Uri.parse(iconUri));
        nameView.setText(name);
        headerView.setText(name + getString(R.string.acceptPeerHeader));
        flavourView.setText(flavour);

        // Get the array list for each class in the request
        final ArrayList<String> classPeers = new ArrayList<>();
        final ArrayList<String> classTitles = new ArrayList<>();
        final ArrayList<String> classTeachers = new ArrayList<>();
        final ArrayList<String> classRooms = new ArrayList<>();
        final ArrayList<String> classOccurrences = new ArrayList<>();
        final ArrayList<Integer> classTimeIns = new ArrayList<>();
        final ArrayList<Integer> classTimeOuts = new ArrayList<>();
        final ArrayList<Integer> classTimeInAlts = new ArrayList<>();
        final ArrayList<Integer> classTimeOutAlts = new ArrayList<>();
        final ArrayList<String> classPeriods = new ArrayList<>();
        final ArrayList<String> classIcons = new ArrayList<>();


        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("requests").child(requestingUserId).child("classes");
        classesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    classTitles.add(userSnapshot.getKey());
                    classPeers.add(userSnapshot.child("peers").getValue(String.class));
                    classTeachers.add(userSnapshot.child("teacher").getValue(String.class));
                    classRooms.add(userSnapshot.child("room").getValue(String.class));
                    classOccurrences.add(userSnapshot.child("occurrence").getValue(String.class));
                    classTimeIns.add(userSnapshot.child("timein").getValue(Integer.class));
                    classTimeOuts.add(userSnapshot.child("timeout").getValue(Integer.class));
                    classTimeInAlts.add(userSnapshot.child("timeinalt").getValue(Integer.class));
                    classTimeOutAlts.add(userSnapshot.child("timeoutalt").getValue(Integer.class));
                    classPeriods.add(userSnapshot.child("periods").getValue(String.class));
                    classIcons.add(userSnapshot.child("icon").getValue(String.class));
                }
                for (int i = 0; i < classTitles.size(); i++) {
                    Bundle bundle = new Bundle();
                    bundle.putString("peers", classPeers.get(i));
                    bundle.putString("title", classTitles.get(i));
                    bundle.putString("teacher", classTeachers.get(i));
                    bundle.putString("room", classRooms.get(i));
                    bundle.putString("occurrence", classOccurrences.get(i));
                    bundle.putInt("timein", classTimeIns.get(i));
                    bundle.putInt("timeout", classTimeOuts.get(i));
                    bundle.putInt("timeinalt", classTimeInAlts.get(i));
                    bundle.putInt("timeoutalts", classTimeOutAlts.get(i));
                    bundle.putString("periods", classPeriods.get(i));
                    bundle.putString("icon", classIcons.get(i));

                    requestClassesList.add(bundle);
                }

                // Compare each class with the current user's classes
                // Match each requested class to a class of the same name
                // If there are mismatched classes, show a dialog to match the classes
                usersClassesList = dbHelper.getAllClassesBundleArray();
                matchedClassesList.clear();
                mismatchedClassesList = new ArrayList<>();
                for (int i = 0; i < requestClassesList.size(); i++) {
                    boolean matched = false;
                    for (int ii = 0; ii < usersClassesList.size(); ii++) {
                        String userClassTitle = usersClassesList.get(ii).getString("title");
                        String requestClassTitle = requestClassesList.get(i).getString("title");
                        if (userClassTitle.equals(requestClassTitle)) {
                            matchedClassesList.add(usersClassesList.get(ii));
                            matched = true;
                        }
                    }
                    if (!matched)
                        mismatchedClassesList.add(requestClassesList.get(i));
                }

                if (mismatchedClassesList.size() > 0) {
                    // TODO: Show the matching dialog
                    // TODO: Find which method invokes accessiblity error
                    MismatchDialog dialog = MismatchDialog.newInstance();
                    Bundle args = new Bundle();
                    args.putSerializable("mismatchedClassesList", mismatchedClassesList);
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), "dialog");
                }

                // Inflate the listview
                for (int i = 0; i < matchedClassesList.size(); i++) {
                    matchedClassesScheduleList.add(new Schedule(AcceptPeerActivity.this, matchedClassesList.get(i).getString("icon"),
                            matchedClassesList.get(i).getString("title"), "", "", "", "", ""));
                }
                adapter = new ScheduleAdapter(AcceptPeerActivity.this, R.layout.list_item_schedule_with_checkbox, matchedClassesScheduleList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void OnClassesMatchedListener(ArrayList<Bundle> matchedClasses) {
        for (int i = 0; i < matchedClasses.size(); i++) {
            Bundle matchedClass = matchedClasses.get(i);
            Bundle mismatchedClass = mismatchedClassesList.get(i);
            if (matchedClasses.get(i).getString("title").equals("null")) {
                matchedClass.putString("title", mismatchedClass.getString("title") + "%0513%" + "cross");
                matchedClassesScheduleList.add(new Schedule(this, matchedClass.getString("icon"),
                        matchedClass.getString("title"), "", "", "", "", ""));
            } else {
                matchedClassesList.add(matchedClass);
                matchedClassesScheduleList.add(new Schedule(this, matchedClass.getString("icon"), matchedClass.getString("title"),
                        "", "", "", "", ""));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void acceptPeerRequest() {
        // Get references to sections of the database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                .child("users");
        DatabaseReference mUserPeersRef = usersRef.child(mUserId).child("peers");
        DatabaseReference requestingUserPeersRef = usersRef.child(requestingUserId).child("peers");
        DatabaseReference mUserClassesRef = usersRef.child(mUserId).child("classes");
        DatabaseReference requestingUserClassesRef = usersRef.child(requestingUserId).child("classes");

        // Add the class to the cloud and SQLite databases for every checked item
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++)
            if (((CheckBox) getViewByPosition(i, listView).findViewById(R.id.checkbox)).isChecked()) {
                positions.add(i);
                Bundle bundle = matchedClassesList.get(i);
                String title = bundle.getString("title");

                // Insert into firebase
                mUserPeersRef.child(requestingUserId).setValue("");
                requestingUserPeersRef.child(mUserId).setValue("");
                mUserClassesRef.child(title).child("peers").child(requestingUserId).setValue("");
                requestingUserClassesRef.child(title).child("peers").child(mUserId).setValue("");

                // Add the peer into the peers table of the SQLite database
                DbHelper dbHelper = new DbHelper(this);
                dbHelper.insertPeer(requestingUserId, iconUri, name);

                // Update SQLite classes to contain the peer into SQLite
                Cursor cursor = dbHelper.getScheduleDataByTitle(title);
                    for (int ii = 0; ii < cursor.getCount(); ii++) {
                        cursor.moveToPosition(ii);
                        Log.v(LOG_TAG, "Column count: " + cursor.getColumnCount());
                        String rowItemUser = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_USER));
                        Log.v(LOG_TAG, "mUserId: " + mUserId);

                        // Add the peer on that user's account
//                        if (rowItemUser.equals(mUserId)) {
                            int rowId = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                            String currentPeers = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PEERS));
                            if (currentPeers != null && !currentPeers.equals(""))
                                currentPeers += ":";

                            String teacher = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER));
                            String room = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM));
                            String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                            int timein = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN));
                            int timeout = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT));
                            int timeinalt = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT));
                            int timeoutalt = cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT));
                            String periods = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS));
                            String icon = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));

                            currentPeers += requestingUserId;
                            dbHelper.updateScheduleItem(rowId, currentPeers, title, teacher, room, occurrence,
                                    timein, timeout, timeinalt, timeoutalt, periods, icon, -1);
//                        }
                    }
            }
    }

    private void deletePeerRequest() {
        // Remove the peer from the cloud
        FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("requests")
                .child(requestingUserId).removeValue();

        // Close the activity
        finish();
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

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("parent", AcceptPeerActivity.class.getSimpleName());
        startActivity(intent);
        finish();
    }

}
