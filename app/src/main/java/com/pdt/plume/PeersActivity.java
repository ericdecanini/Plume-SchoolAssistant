package com.pdt.plume;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

public class PeersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers);

        // Inflate the listview
        final DbHelper helper = new DbHelper(this);
        ListView listView = (ListView) findViewById(R.id.listView);
        PeerAdapter adapter = new PeerAdapter(this, R.layout.list_item_peer, helper.getPeersDataArray(this));
        listView.setAdapter(adapter);

        // Set the listener of the listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = helper.getPeersData(PeersActivity.this);
                for (int n = 0; n < cursor.getCount(); n++) {
                    cursor.moveToPosition(n);
                    String uid = cursor.getString(cursor.getColumnIndex(DbContract.PeersEntry.COLUMN_ID));
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

}
