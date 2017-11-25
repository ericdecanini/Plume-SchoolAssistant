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
import android.widget.Toast;

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
    CheckScheduleAdapter mScheduleAdapter;
    ArrayList<MatchingClass> mScheduleList = new ArrayList<>();
    ArrayList<ClassItem> mClassList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) savedInstanceState.clear();

        if (!getResources().getBoolean(R.bool.isTablet)) setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_add_peer);
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

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
        final int textColor = preferences.getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), getResources().getColor(R.color.gray_900));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
            ((Button) findViewById(R.id.button)).setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
        }

        if (isTablet) {
            if (getResources().getBoolean(R.bool.isLandscape)) {
                findViewById(R.id.cardview).setBackgroundColor(backgroundColor);
                findViewById(R.id.activity_people).setBackgroundColor(darkBackgroundColor);
                findViewById(R.id.gradient_overlay).setBackgroundColor(mPrimaryColor);
            } else findViewById(R.id.activity_people).setBackgroundColor(backgroundColor);
            
            findViewById(R.id.extended_appbar).setBackgroundColor(mPrimaryColor);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(mDarkColor);
                ((Button) findViewById(R.id.button)).setBackgroundTintList(ColorStateList.valueOf(mPrimaryColor));
            }
        } else {
            findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);
            findViewById(R.id.activity_people).setBackgroundColor(backgroundColor);
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
                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    // For listed values, arrayLists must be stored in the bundle
                    ArrayList<String> occurrenceList = new ArrayList<>();
                    for (DataSnapshot occurrenceSnapshot : classSnapshot.child("occurrence").getChildren()) {
                        occurrenceList.add(occurrenceSnapshot.getValue(String.class));
                    }
                    ArrayList<Long> timeInList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timein").getChildren()) {
                        timeInList.add(timeinSnapshot.getValue(long.class));
                    }
                    ArrayList<Long> timeOutList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timeout").getChildren()) {
                        timeOutList.add(timeinSnapshot.getValue(long.class));
                    }
                    ArrayList<Long> timeInAltList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timeinalt").getChildren()) {
                        timeInAltList.add(timeinSnapshot.getValue(long.class));
                    }
                    ArrayList<Long> timeOutAltList = new ArrayList<>();
                    for (DataSnapshot timeinSnapshot : classSnapshot.child("timeoutalt").getChildren()) {
                        timeOutAltList.add(timeinSnapshot.getValue(long.class));
                    }
                    ArrayList<String> periodsList = new ArrayList<>();
                    for (DataSnapshot periodSnapshot : classSnapshot.child("periods").getChildren()) {
                        periodsList.add(periodSnapshot.getValue(String.class));
                    }

                    String icon = classSnapshot.child("icon").getValue(String.class);
                    String title = classSnapshot.getKey();
                    String teacher = classSnapshot.child("teacher").getValue(String.class);
                    String room = classSnapshot.child("room").getValue(String.class);

                    // Check the classes ref if the peer is existent
                    boolean shouldAdd = true;
                    for (DataSnapshot peerSnapshot: classSnapshot.child("peers").getChildren()) {
                        if (peerSnapshot.getKey().equals(targetUserId))
                            shouldAdd = false;
                    }


                    if (shouldAdd)
                        mClassList.add(new ClassItem(icon, title, teacher, room, occurrenceList, timeInList, timeInAltList, timeOutList, timeOutAltList, periodsList));
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
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.check);
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
        User user = (User) intent.getSerializableExtra("user");
        targetUserId = user.id;

        name = user.name;
        nameView.setText(name);
        ((TextView) findViewById(R.id.header)).setText
                (getString(R.string.whichClasses, name));
        ((TextView) findViewById(R.id.header)).setTextColor(textColor);

        flavour = user.flavour;
        flavourView.setText(flavour);

        iconUri = user.icon.replace("icon", targetUserId);
        // Check if the icon points to an existing file
        // First check if the icon uses a default drawable or from the storage
        if (!iconUri.contains("art_")) {
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


        // Set the listview mClassAdapter
        mScheduleAdapter = new CheckScheduleAdapter(this, R.layout.list_item_check_schedule, mScheduleList);
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
                        String selfFlavour = dataSnapshot.child("flavour").getValue(String.class);

                        requestsRef.child("nickname").setValue(selfName);
                        requestsRef.child("icon").setValue(selfIcon);
                        requestsRef.child("flavour").setValue(selfFlavour);

                        Toast.makeText(AddPeerActivity.this, getString(R.string.requestSent, name), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddPeerActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        // Add the selected classes into an array
        ArrayList<ClassItem> selectedClasses = new ArrayList<>();
        for (int i = 0; i < listView.getCount(); i++)
            if (((CheckBox) getViewByPosition(i, listView).findViewById(R.id.check)).isChecked()) {
                selectedClasses.add(mClassList.get(i));
            }

        // Finally, add the classes to the cloud
        DatabaseReference classesRef = requestsRef.child("classes");
        for (int i = 0; i < selectedClasses.size(); i++) {
            ClassItem selectedClass = selectedClasses.get(i);
            DatabaseReference classTitleRef = classesRef.child(selectedClass.title);

            classTitleRef.child("icon").setValue(selectedClass.icon);
        }
    }

    private void getAllClassesArray() {
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("classes");

        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                findViewById(R.id.header).setVisibility(View.VISIBLE);
                if (dataSnapshot.getChildrenCount() == 0) {
                    findViewById(R.id.splash).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.textView1)).setText(getString(R.string.whichClassesNone1, name));
                } else findViewById(R.id.splash).setVisibility(View.GONE);

                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    String icon = classSnapshot.child("icon").getValue(String.class);
                    String title = classSnapshot.getKey();
                    String teacher = classSnapshot.child("teacher").getValue(String.class);
                    String room = classSnapshot.child("room").getValue(String.class);

                    boolean shouldAdd = true;
                    for (DataSnapshot peerSnapshot: classSnapshot.child("peers").getChildren()) {
                        if (peerSnapshot.getKey().equals(targetUserId))
                            shouldAdd = false;
                    }

                    if (shouldAdd)
                        mScheduleList.add(new MatchingClass(icon, title, "", ""));
                    mScheduleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ((TextView) findViewById(R.id.header)).setText(getString(R.string.check_internet));
                findViewById(R.id.header).setVisibility(View.VISIBLE);
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
