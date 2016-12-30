package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.pdt.plume.R.string.sendPeerRequest;

public class AddPeerActivity extends AppCompatActivity {

    String LOG_TAG = AddPeerActivity.class.getSimpleName();

    // UI Elements
    ListView listView;

    // UI Data
    String iconUri;
    String name;
    String flavour;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;
    String targetUserId;

    // Arrays
    ArrayList<Bundle> classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_peer);

        // Initialise the data
        DbHelper dbHelper = new DbHelper(this);
        classList = dbHelper.getAllClassesBundleArray();

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

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
                            (getString(R.string.whichClasses) + name + getString(R.string.in));

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
        ScheduleAdapter adapter = new ScheduleAdapter(this, R.layout.list_item_schedule_with_checkbox, dbHelper.getAllClassesArray(this));
        listView.setAdapter(adapter);
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
        ArrayList<String> classOccurrences = new ArrayList<>();
        ArrayList<Integer> classTimeIns = new ArrayList<>();
        ArrayList<Integer> classTimeOuts = new ArrayList<>();
        ArrayList<Integer> classTimeInAlts = new ArrayList<>();
        ArrayList<Integer> classTimeOutAlts = new ArrayList<>();
        ArrayList<String> classPeriods = new ArrayList<>();
        ArrayList<String> classIcons = new ArrayList<>();


        for (int i = 0; i < listView.getCount(); i++)
            if (((CheckBox) getViewByPosition(i, listView).findViewById(R.id.checkbox)).isChecked()) {
                classTitles.add(classList.get(i).getString("title"));
                classTeachers.add(classList.get(i).getString("teacher"));
                classRooms.add(classList.get(i).getString("room"));
                classOccurrences.add(classList.get(i).getString("occurrence"));
                classTimeIns.add(classList.get(i).getInt("timein"));
                classTimeOuts.add(classList.get(i).getInt("timeout"));
                classTimeInAlts.add(classList.get(i).getInt("timeinalt"));
                classTimeOutAlts.add(classList.get(i).getInt("timeoualt"));
                classPeriods.add(classList.get(i).getString("periods"));
                classIcons.add(classList.get(i).getString("icon"));
            }

        // Finally, add the classes to the cloud
        for (int i = 0; i < classTitles.size(); i++) {
            DatabaseReference classTitleRef = classesRef.child(classTitles.get(i));
            classTitleRef.child("icon").setValue(classIcons.get(i));
            classTitleRef.child("teacher").setValue(classTeachers.get(i));
            classTitleRef.child("room").setValue(classRooms.get(i));
            classTitleRef.child("occurrence").setValue(classOccurrences.get(i));
            classTitleRef.child("timein").setValue(classTimeIns.get(i));
            classTitleRef.child("timeout").setValue(classTimeOuts.get(i));
            classTitleRef.child("timeinalt").setValue(classTimeInAlts.get(i));
            classTitleRef.child("timeoutalt").setValue(classTimeOutAlts.get(i));
            classTitleRef.child("period").setValue(classPeriods.get(i));
        }

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
