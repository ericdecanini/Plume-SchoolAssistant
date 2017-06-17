package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;

import static com.pdt.plume.R.id.listView;

public class RequestsActivity extends AppCompatActivity {

    String LOG_TAG = RequestsActivity.class.getSimpleName();

    // UI Data
    ProgressBar spinner;
    TextView headerTextView;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    // Arrays and Lists
    ArrayList<String> userIdList = new ArrayList<>();
    ArrayList<String> userNameList = new ArrayList<>();
    ArrayList<String> userIconList = new ArrayList<>();
    ArrayList<String> userFlavourList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        // Check if the user is logged in
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            Intent intent;
            boolean isTablet = getResources().getBoolean(R.bool.isTablet);
            if (isTablet) intent = new Intent(this, LoginActivityTablet.class);
            else intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        // Initialise the progress bar
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);
        headerTextView = (TextView) findViewById(R.id.header_textview);
        headerTextView.setVisibility(View.GONE);

        // Initialise the theme
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), mPrimaryColor);
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }

        // Inflate the list
        setRequestsListAdapater();
    }

    private void setRequestsListAdapater() {
        // First, clear all the previous data of the array lists
        userIdList.clear();
        userIconList.clear();
        userNameList.clear();
        userFlavourList.clear();

        // Get a reference to the requests database and create the array lists for the name and icon
        if (mFirebaseUser != null) {
            final DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mFirebaseUser.getUid()).child("requests").getRef();

            // Add the values to the array lists
            requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    spinner.setVisibility(View.GONE);
                    if (dataSnapshot.getChildrenCount() == 0) {
                        headerTextView.setVisibility(View.VISIBLE);
                        headerTextView.setText(getString(R.string.splash_no_request));
                    }

                    for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                        final String userIDItem = userSnapshot.getKey();
                        userIdList.add(userIDItem);
                        final int snapshotChildrenCount = (int) dataSnapshot.getChildrenCount();
                        userSnapshot.getRef().child("nickname").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userNameList.add(dataSnapshot.getValue(String.class));
                                if (userNameList.size() == snapshotChildrenCount
                                        && userIconList.size() == snapshotChildrenCount
                                        && userFlavourList.size() == snapshotChildrenCount)
                                    applyDataToListView();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                spinner.setVisibility(View.GONE);
                                headerTextView.setVisibility(View.VISIBLE);
                            }
                        });
                        userSnapshot.getRef().child("icon").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userIconList.add(dataSnapshot.getValue(String.class));
                                if (userNameList.size() == snapshotChildrenCount
                                        && userIconList.size() == snapshotChildrenCount
                                        && userFlavourList.size() == snapshotChildrenCount)
                                    applyDataToListView();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        userSnapshot.getRef().child("flavour").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userFlavourList.add(dataSnapshot.getValue(String.class));
                                if (userNameList.size() == snapshotChildrenCount
                                        && userIconList.size() == snapshotChildrenCount
                                        && userFlavourList.size() == snapshotChildrenCount)
                                    applyDataToListView();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    headerTextView.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                }
            });
        }
    }

    private void applyDataToListView() {
        // This method is fired by the previous method after all the data is collected
        ArrayList<Peer> peerArrayList = new ArrayList<>();
        for (int i = 0; i < userIdList.size(); i++)
            peerArrayList.add(new Peer(userIconList.get(i), userNameList.get(i)));

        ListView listView = (ListView) findViewById(R.id.listView);
        int layout;
        if (getResources().getBoolean(R.bool.isTablet))
            layout = R.layout.list_item_peer2;
        else layout = R.layout.list_item_peer;
        PeerAdapter adapter = new PeerAdapter(this, layout, peerArrayList);
        listView.setAdapter(adapter);
        spinner.setVisibility(View.GONE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Class activity;
                if (getResources().getBoolean(R.bool.isTablet))
                    activity = AcceptPeerActivityTablet.class;
                else activity = AcceptPeerActivity.class;
                Intent intent = new Intent(RequestsActivity.this, activity);
                intent.putExtra("requestingUserId", userIdList.get(i));
                intent.putExtra("icon", userIconList.get(i));
                intent.putExtra("name", userNameList.get(i));
                intent.putExtra("flavour", userFlavourList.get(i));
                startActivity(intent);
            }
        });
    }

}
