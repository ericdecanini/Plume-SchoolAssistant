package com.pdt.plume;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.R.attr.data;
import static android.os.Build.ID;
import static com.pdt.plume.R.string.B;
import static com.pdt.plume.StaticRequestCodes.REQUEST_IMAGE_GET_ICON;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ID;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT;
import static com.pdt.plume.StaticRequestCodes.REQUEST_PERMISSION_MANAGE_DOCUMENTS;
import static com.pdt.plume.StaticRequestCodes.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE;
import static com.pdt.plume.StaticRequestCodes.REQUEST_SCAN_QR_CODE;

public class PeopleActivity extends AppCompatActivity
        implements IconPromptDialog.iconDialogListener,
        NameDialogFragment.onNameSelectedListener,
        FlavourDialogFragment.onFlavourSelectedListener {

    String LOG_TAG = PeopleActivity.class.getSimpleName();

    // UI Variables
    ImageView selfIconView;
    TextView selfNameView, flavourView;

    // UI Data
    String selfIconUri, selfName, flavour;
    public static String defaultIconUri = "android.resource://com.pdt.plume/drawable/art_profile_default";

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    // Firebase Variables
    private DatabaseReference mDatabase;
    private String mUserId;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseStorage mFirebaseStorage;
    boolean loggedIn = false;
    MenuItem logInOut;

    // Peers List Variables
    ArrayList<Peer> arrayList = new ArrayList<>();
    PeerAdapter adapter = null;
    ListView listView = null;

    // Item variables
    ArrayList<String> uidList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> flavourList = new ArrayList<>();
    ArrayList<String> iconList = new ArrayList<>();

    // Dialog item arrays
    private Integer[] mThumbIds = {
            R.drawable.art_profile_default,
            R.drawable.art_profile_uniform,
            R.drawable.art_profile_uniform_female,
            R.drawable.art_profile_blazer,
            R.drawable.art_profile_blazer_female,
            R.drawable.art_profile_mustache,
            R.drawable.art_profile_pandakun
    };

    private CharSequence[] addPeerMethodsArray = {"", ""};
    boolean isTablet;
    boolean isLandscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Initialise AddPeerMethodsArray and mUserId
        addPeerMethodsArray[0] = getString(R.string.AddByUsername);
        addPeerMethodsArray[1] = getString(R.string.ScanQRCode);


        // Get references to the views
        selfIconView = (ImageView) findViewById(R.id.icon);
        selfNameView = (TextView) findViewById(R.id.name);
        flavourView = (TextView) findViewById(R.id.flavour);
        ImageView QRCodeView = (ImageView) findViewById(R.id.qr);
        TextView addPeersTextview = (TextView) findViewById(R.id.add_peer);
        listView = (ListView) findViewById(R.id.listView);

        // Initialise the theme
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);
        addPeersTextview.setTextColor(mPrimaryColor);

        // Set the click listeners of the views
        // PROFILE VIEWS
        selfIconView.setOnClickListener(showIconDialog());
        selfNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NameDialogFragment fragment = NameDialogFragment.newInstance(selfName);
                Bundle args = new Bundle();
                Log.v(LOG_TAG, "SelfName: " + selfName);
                args.putString("name", selfName);
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), "dialog");
            }
        });
        flavourView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlavourDialogFragment fragment = FlavourDialogFragment.newInstance(flavour);
                Bundle args = new Bundle();
                args.putString("flavour", flavour);
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), "dialog");
            }
        });

        // ACTION BUTTONS
        addPeersTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(PeopleActivity.this).
                        setItems(addPeerMethodsArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Popup menu click listener
                                Intent intent;
                                switch (i) {
                                    case 0:
                                        // Add by Username
                                        intent = new Intent(PeopleActivity.this, UserSearchActivity.class);
                                        startActivity(intent);
                                        return;
                                    case 1:
                                        // Scan QR Code
                                        try {
                                            intent = new Intent("com.google.zxing.client.android.SCAN");
                                            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
                                            startActivityForResult(intent, REQUEST_SCAN_QR_CODE);
                                        } catch (Exception e) {
                                            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                                            startActivity(marketIntent);
                                        }
                                        return;
                                }
                            }
                        }).show();
            }
        });

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();


        // Direct the user to the sign in page if he isn't logged in
        if (mFirebaseUser == null) {
            loadLogInView();
            return;
        } else mUserId = mFirebaseUser.getUid();

        // If there is previously set data in shared preferences, set it accordingly
        // If new data is found in the cloud database, this data will be replaced once loaded
        String savedName = preferences.getString(getString(R.string.KEY_PREFERENCES_SELF_NAME), getString(R.string.yourNameHere));
        flavour = preferences.getString(getString(R.string.KEY_PREFERENCES_FLAVOUR), getString(R.string.whats_up));
        selfNameView.setText(savedName);
        selfName = savedName;
        flavourView.setText(flavour);
        setImageUri();

        // Retrieve the data from the cloud database
        // and set the listener for changes in the cloud
        if (mFirebaseUser != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(mUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // After setting the self data to the saved key values in the phone
                    // The program will check for updated data in the cloud
                    String nicknameData = dataSnapshot.child("nickname").getValue(String.class);
                    String flavourData = dataSnapshot.child("flavour").getValue(String.class);

                    if (flavourData == null) {
                        flavourData = flavour;
                    }

                    // Data snapshot is the name
                    if (nicknameData != null) {
                        selfNameView.setText(nicknameData);
                        selfName = nicknameData;
                        flavourView.setText(flavourData);
                        flavour = flavourData;
                        // Save the data to shared preferences
                        preferences.edit()
                                .putString(getString(R.string.KEY_PREFERENCES_SELF_NAME), nicknameData)
                                .putString(getString(R.string.KEY_PREFERENCES_FLAVOUR), flavourData)
                                .apply();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        // Generate the QR code and set the ImageView to such
        Bitmap QRCodeBitmap = generateQRCode(mUserId);
        QRCodeView.setImageBitmap(QRCodeBitmap);

        // Initialise the Peers listview
        adapter = new PeerAdapter(this, R.layout.list_item_peer, arrayList);
        listView.setAdapter(adapter);
        getPeersArrayData();

        // Set the listener of the listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String uid = uidList.get(i);
                String name = nameList.get(i);
                String flavour = flavourList.get(i);
                String iconUri = iconList.get(i);
                // Make the intent to the profile activity
                Intent intent = new Intent(PeopleActivity.this, PeerProfileActivity.class);
                intent.putExtra("uid", uid)
                        .putExtra("name", name)
                        .putExtra("icon", iconUri)
                        .putExtra("flavour", flavour);
                startActivity(intent);
            }
        });

    }

    private void setImageUri() {
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String iconUri = dataSnapshot.child("icon").getValue(String.class);
                if (iconUri == null) {
                    iconUri = defaultIconUri;
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).setValue(iconUri);
                }

                // First check if the icon uses a default drawable or from the storage
                if (!iconUri.contains("android.resource://com.pdt.plume")) {
                    String[] iconUriSplit = iconUri.split("/");
                    File file = new File(getFilesDir(), iconUriSplit[iconUriSplit.length - 1]);
                    selfIconView.setImageURI(Uri.parse(iconUri));
                    if (file.exists()) {
                        selfIconView.setImageURI(Uri.parse(iconUri));
                        selfIconUri = iconUri;
                    } else {
                        // File doesn't exist: Download from storage
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference iconRef = storageRef.child(mUserId).child("/icon");

                        file = new File(getFilesDir(), "icon.jpg");
                        selfIconUri = Uri.fromFile(file).toString();
                        userRef.child("icon").setValue(selfIconUri);

                        iconRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                selfIconView.setImageURI(Uri.parse(selfIconUri));
                            }
                        });
                    }
                } else {
                    selfIconUri = iconUri;
                    selfIconView.setImageURI(Uri.parse(selfIconUri));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_people, menu);
        logInOut = menu.findItem(R.id.action_logout);
        if (mFirebaseUser != null)
            loggedIn = true;
        else loggedIn = false;
        if (loggedIn)
            logInOut.setTitle(getString(R.string.action_logout));
        else logInOut.setTitle(getString(R.string.action_login));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_invite) {
            ShareDialog dialog = ShareDialog.newInstance();
            dialog.show(getSupportFragmentManager(), "dialog");
            return true;
        }

        if (id == R.id.action_logout) {
            if (loggedIn)
                logOut();
            else {
                loadLogInView();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Execute upload intent
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(intent, REQUEST_IMAGE_GET_ICON);
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.dialog_permission_rationale_take_photo))
                            .setPositiveButton(getString(R.string.ok), null)
                            .show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Custom Icon Upload
        if (requestCode == REQUEST_IMAGE_GET_ICON && resultCode == RESULT_OK) {
            Uri dataUri = data.getData();
            selfIconView.setImageURI(dataUri);

            // Save the icon uri
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(getString(R.string.KEY_PREFERENCES_SELF_ICON), dataUri.toString())
                    .apply();

            // Copy the file to the app's directory
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(dataUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String filename = "icon.jpg";
            byte[] data1 = new byte[0];
            try {
                data1 = getBytes(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File(getFilesDir(), filename);
            FileOutputStream outputStream;
            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(data1);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Image compression
            Bitmap decodedBitmap = decodeFile(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            decodedBitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
            byte[] bitmapData = baos.toByteArray();
            file.delete();
            File file1 = new File(getFilesDir(), filename);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file1);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Get the uri of the file so it can be saved
            dataUri = Uri.fromFile(file1);

            selfIconView.setImageURI(dataUri);
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mUserId).child("icon")
                    .setValue(dataUri.toString());

            // Upload the custom icon and photos
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference iconRef = storageRef.child(mUserId + "/icon");
            selfIconView.setDrawingCacheEnabled(true);
            selfIconView.buildDrawingCache();
            Bitmap bitmap = loadBitmapFromView(selfIconView);
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos1);
            byte[] data2 = baos1.toByteArray();

            UploadTask uploadTask = iconRef.putBytes(data2);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO: Handle unsuccessful uploads
                    Log.v(LOG_TAG, "Upload Failed: " + e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // TODO: Handle successful upload if any action is required
                    Log.v(LOG_TAG, "Upload Success: " + taskSnapshot.getDownloadUrl());
                }
            });
        }

        if (requestCode == REQUEST_SCAN_QR_CODE) {
            if (resultCode == RESULT_OK) {
                final String contents = data.getStringExtra("SCAN_RESULT");
                // Check if the QR Code is invalid
                if (contents.contains(".") || contents.contains("#")
                        || contents.contains("&") || contents.contains("[")
                        || contents.contains("]")) {
                    new AlertDialog.Builder(PeopleActivity.this).setTitle(getString(R.string.error))
                            .setMessage(getString(R.string.invalid_qr_message))
                            .setPositiveButton(getString(R.string.ok), null)
                            .show();
                    return;
                }

                // Check if the QR Code is a user's Firebase ID
                // Then get the id and send the intent to AddPeerActivity
                final DatabaseReference peerRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(contents);

                peerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            if (contents.equals(mUserId)) {
                                new AlertDialog.Builder(PeopleActivity.this).setTitle(getString(R.string.nice_try_title))
                                        .setMessage(getString(R.string.nice_try_message))
                                        .setPositiveButton(getString(R.string.ok), null)
                                        .show();
                                return;
                            }

                            Intent intent = new Intent(PeopleActivity.this, AddPeerActivity.class);
                            intent.putExtra("id", contents);
                            startActivity(intent);

                        } else {
                            new AlertDialog.Builder(PeopleActivity.this).setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.invalid_qr_message))
                                    .setPositiveButton(getString(R.string.ok), null)
                                    .show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            if (resultCode == RESULT_CANCELED) {
                //handle cancel
            }
        }
    }

    private Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 300;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    @Override
    public void onNameSelected(String name) {
        // Set the text on the TextView
        selfName = name;
        selfNameView.setText(name);

        // Save the name to SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.KEY_PREFERENCES_SELF_NAME), name)
                .apply();

        // Save the name to the cloud database
        mDatabase.child("users").child(mUserId).child("nickname").setValue(name);
    }

    @Override
    public void onFlavourSelected(String flavour) {
        // Set the flavour on the TextView
        this.flavour = flavour;
        flavourView.setText(flavour);

        // Save the name to SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.KEY_PREFERENCES_FLAVOUR), flavour)
                .apply();

        // Save the name to the cloud database
        mDatabase.child("users").child(mUserId).child("flavour").setValue(flavour);
    }

    @Override
    public void OnIconListItemSelected(int item) {
        switch (item) {
            case 0:
                showBuiltInIconsDialog();
                break;
            case 1:
                sendCustomIconIntent();
                break;
        }
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void logOut() {
        // Disable any notifications
        // CANCEL TASK REFERENCES
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid()).child("tasks");
        tasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the data
                String title = dataSnapshot.child("title").getValue(String.class);
                String icon = dataSnapshot.child("icon").getValue(String.class);

                // Rebuild the notification
                final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(PeopleActivity.this);
                Bitmap largeIcon = null;
                try {
                    largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                        .setBackground(largeIcon);

                Intent contentIntent = new Intent(PeopleActivity.this, TasksDetailActivity.class);
                contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(PeopleActivity.this);
                stackBuilder.addParentStack(TasksDetailActivity.class);
                stackBuilder.addNextIntent(contentIntent);
                final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);
                builder.setContentIntent(contentPendingIntent)
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(getString(R.string.notification_message_reminder))
                        .setContentText(title)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL);

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(PeopleActivity.this, TaskNotificationPublisher.class);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(PeopleActivity.this, REQUEST_NOTIFICATION_ALARM,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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

        // CANCEL CLASS NOTIFICATIONS
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String weekNumber = preferences.getString(getString(R.string.KEY_WEEK_NUMBER), "0");
        DatabaseReference classesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mFirebaseUser.getUid()).child("classes");
        classesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the key data
                String title = dataSnapshot.getKey();
                String icon = dataSnapshot.child("icon").getValue(String.class);
                String message = getString(R.string.class_notification_message,
                        Integer.toString(preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0)));

                // Get the listed data
                ArrayList<Integer> timeins = new ArrayList<>();
                if (weekNumber.equals("0"))
                    for (DataSnapshot timeinSnapshot : dataSnapshot.child("timein").getChildren())
                        timeins.add(timeinSnapshot.getValue(int.class));
                else
                    for (DataSnapshot timeinaltSnapshot : dataSnapshot.child("timeinalt").getChildren())
                        timeins.add(timeinaltSnapshot.getValue(int.class));

                Calendar c = Calendar.getInstance();
                for (int i = 0; i < timeins.size(); i++) {
                    c.setTimeInMillis(timeins.get(i));

                    // Rebuild the notification
                    final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(PeopleActivity.this);
                    Bitmap largeIcon = null;
                    try {
                        largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                            .setBackground(largeIcon);

                    Intent contentIntent = new Intent(PeopleActivity.this, ScheduleDetailActivity.class);
                    if (mFirebaseUser != null)
                        contentIntent.putExtra("id", title);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(PeopleActivity.this);
                    stackBuilder.addParentStack(ScheduleDetailActivity.class);
                    stackBuilder.addNextIntent(contentIntent);
                    final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);

                    builder.setContentIntent(contentPendingIntent)
                            .setSmallIcon(R.drawable.ic_assignment)
                            .setColor(getResources().getColor(R.color.colorPrimary))
                            .setContentTitle(title)
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .extend(wearableExtender)
                            .setDefaults(Notification.DEFAULT_ALL);

                    Notification notification = builder.build();

                    Intent notificationIntent = new Intent(PeopleActivity.this, TaskNotificationPublisher.class);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, REQUEST_NOTIFICATION_ID);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                    final PendingIntent pendingIntent = PendingIntent.getBroadcast(PeopleActivity.this, REQUEST_NOTIFICATION_ALARM,
                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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

        // Reschedule all SQLite based Task Notifications
        DbHelper dbHelper = new DbHelper(PeopleActivity.this);
        Cursor tasksCursor = dbHelper.getTaskData();
        tasksCursor.moveToFirst();
        for (int i = 0; i < tasksCursor.getCount(); i++) {
            // Get the data
            tasksCursor.moveToPosition(i);
            String title = tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
            String icon = tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));
            long reminderDateMillis = tasksCursor.getLong(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_DATE));
            long reminderTimeSeconds = tasksCursor.getLong(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_REMINDER_TIME));
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(reminderDateMillis);
            int hour = (int) reminderTimeSeconds / 3600;
            int minute = (int) (reminderTimeSeconds - hour * 3600) / 60;
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            long notificationMillis = (c.getTimeInMillis());

            // Rebuild the notification
            final android.support.v4.app.NotificationCompat.Builder builder
                    = new NotificationCompat.Builder(PeopleActivity.this);
            Bitmap largeIcon = null;
            try {
                largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender
                    = new NotificationCompat.WearableExtender().setBackground(largeIcon);

            Intent contentIntent = new Intent(PeopleActivity.this, TasksDetailActivity.class);
            contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(PeopleActivity.this);
            stackBuilder.addParentStack(TasksDetailActivity.class);
            stackBuilder.addNextIntent(contentIntent);
            final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);
            builder.setContentIntent(contentPendingIntent)
                    .setSmallIcon(R.drawable.ic_assignment)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(getString(R.string.notification_message_reminder))
                    .setContentText(title)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .extend(wearableExtender)
                    .setDefaults(Notification.DEFAULT_ALL);

            Notification notification = builder.build();

            Intent notificationIntent = new Intent(PeopleActivity.this, TaskNotificationPublisher.class);
            notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
            notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast
                    (PeopleActivity.this, REQUEST_NOTIFICATION_ALARM,
                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (reminderDateMillis > 0)
                alarmManager.set(AlarmManager.RTC, new Date(notificationMillis).getTime(), pendingIntent);
        }
        tasksCursor.close();

        // Reschedule all SQLite based Class Notifications
        Cursor classesCursor = dbHelper.getCurrentDayScheduleDataFromSQLite(this);
        Calendar c = Calendar.getInstance();
        final int forerunnerTime = preferences.getInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), 0);
        for (int i = 0; i < classesCursor.getCount(); i++) {
            classesCursor.moveToPosition(i);
            final String title = classesCursor.getString(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TITLE));
            String icon = classesCursor.getString(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_ICON));
            int ID = classesCursor.getInt(classesCursor.getColumnIndex(DbContract.ScheduleEntry._ID));

            long timeInValue = classesCursor.getLong(classesCursor.getColumnIndex(DbContract.ScheduleEntry.COLUMN_TIMEIN));
            c = Calendar.getInstance();
            Calendar timeInCalendar = Calendar.getInstance();
            timeInCalendar.setTimeInMillis(timeInValue);
            c.set(Calendar.HOUR, timeInCalendar.get(Calendar.HOUR) - 1);
            c.set(Calendar.MINUTE, timeInCalendar.get(Calendar.MINUTE) - forerunnerTime);
            Calendar current = Calendar.getInstance();
            if (c.getTimeInMillis() < current.getTimeInMillis())
                c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
            c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) - forerunnerTime);

            c.setTimeInMillis(timeInValue);
            c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) - forerunnerTime);

            final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            Bitmap largeIcon = null;
            try {
                largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                    .setBackground(largeIcon);

            Intent contentIntent = new Intent(PeopleActivity.this, ScheduleDetailActivity.class);
            contentIntent.putExtra("_ID", ID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(PeopleActivity.this);
            stackBuilder.addParentStack(ScheduleDetailActivity.class);
            stackBuilder.addNextIntent(contentIntent);
            final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);

            final Calendar finalC = c;
            Palette.generateAsync(largeIcon, new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    builder.setContentIntent(contentPendingIntent)
                            .setSmallIcon(R.drawable.ic_assignment)
                            .setColor(getResources().getColor(R.color.colorPrimary))
                            .setContentTitle(title)
                            .setContentText(getString(R.string.class_notification_message, Integer.toString(forerunnerTime)))
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .extend(wearableExtender)
                            .setDefaults(Notification.DEFAULT_ALL);

                    Notification notification = builder.build();

                    Intent notificationIntent = new Intent(PeopleActivity.this, TaskNotificationPublisher.class);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 0);
                    notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                    final PendingIntent pendingIntent = PendingIntent.getBroadcast(PeopleActivity.this, REQUEST_NOTIFICATION_ALARM,
                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, finalC.getTimeInMillis(), pendingIntent);
                }
            });
        }
        classesCursor.close();

        // Execute the Sign Out Operation
        mFirebaseAuth.signOut();
        loggedIn = false;
        logInOut.setTitle(getString(R.string.action_login));
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private View.OnClickListener showIconDialog() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new IconPromptDialog();
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        };
    }

    // Custom Icon Upload Intent
    private void sendCustomIconIntent() {
        // Conduct permission check
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(intent, REQUEST_IMAGE_GET_ICON);
        }
    }

    private void showBuiltInIconsDialog() {
        // Prepare grid view
        GridView gridView = new GridView(this);
        final AlertDialog dialog;

        int[] builtinIcons = getResources().getIntArray(R.array.builtin_icons);
        List<Integer> mList = new ArrayList<>();
        for (int i = 1; i < builtinIcons.length; i++) {
            mList.add(builtinIcons[i]);
        }

        gridView.setAdapter(new BuiltInProfileIconsAdapter(this));
        gridView.setNumColumns(4);
        gridView.setPadding(0, 16, 0, 16);
        gridView.setGravity(Gravity.CENTER);
        // Set grid view to alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(gridView);
        builder.setTitle(getString(R.string.new_schedule_icon_builtin_title));
        dialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Set the image resource and save the URI as a global variable
                int resId = mThumbIds[position];
                selfIconView.setImageResource(resId);
                Resources resources = getResources();
                Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                        + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId));
                selfIconUri = drawableUri.toString();

                // Save the selected icon in SharedPreferences
                PreferenceManager.getDefaultSharedPreferences(PeopleActivity.this).edit()
                        .putString(getString(R.string.KEY_PREFERENCES_SELF_ICON), selfIconUri)
                        .apply();

                // Save the selected icon in the Cloud Database
                mDatabase.child("users").child(mUserId).child("icon").setValue(selfIconUri);

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private Bitmap generateQRCode(String FirebaseID) {
        //Find screen size
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        //Encode with a QR Code image
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(FirebaseID,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addPeer() {

    }

    private void getPeersArrayData() {
        // Get Array data from Firebase
        final ArrayList<Peer> arrayList = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();
        DatabaseReference peersRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("peers");
        peersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot peerSnapshot: dataSnapshot.getChildren()) {
                    final String name = peerSnapshot.child("nickname").getValue(String.class);
                    String flavour = peerSnapshot.child("flavour").getValue(String.class);
                    uidList.add(peerSnapshot.getKey());
                    nameList.add(name);
                    flavourList.add(flavour);
                    final String iconUri = peerSnapshot.child("icon").getValue(String.class);

                    // Check if iconUri points to a valid file
                    final File file = new File(getFilesDir(), iconUri);
                    if (file.exists() || iconUri.contains("android.resource://com.pdt.plume")) {
                        iconList.add(iconUri);
                        arrayList.add(new Peer(iconUri, name));
                        final PeerAdapter adapter = new PeerAdapter(PeopleActivity.this, R.layout.list_item_peer, arrayList);
                        listView.setAdapter(adapter);
                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                PopupMenu popupMenu = new PopupMenu(PeopleActivity.this, view);
                                popupMenu.getMenuInflater().inflate(R.menu.menu_peer, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        if (item.getItemId() == R.id.action_remove) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(PeopleActivity.this);
                                            builder.setMessage(getString(R.string.dialog_remove_peer, name))
                                                    .setNegativeButton(getString(R.string.cancel), null)
                                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            // Remove the peer from the peers tab
                                                            final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                                                                    .child("users");
                                                            usersRef.child(mUserId).child("peers").child(peerSnapshot.getKey()).removeValue();
                                                            usersRef.child(peerSnapshot.getKey()).child("peers").child(mUserId).removeValue();
                                                            arrayList.remove(i);
                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    })
                                                    .show();
                                        }
                                        return true;
                                    }
                                });
                                popupMenu.show();
                                return true;
                            }
                        });
                    } else {
                        // FILE DOESN'T EXIST: DOWNLOAD FROM CLOUD STORAGE
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference iconRef = storageRef.child(peerSnapshot.getKey()).child("icon");
                        iconRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // ADD THE LIST ITEM HERE
                                arrayList.add(new Peer(iconUri, name));
                                final PeerAdapter adapter = new PeerAdapter(PeopleActivity.this, R.layout.list_item_peer, arrayList);
                                listView.setAdapter(adapter);
                                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                    @Override
                                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        PopupMenu popupMenu = new PopupMenu(PeopleActivity.this, view);
                                        popupMenu.getMenuInflater().inflate(R.menu.menu_peer, popupMenu.getMenu());
                                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                if (item.getItemId() == R.id.action_remove) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(PeopleActivity.this);
                                                    builder.setMessage(getString(R.string.dialog_remove_peer, name))
                                                            .setNegativeButton(getString(R.string.cancel), null)
                                                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    // Remove the peer from the peers tab
                                                                    final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                                                                            .child("users");
                                                                    usersRef.child(mUserId).child("peers").child(peerSnapshot.getKey()).removeValue();
                                                                    usersRef.child(peerSnapshot.getKey()).child("peers").child(mUserId).removeValue();
                                                                    arrayList.remove(i);
                                                                    adapter.notifyDataSetChanged();
                                                                }
                                                            })
                                                            .show();
                                                }
                                                return true;
                                            }
                                        });
                                        popupMenu.show();
                                        return true;
                                    }
                                });

                            }
                        });
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
