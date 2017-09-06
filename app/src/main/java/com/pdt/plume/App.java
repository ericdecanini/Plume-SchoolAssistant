package com.pdt.plume;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.pdt.plume.services.ActiveNotificationService;


public class App extends Application {

    String LOG_TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        startService(new Intent(this, ActiveNotificationService.class));

        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            String mUserId = mFirebaseUser.getUid();
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("requests")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.v(LOG_TAG, "onChildAdded");
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            Log.v(LOG_TAG, "onChildChanged");
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            Log.v(LOG_TAG, "onChildRemoved");
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            Log.v(LOG_TAG, "onChildMoved");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.v(LOG_TAG, "onCancelled");
                        }
                    });
        }
    }

}
