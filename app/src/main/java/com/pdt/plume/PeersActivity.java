package com.pdt.plume;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.util.ArrayList;

import static com.pdt.plume.R.id.listView;

public class PeersActivity extends AppCompatActivity {

    String LOG_TAG = PeersActivity.class.getSimpleName();
    ArrayList<Peer> arrayList = new ArrayList<>();
    PeerAdapter adapter = null;
    ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers);

        // Inflate the listview
        final DbHelper helper = new DbHelper(this);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new PeerAdapter(this, R.layout.list_item_peer, arrayList);
        listView.setAdapter(adapter);
        getPeersArrayData();

        // Set the listener of the listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = helper.getPeersData(PeersActivity.this);
                for (int n = 0; n < cursor.getCount(); n++) {
                    cursor.moveToPosition(n);
                    String uid = cursor.getString(cursor.getColumnIndex(DbContract.PeersEntry.COLUMN_UID));
                    String name = cursor.getString(cursor.getColumnIndex(DbContract.PeersEntry.COLUMN_NAME));
                    String iconUri = cursor.getString(cursor.getColumnIndex(DbContract.PeersEntry.COLUMN_ICON));
                    String flavour = cursor.getString(cursor.getColumnIndex(DbContract.PeersEntry.COLUMN_FLAVOUR));

                    // Make the intent to the profile activity
                    Intent intent = new Intent(PeersActivity.this, PeerProfileActivity.class);
                    intent.putExtra("uid", uid)
                            .putExtra("name", name)
                            .putExtra("icon", iconUri)
                            .putExtra("flavour", flavour);
                    startActivity(intent);
                }
            }
        });
    }

    private void getPeersArrayData() {
        // Get Array data from Firebase
        final ArrayList<Peer> arrayList = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();
        DatabaseReference peersRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("peers");
        peersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.v(LOG_TAG, "onChildAdded");
                String iconUri = dataSnapshot.child("icon").getValue(String.class);
                String name = dataSnapshot.child("nickname").getValue(String.class);
                Log.v(LOG_TAG, "IconUri: " + iconUri);
                Log.v(LOG_TAG, "Name: " + name);
                arrayList.add(new Peer(iconUri, name));
                PeerAdapter adapter = new PeerAdapter(PeersActivity.this, R.layout.list_item_peer, arrayList);
                listView.setAdapter(adapter);
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

}
