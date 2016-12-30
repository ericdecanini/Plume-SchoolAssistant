package com.pdt.plume;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    // Arrays and Lists
    ArrayList<String> userIdList = new ArrayList<>();
    ArrayList<String> userNameList = new ArrayList<>();
    ArrayList<String> userIconList = new ArrayList<>();
    ArrayList<String> userFlavourList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        DbHelper dbHelper = new DbHelper(this);

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Inflate the list
        setRequestsListAdapater();
        dbHelper.updateRequestsInDb();

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
            requestsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
        PeerAdapter adapter = new PeerAdapter(this, R.layout.list_item_peer, peerArrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(RequestsActivity.this, AcceptPeerActivity.class);
                intent.putExtra("requestingUserId", userIdList.get(i));
                intent.putExtra("icon", userIconList.get(i));
                intent.putExtra("name", userNameList.get(i));
                intent.putExtra("flavour", userFlavourList.get(i));
                startActivity(intent);
            }
        });
    }

}
