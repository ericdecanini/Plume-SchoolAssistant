package com.pdt.plume;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.api.model.StringList;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PeerProfileActivity extends AppCompatActivity {

    String LOG_TAG = PeerProfileActivity.class.getSimpleName();

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId, uid, profileName, profileIcon, profileFlavour;

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    // List Variables
    ListView listView;
    ArrayList<Schedule> arrayList = new ArrayList<>();
    ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get reference to the views
        TextView nameView = (TextView) findViewById(R.id.name);
        TextView flavourView = (TextView) findViewById(R.id.flavour);
        ImageView iconView = (ImageView) findViewById(R.id.icon);

        // Initialise the theme
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
        findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            loadLogInView();
            return;
        }
        mUserId = mFirebaseUser.getUid();

        // Get the intent data (User's profile data) from SQLite
        // This data was what was taken from SQLite, not the cloud
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        profileName = intent.getStringExtra("name");
        profileIcon = intent.getStringExtra("icon");
        profileFlavour = intent.getStringExtra("flavour");

        // Set the key data
        nameView.setText(profileName);
        iconView.setImageURI(Uri.parse(profileIcon));
        flavourView.setText(profileFlavour);

        // Initialise and inflate the listview
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ScheduleAdapter(this, R.layout.list_item_schedule_with_menu, arrayList);
        listView.setAdapter(adapter);
        getPeerClassesArrayData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.dialog_remove_peer, profileName))
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Remove the peer from the peers tab
                                final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                                        .child("users");
                                usersRef.child(mUserId).child("peers").child(uid).removeValue();
                                usersRef.child(uid).child("peers").child(mUserId).removeValue();
                            }
                        })
                        .show();
                return true;
        }
        return false;
    }

    private void getPeerClassesArrayData() {
        // Get Array data from Firebase

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("peers").child(uid).child("classes");

        classesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.v(LOG_TAG, "onChildAdded");
                String title = dataSnapshot.getKey();
                String iconUri = dataSnapshot.getValue(String.class);
                Schedule schedule = new Schedule(PeerProfileActivity.this, iconUri, title,
                        "", "", "", "", "");
                schedule.addExtra(uid);
                arrayList.add(schedule);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.v(LOG_TAG, "onChildChanged");
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
        });
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("parent", AcceptPeerActivity.class.getSimpleName());
        startActivity(intent);
        finish();
    }
}