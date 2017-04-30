package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.R.attr.editable;

public class UserSearchActivity extends AppCompatActivity {

    // General Variables
    String LOG_TAG = UserSearchActivity.class.getSimpleName();

    // Firebase Variables
    DatabaseReference mDatabase;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;

    // UI Elements
    ListView listView;
    ProgressBar spinner;
    EditText searchBar;
    TextWatcher textWatcher;

    // UI Data
    PeerAdapter adapter;

    // Arrays
    ArrayList<Peer> searchResults = new ArrayList<>();
    ArrayList<String> searchResultIDs = new ArrayList<>();
    ArrayList<String> userNames = new ArrayList<>();
    ArrayList<Peer> userList = new ArrayList<>();
    ArrayList<String> userIDs = new ArrayList<>();

    // Theme Variables
    int mPrimaryColor, mDarkColor, mSecondaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        // Get references to the views
        LinearLayout toolbar = (LinearLayout) findViewById(R.id.toolbar);
        ImageView homeButton = (ImageView) findViewById(R.id.home);searchBar = (EditText) findViewById(R.id.search_bar);
        final ImageView clearButton = (ImageView) findViewById(R.id.clear);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new PeerAdapter(this, R.layout.list_item_search_result, searchResults);
        listView.setAdapter(adapter);

        // Set the listeners of the views
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(UserSearchActivity.this, AddPeerActivity.class);
                intent.putExtra("id", searchResultIDs.get(i));
                startActivity(intent);
            }
        });

        // This listener sets the behavior of the clear button's visibility
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.toString().length() == 0) {
//                    clearButton.setVisibility(View.GONE);
//                    searchResults.clear();
//                    searchResultIDs.clear();
//                    adapter.notifyDataSetChanged();
//                }
//                else {
//                    clearButton.setVisibility(View.VISIBLE);
//                    ASyncUserSearch(charSequence.toString());
//                }
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 0) {
                    clearButton.setVisibility(View.GONE);
                    searchResults.clear();
                    searchResultIDs.clear();
                    adapter.notifyDataSetChanged();
                }
                else {
                    clearButton.setVisibility(View.VISIBLE);
                    ASyncUserSearch(charSequence.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
//                if (editable.toString().length() == 0) {
//                    clearButton.setVisibility(View.GONE);
//                    searchResults.clear();
//                    searchResultIDs.clear();
//                    adapter.notifyDataSetChanged();
//                }
//                else {
//                    clearButton.setVisibility(View.VISIBLE);
//                    ASyncUserSearch(editable.toString());
//                }
            }
        };
        searchBar.addTextChangedListener(textWatcher);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBar.setText("");
            }
        });

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mUserId = mFirebaseUser.getUid();

            // Store the nicknames of all users
            FirebaseDatabase.getInstance().getReference().child("users")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                                String name = userSnapshot.child("nickname").getValue(String.class);
                                String icon = userSnapshot.child("icon").getValue(String.class);
                                userNames.add(name);
                                userList.add(new Peer(icon, name));
                                userIDs.add(userSnapshot.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        } else loadLogInView();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor  = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));

        toolbar.setBackgroundColor(mPrimaryColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        searchBar.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));

    }

    private void ASyncUserSearch(final String text)  {
        searchResults.clear();
        searchResultIDs.clear();
        for (int i = 0; i < userNames.size(); i++) {
            if (searchResultApproved(userNames.get(i), text, 0)) {
                searchResults.add(userList.get(i));
                searchResultIDs.add(userIDs.get(i));
            }
        }
        for (int i = 0; i < userNames.size(); i++) {
            if (searchResultApproved(userNames.get(i), text, 1) && !searchResultIDs.contains(userIDs.get(i))) {
                searchResults.add(userList.get(i));
                searchResultIDs.add(userIDs.get(i));
            }
        }
        for (int i = 0; i < userNames.size(); i++) {
            if (searchResultApproved(userNames.get(i), text, 2) && !searchResultIDs.contains(userIDs.get(i))) {
                searchResults.add(userList.get(i));
                searchResultIDs.add(userIDs.get(i));
            }
        }
        for (int i = 0; i < userNames.size(); i++) {
            if (searchResultApproved(userNames.get(i), text, 3) && !searchResultIDs.contains(userIDs.get(i))) {
                searchResults.add(userList.get(i));
                searchResultIDs.add(userIDs.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }

    boolean searchResultApproved(String nickname, String text, int l) {
        String n = nickname.toLowerCase();
        String t = text.toLowerCase();
        if (n.equals(t) && l == 0) {
            Log.v(LOG_TAG, "returning 1");
            return true;
        }
        if (n.startsWith(t) && l == 1) {
            Log.v(LOG_TAG, "returning 2, n = " + n + ", t = " + t);
            return true;
        }

        String[] splitN = n.split(" ");
        String[] splitT = t.split(" ");
        for (int i = 0; i < splitN.length; i++) {
            for (int i1 = 0; i1 < splitT.length; i1++) {
                if (splitN[i].equals(splitT[i1]) && l == 2) {
                    Log.v(LOG_TAG, "returning 3");
                    return true;
                }
                if (splitN[i].startsWith(splitT[i1]) && l == 3) {
                    Log.v(LOG_TAG, "returning 4");
                    return true;
                }
            }
        }

        return false;
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        searchBar.removeTextChangedListener(textWatcher);
        startActivity(intent);
    }

}