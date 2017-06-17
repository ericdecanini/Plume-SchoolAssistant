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

public class AcceptPeerActivityTablet extends AppCompatActivity
        implements MismatchDialog.MismatchDialogListener {

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
    String iconUri;
    String name;
    String flavour;

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    // Listview variables
    ListView listView;
    ScheduleAdapter mScheduleAdapter;
    ProgressBar spinner;

    // Arrays and Lists
    ArrayList<Schedule> mScheduleList = new ArrayList<>();
    ArrayList<String> mismatchedClassesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_peer);
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        boolean isLandscape = getResources().getBoolean(R.bool.isLandscape);

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
            findViewById(R.id.accept).setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
        }

        if (isTablet) {
            if (isLandscape)
                findViewById(R.id.gradient_overlay).setBackgroundColor(mPrimaryColor);
            findViewById(R.id.extended_appbar).setBackgroundColor(mPrimaryColor);
        } else {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
            findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);
        }

        mScheduleAdapter = new ScheduleAdapter(AcceptPeerActivityTablet.this, R.layout.list_item_schedule_with_checkbox, mScheduleList);
        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null)
            loadLogInView();
        mUserId = mFirebaseUser.getUid();

        // Set the self ref data
        DatabaseReference selfRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId);
        selfRef.child("nickname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfName = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        selfRef.child("icon").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfIcon = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        selfRef.child("flavour").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfFlavour = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
        iconUri = intent.getStringExtra("icon").replace("icon", requestingUserId);
        name = intent.getStringExtra("name");
        flavour = intent.getStringExtra("flavour");

        nameView.setText(name);
        headerView.setText(getString(R.string.acceptPeerHeader, name));
        flavourView.setText(flavour);

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
                for (final DataSnapshot requestClassSnapshot : dataSnapshot.getChildren()) {
                    headerView.setVisibility(View.VISIBLE);

                    // Match each requested class with the user's classes
                    FirebaseDatabase.getInstance().getReference().child("users")
                            .child(mUserId).child("classes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int oldSize = mScheduleList.size();
                            for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                                if (classSnapshot.getKey().equals(requestClassSnapshot.getKey())) {
                                    // CLASSES MATCHED: Add to schedule list
                                    mScheduleList.add(new Schedule(AcceptPeerActivityTablet.this,
                                            classSnapshot.child("icon").getValue(String.class),
                                            classSnapshot.getKey(), "", "", "", "", ""));
                                }
                            }
                            if (oldSize == mScheduleList.size()) {
                                // Size is the same, no classes matched, add to dialog
                                mismatchedClassesList.add(requestClassSnapshot.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mScheduleAdapter = new ScheduleAdapter(AcceptPeerActivityTablet.this, R.layout.list_item_schedule_with_checkbox, mScheduleList);
                    listView.setAdapter(mScheduleAdapter);
                    spinner.setVisibility(View.GONE);
                }

                // Show the mismatched dialog if applicable
                if (mismatchedClassesList.size() > 0) {
                    MismatchDialog dialog = MismatchDialog.newInstance();
                    Bundle args = new Bundle();
                    args.putString("uid", requestingUserId);
                    args.putStringArrayList("mismatched", mismatchedClassesList);
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), "dialog");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                spinner.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void OnClassesMatchedListener(ArrayList<Bundle> matchedClasses) {
        for (int i = 0; i < matchedClasses.size(); i++) {
            String title = matchedClasses.get(i).getString("title");
            String icon = matchedClasses.get(i).getString("icon");
            mScheduleList.add(new Schedule(this, icon, title, "", "", "", "", ""));
        }
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

        for (int i = 0; i < mScheduleList.size(); i++)
            if (((CheckBox) getViewByPosition(i, listView).findViewById(R.id.checkbox)).isChecked()) {
                positions.add(i);
                String title = mScheduleList.get(i).scheduleLesson;
                String classIcon = mScheduleList.get(i).scheduleIcon;

                // Insert into firebase
                // User's peers ref
                mUserPeersRef.child(requestingUserId).child("nickname").setValue(name);
                mUserPeersRef.child(requestingUserId).child("icon").setValue(iconUri);
                mUserPeersRef.child(requestingUserId).child("flavour").setValue(flavour);
                mUserPeersRef.child(requestingUserId).child("classes").child(title).setValue(classIcon);
                // Requesting user's peers ref
                requestingUserPeersRef.child(mUserId).child("nickname").setValue(selfName);
                requestingUserPeersRef.child(mUserId).child("icon").setValue(selfIcon);
                requestingUserPeersRef.child(mUserId).child("flavour").setValue(selfFlavour);
                requestingUserPeersRef.child(mUserId).child("classes").child(title).setValue(classIcon);
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
