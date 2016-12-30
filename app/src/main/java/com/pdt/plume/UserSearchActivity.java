package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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

    // UI Data
    PeerAdapter adapter;

    // Arrays
    ArrayList<Peer> searchResults = new ArrayList<>();
    ArrayList<String> searchResultIDs = new ArrayList<>();

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        // Get references to the views
        LinearLayout toolbar = (LinearLayout) findViewById(R.id.toolbar);
        ImageView homeButton = (ImageView) findViewById(R.id.home);
        final EditText searchBar = (EditText) findViewById(R.id.search_bar);
        final ImageView clearButton = (ImageView) findViewById(R.id.clear);

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
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    clearButton.setVisibility(View.GONE);
                }
                else {
                    clearButton.setVisibility(View.VISIBLE);
                    ASyncUserSearch(charSequence.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

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
        } else loadLogInView();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor  = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        toolbar.setBackgroundColor(mPrimaryColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }

    }

    private void ASyncUserSearch(String text)  {
        // Query a list by the entered text as the nickname
        searchResults.clear();
        searchResultIDs.clear();
        Query queryRef = mDatabase.child("users").orderByChild("nickname").equalTo(text);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Log.v(LOG_TAG, "User key: " + child.getKey());
                    Log.v(LOG_TAG, "User ref: " + child.getRef().toString());
                    Log.v(LOG_TAG, "User val: " + child.getValue().toString());

                    // Get the data and add them as a new list object
                    String nicknameResult = child.child("nickname").getValue(String.class);
                    String iconResultUri = child.child("icon").getValue(String.class);
                    searchResults.add(new Peer(iconResultUri, nicknameResult));
                    searchResultIDs.add(child.getRef().getKey());
                }
                Log.v(LOG_TAG, "Search Results: " + searchResults.size());
                adapter.notifyDataSetChanged();
                Log.v(LOG_TAG, "Adapter Count: " + adapter.getCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
