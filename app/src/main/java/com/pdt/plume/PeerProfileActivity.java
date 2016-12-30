package com.pdt.plume;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PeerProfileActivity extends AppCompatActivity {

    String LOG_TAG = PeerProfileActivity.class.getSimpleName();

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId, uid, profileName, profileIcon, profileFlavour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get reference to the views
        TextView nameView = (TextView) findViewById(R.id.name);
        TextView flavourView = (TextView) findViewById(R.id.flavour);
        ImageView iconView = (ImageView) findViewById(R.id.icon);

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

        // Set the data temporarily
        nameView.setText(profileName);
        iconView.setImageURI(Uri.parse(profileIcon));
        flavourView.setText(profileFlavour);

    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("parent", AcceptPeerActivity.class.getSimpleName());
        startActivity(intent);
        finish();
    }
}
