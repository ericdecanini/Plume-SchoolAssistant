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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class AcceptPeerActivity extends AppCompatActivity {

    String LOG_TAG = AcceptPeerActivity.class.getSimpleName();

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;

    // Self Profile Variables
    String selfName;
    String selfIcon;
    String selfFlavour;

    // Target User Profile Variables
    String requestingUserId;
    String icon;
    String iconUri;
    String name;
    String flavour;

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    // Listview variables
    ListView listView;
    CheckScheduleAdapter mClassAdapter;
    ProgressBar spinner;
    View splash;

    // Arrays and Lists
    ArrayList<MatchingClass> mClassList = new ArrayList<>();
    ArrayList<MatchingClass> mMatchingList = new ArrayList<>();
    ArrayList<String> addedClasses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) savedInstanceState.clear();

        if (!getResources().getBoolean(R.bool.isTablet)) setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_accept_peer);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        boolean isLandscape = getResources().getBoolean(R.bool.isLandscape);

        if (isTablet) {
            if (getResources().getBoolean(R.bool.isLandscape))
                getSupportActionBar().setElevation(0f);
        } else {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialise the Progress Bar
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);
        splash = findViewById(R.id.splash);

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.95f; // value component
        int actionColor = Color.HSVToColor(hsv);
        int backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));
        Color.colorToHSV(backgroundColor, hsv);
        hsv[2] *= 0.9f;
        int darkBackgroundColor = Color.HSVToColor(hsv);
        findViewById(R.id.activity_people).setBackgroundColor(backgroundColor);
        int textColor = preferences.getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), getResources().getColor(R.color.gray_900));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
            findViewById(R.id.accept).setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
        }

        if (isTablet) {
            if (isLandscape) {
                findViewById(R.id.gradient_overlay).setBackgroundColor(mPrimaryColor);
                findViewById(R.id.cardview).setBackgroundColor(backgroundColor);
                findViewById(R.id.activity_people).setBackgroundColor(darkBackgroundColor);
            } else {
                findViewById(R.id.activity_people).setBackgroundColor(backgroundColor);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(mDarkColor);
                findViewById(R.id.accept).setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
            }
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionColor));
            findViewById(R.id.extended_appbar).setBackgroundColor(mPrimaryColor);
        } else {
            findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);
        }

        mClassAdapter = new CheckScheduleAdapter(AcceptPeerActivity.this, R.layout.list_item_check_schedule, mClassList);
        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            loadLogInView();
            return;
        }
        mUserId = mFirebaseUser.getUid();

        // Set the self ref data
        DatabaseReference selfRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId);
        selfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                splash.setVisibility(View.GONE);
                selfName = dataSnapshot.child("nickname").getValue(String.class);
                selfIcon = dataSnapshot.child("icon").getValue(String.class);
                selfFlavour = dataSnapshot.child("flavour").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                spinner.setVisibility(View.GONE);
                splash.setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.textView1)).setText(getString(R.string.check_internet));
            }
        });

        // Get references to the views
        final ImageView iconView = (ImageView) findViewById(R.id.icon);
        TextView nameView = (TextView) findViewById(R.id.name);
        TextView flavourView = (TextView) findViewById(R.id.flavour);
        final TextView headerView = (TextView) findViewById(R.id.header);
        listView = (ListView) findViewById(R.id.listView);
        Button acceptButton = (Button) findViewById(R.id.accept);
        Button ignoreButton = (Button) findViewById(R.id.ignore);

        // Set the listeners of the views
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.check);
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
        icon = intent.getStringExtra("icon");
        iconUri = intent.getStringExtra("icon").replace("icon", requestingUserId);
        name = intent.getStringExtra("title");
        flavour = intent.getStringExtra("flavour");

        nameView.setText(name);
        headerView.setText(getString(R.string.acceptPeerHeader, name));
        headerView.setTextColor(textColor);
        flavourView.setText(flavour);

        // Check if the icon points to an existing file
        // First check if the icon uses a default drawable or from the storage
        if (!iconUri.contains("android.resource://com.pdt.plume")) {
            File file = new File(getFilesDir(), iconUri);
            iconView.setImageURI(Uri.parse(iconUri));
            if (file.exists()) {
                iconView.setImageURI(Uri.parse(iconUri));
            } else {
                // File doesn't exist: Download from storage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference iconRef = storageRef.child(requestingUserId).child("/icon");

                file = new File(getFilesDir(), requestingUserId + ".jpg");
                iconUri = Uri.fromFile(file).toString();

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

        // Get the array list for each class in the request
        final DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("requests").child(requestingUserId).child("classes");
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    spinner.setVisibility(View.GONE);
                    headerView.setText(getString(R.string.whichClassesNone2, name));
                    headerView.setVisibility(View.VISIBLE);
                }

                int i = 0;
                final int snapshotCount = ((int) dataSnapshot.getChildrenCount());
                for (final DataSnapshot requestClassSnapshot : dataSnapshot.getChildren()) {
                    i++;
                    headerView.setVisibility(View.VISIBLE);
                    final String newTitle = requestClassSnapshot.child("newtitle").getValue(String.class);

                    // Match each requested class with the user's classes
                    final int finalI = i;

                    FirebaseDatabase.getInstance().getReference().child("users")
                            .child(mUserId).child("classes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int classCount = ((int) dataSnapshot.getChildrenCount());
                            int i1 = 0;
                            int oldListSize = mClassList.size();

                            for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                                i1++;

                                if (classSnapshot.getKey().equals(requestClassSnapshot.getKey())) {
                                    // CLASSES MATCHED: Add to schedule list
                                    mClassList.add(new MatchingClass(
                                            classSnapshot.child("icon").getValue(String.class),
                                            classSnapshot.getKey(),
                                            requestClassSnapshot.child("icon").getValue(String.class),
                                            requestClassSnapshot.getKey()));

                                    classesRef.child(requestClassSnapshot.getKey()).child("newtitle")
                                            .setValue(requestClassSnapshot.getKey());
                                    classesRef.child(requestClassSnapshot.getKey()).child("newicon")
                                            .setValue(requestClassSnapshot.child("icon").getValue());

                                    addedClasses.add(classSnapshot.getKey());
                                } else if (newTitle != null && newTitle.equals(classSnapshot.getKey())) {
                                    if (!addedClasses.contains(newTitle)) {
                                        mClassList.add(new MatchingClass(
                                                requestClassSnapshot.child("newicon").getValue(String.class),
                                                requestClassSnapshot.child("newtitle").getValue(String.class),
                                                requestClassSnapshot.child("icon").getValue(String.class),
                                                requestClassSnapshot.getKey()));
                                        addedClasses.add(requestClassSnapshot.child("newtitle").getValue(String.class));
                                    }
                                } else if (oldListSize == mClassList.size() && i1 == classCount) {
                                    // CLASSES NOT MATCHED, send with empty icon and category
                                    mMatchingList.add(new MatchingClass("","",
                                            requestClassSnapshot.child("icon").getValue(String.class),
                                            requestClassSnapshot.getKey()));

                                    if (finalI == snapshotCount && mMatchingList.size() > 0) {
                                        // Send the matching list to MatchClassActivity
                                        Intent intent = new Intent(AcceptPeerActivity.this, MatchClassActivity.class);
                                        intent.putExtra("matchingList", (mMatchingList));
                                        intent.putExtra("id", requestingUserId);
                                        intent.putExtra("name", name);
                                        intent.putExtra("icon", icon);
                                        intent.putExtra("flavour", flavour);
                                        startActivity(intent);
                                    }
                                }
                            }

                            mClassAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mClassAdapter = new CheckScheduleAdapter(AcceptPeerActivity.this, R.layout.list_item_check_schedule, mClassList);
                    listView.setAdapter(mClassAdapter);
                    spinner.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                spinner.setVisibility(View.GONE);
            }
        });

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
        mUserPeersRef.child(requestingUserId).child("nickname").setValue(name);
        mUserPeersRef.child(requestingUserId).child("icon").setValue(iconUri);
        mUserPeersRef.child(requestingUserId).child("flavour").setValue(flavour);
        // Requesting user's peers ref
        requestingUserPeersRef.child(mUserId).child("nickname").setValue(selfName);
        requestingUserPeersRef.child(mUserId).child("icon").setValue(selfIcon);
        requestingUserPeersRef.child(mUserId).child("flavour").setValue(selfFlavour);

        for (int i = 0; i < mClassList.size(); i++)
            if (((CheckBox) getViewByPosition(i, listView).findViewById(R.id.check)).isChecked()) {
                positions.add(i);
                String title = mClassList.get(i).title;
                String icon = mClassList.get(i).icon;
                String originalTitle = mClassList.get(i).originalTitle;
                String originalIcon = mClassList.get(i).originalIcon;

                // Insert into firebase
                // User's peers ref
                mUserPeersRef.child(requestingUserId).child("nickname").setValue(name);
                mUserPeersRef.child(requestingUserId).child("icon").setValue(iconUri);
                mUserPeersRef.child(requestingUserId).child("flavour").setValue(flavour);

                // Requesting user's peers ref
                requestingUserPeersRef.child(mUserId).child("nickname").setValue(selfName);
                requestingUserPeersRef.child(mUserId).child("icon").setValue(selfIcon);
                requestingUserPeersRef.child(mUserId).child("flavour").setValue(selfFlavour);

                // User's class ref
                DatabaseReference classRef = mUserClassesRef.child(title).child("peers").child(requestingUserId);
                classRef.child("originalicon").setValue(originalIcon);
                classRef.child("originaltitle").setValue(originalTitle);

                // Requesting user's class ref
                classRef = requestingUserClassesRef.child(originalTitle).child("peers").child(mUserId);
                classRef.child("originalicon").setValue(icon);
                classRef.child("originaltitle").setValue(title);

                // User's peer class ref
                classRef = mUserPeersRef.child(requestingUserId).child("classes").child(title);
                classRef.child("icon").setValue(icon);
                classRef.child("originalicon").setValue(originalIcon);
                classRef.child("originaltitle").setValue(originalTitle);

                // Requesting user's peer class ref
                classRef = requestingUserPeersRef.child(mUserId).child("classes").child(originalTitle);
                classRef.child("icon").setValue(originalIcon);
                classRef.child("originalicon").setValue(icon);
                classRef.child("originaltitle").setValue(title);

            }
    }

    private void deletePeerRequest() {
        // Remove the peer from the cloud
        FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("requests")
                .child(requestingUserId).removeValue();

        // Close the activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
