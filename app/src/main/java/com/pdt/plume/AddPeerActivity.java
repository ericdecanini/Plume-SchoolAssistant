package com.pdt.plume;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pdt.plume.data.DbHelper;

import static android.R.attr.data;

public class AddPeerActivity extends AppCompatActivity {

    String LOG_TAG = AddPeerActivity.class.getSimpleName();
    String peerID;
    String peerIconUri = "";
    String peerName = "Raj";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_peer);

        Button confirmButton = (Button) findViewById(R.id.button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (peerID != null) {
                    DbHelper helper = new DbHelper(AddPeerActivity.this);
                    helper.insertPeer(peerID, peerIconUri, peerName);
                    Toast.makeText(AddPeerActivity.this, peerName + " " + getString(R.string.added), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddPeerActivity.this, "Peer not added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        initialiseQRScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                finish();
                Log.d(LOG_TAG, "Scan cancelled");
            } else {
                peerID =  result.getContents();
                Log.d(LOG_TAG, "Scanned " + peerID);
                // TODO: Get data from scanned QR code
                // TODO: May require database access and loading
                // TODO: Have fallback for manually adding peer
            }
        } else {
            if (requestCode == 0) {
                if (resultCode == RESULT_OK) {
                    String contents = data.getStringExtra("SCAN_RESULT");
                    Log.v(LOG_TAG, "Contents: " + contents);
                }
                if(resultCode == RESULT_CANCELED){
                    //handle cancel
                }
            } else super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void initialiseQRScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(getString(R.string.qrScanPrompt));
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

}
