package com.pdt.plume;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.io.Serializable;
import java.util.ArrayList;

import static com.pdt.plume.R.bool.isLandscape;
import static com.pdt.plume.R.id.flavour;

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
    ArrayList<Peer> arrayList = new ArrayList<>();
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) savedInstanceState.clear();

        if (!getResources().getBoolean(R.bool.isTablet)) setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_peer_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (!getResources().getBoolean(R.bool.isTablet)) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get reference to the views
        TextView nameView = (TextView) findViewById(R.id.name);
        TextView flavourView = (TextView) findViewById(flavour);
        final ImageView iconView = (ImageView) findViewById(R.id.icon);

        // Initialise the theme
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);
        int backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));
        findViewById(R.id.activity_people).setBackgroundColor(backgroundColor);
        Color.colorToHSV(backgroundColor, hsv);
        hsv[2] *= 0.9f;
        int darkBackgroundColor = Color.HSVToColor(hsv);
        int textColor = preferences.getInt(getString(R.string.KEY_THEME_TITLE_COLOUR), getResources().getColor(R.color.gray_900));

        ((TextView) findViewById(R.id.textView2)).setTextColor(textColor);
        ((TextView) findViewById(R.id.textView1)).setTextColor(textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }

        if (getResources().getBoolean(R.bool.isTablet)) {
            if (getResources().getBoolean(isLandscape)) {
                findViewById(R.id.cardview).setBackgroundColor(backgroundColor);
                findViewById(R.id.activity_people).setBackgroundColor(darkBackgroundColor);
                findViewById(R.id.gradient_overlay).setBackgroundColor(mPrimaryColor);
            } else {
                findViewById(R.id.activity_people).setBackgroundColor(backgroundColor);
                getSupportActionBar().setElevation(0f);
            }
            findViewById(R.id.extended_appbar).setBackgroundColor(mPrimaryColor);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        } else {
            findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);
        }


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
        profileName = intent.getStringExtra("title");
        profileIcon = intent.getStringExtra("icon");
        profileFlavour = intent.getStringExtra("flavour");

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.TEMP_MANAGING_PEER), uid)
                .apply();

        // Set the key data
        nameView.setText(profileName);
        flavourView.setText(profileFlavour);
        // Check if the icon points to an existing file
        // First check if the icon uses a default drawable or from the storage
        if (!profileIcon.contains("android.resource://com.pdt.plume")) {
            String[] iconUriSplit = profileIcon.split("/");
            File file = new File(getFilesDir(), iconUriSplit[iconUriSplit.length - 1]);
            iconView.setImageURI(Uri.parse(profileIcon));
            if (file.exists()) {
                iconView.setImageURI(Uri.parse(profileIcon));
            } else {
                // File doesn't exist: Download from storage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference iconRef = storageRef.child(mUserId).child("/icon");

                file = new File(getFilesDir(), "icon.jpg");
                profileIcon = Uri.fromFile(file).toString();
                FirebaseDatabase.getInstance().getReference()
                        .child("users").child(uid).child("icon")
                        .setValue(profileIcon);

                iconRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        iconView.setImageURI(Uri.parse(profileIcon));
                    }
                });
            }
        } else {
            iconView.setImageURI(Uri.parse(profileIcon));
        }


        // Initialise and inflate the listview
        listView = (ListView) findViewById(R.id.listView);
        adapter = new PeerAdapter(PeerProfileActivity.this, R.layout.list_item_peer, arrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(PeerProfileActivity.this, ScheduleDetailActivity.class);
                intent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), arrayList.get(i).peerName);
                intent.putExtra("icon", arrayList.get(i).peerIcon);
                intent.putExtra(getString(R.string.INTENT_FLAG_NO_TRANSITION), true);
                startActivity(intent);
            }
        });

        // Check if MANAGE_CLASSES mode must be instantiated
        querySharedClasses();

        // Initialise the fab
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User(profileIcon, profileName, profileFlavour, uid);
                Intent intent = new Intent(PeerProfileActivity.this, AddPeerActivity.class);
                intent.putExtra("user", (Serializable) user);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_peer, menu);
        return true;
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

                                // Remove the peer from each class
                                final DatabaseReference classesRef = usersRef.child(mUserId).child("classes");
                                classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                                            DatabaseReference peerRef = classesRef.child(classSnapshot.getKey())
                                                    .child("peers").child(uid);
                                            if (peerRef != null)
                                                peerRef.removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                final DatabaseReference classesRef1 = usersRef.child(uid).child("classes");
                                classesRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                                            DatabaseReference peerRef = classesRef1.child(classSnapshot.getKey())
                                                    .child("peers").child(mUserId);
                                            if (peerRef != null)
                                                peerRef.removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                Intent intent = new Intent(PeerProfileActivity.this, PeopleActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();
                return true;
        }
        return false;
    }

    private void querySharedClasses() {
        // Get Array data from Firebase
        getWindow().getDecorView().findViewById(android.R.id.content).setEnabled(false);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("peers").child(uid).child("classes");
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getWindow().getDecorView().findViewById(android.R.id.content).setEnabled(true);
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String title = userSnapshot.getKey();
                    String iconUri = userSnapshot.child("icon").getValue(String.class);
                    Peer schedule = new Peer(iconUri, title, "");
                    arrayList.add(schedule);
                    adapter.notifyDataSetChanged();
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    ((TextView) findViewById(R.id.textView1)).setText(getString(R.string.splash_no_classes_shared));
                    findViewById(R.id.textView2).setVisibility(View.GONE);
                    findViewById(R.id.splash).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.splash).setVisibility(View.GONE);
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                }

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
