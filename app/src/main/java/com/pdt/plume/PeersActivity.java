package com.pdt.plume;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.pdt.plume.data.DbHelper;

public class PeersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers);

        DbHelper helper = new DbHelper(this);
        ListView listView = (ListView) findViewById(R.id.listView);
        PeerAdapter adapter = new PeerAdapter(this, R.layout.list_item_peer, helper.getPeersDataArray());
        listView.setAdapter(adapter);
    }

}
