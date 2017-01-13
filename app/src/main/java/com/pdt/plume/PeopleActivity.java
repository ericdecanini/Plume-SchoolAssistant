package com.pdt.plume;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.LinearLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.bitmap;
import static android.R.attr.data;

public class PeopleActivity extends AppCompatActivity
        implements IconPromptDialog.iconDialogListener,
        NameDialogFragment.onNameSelectedListener,
        FlavourDialogFragment.onFlavourSelectedListener {

    String LOG_TAG = PeopleActivity.class.getSimpleName();

    // TODO: Make custom icon upload ask for permission

    // UI Variables
    ImageView selfIconView;
    TextView selfNameView, flavourView;

    // UI Data
    String selfIconUri, selfName, flavour;
    private static String defaultIconUri = "android.resource://com.pdt.plume/drawable/ic_person_white";

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    // Intent Data
    private static final int REQUEST_IMAGE_GET_ICON = 0;
    private static final int REQUEST_PERMISSION_MANAGE_DOCUMENTS = 1;
    private static final int REQUEST_SCAN_QR_CODE = 7;

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
            R.drawable.art_arts_64dp,
            R.drawable.art_biology_64dp,
            R.drawable.art_business_64dp,
            R.drawable.art_chemistry_64dp,
            R.drawable.art_childdevelopment_64dp,
            R.drawable.art_class_64dp,
            R.drawable.art_computing_64dp,
            R.drawable.art_cooking_64dp,
            R.drawable.art_creativestudies_64dp,
            R.drawable.art_drama_64dp,
            R.drawable.art_engineering_64dp,
            R.drawable.art_english_64dp,
            R.drawable.art_french_64dp,
            R.drawable.art_geography_64dp,
            R.drawable.art_graphics_64dp,
            R.drawable.art_hospitality_64dp,
            R.drawable.art_ict_64dp,
            R.drawable.art_maths_64dp,
            R.drawable.art_media_64dp,
            R.drawable.art_music_64dp,
            R.drawable.art_pe_64dp,
            R.drawable.art_physics_64dp,
            R.drawable.art_psychology_64dp,
            R.drawable.art_re_64dp,
            R.drawable.art_science_64dp,
            R.drawable.art_spanish_64dp,
            R.drawable.art_task_64dp,
            R.drawable.art_woodwork_64dp
    };

    private CharSequence[] addPeerMethodsArray = {"", ""};

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
                                            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
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
        }
        else mUserId = mFirebaseUser.getUid();

        // If there is previously set data in shared preferences, set it accordingly
        // If new data is found in the cloud database, this data will be replaced once loaded
        String savedName = preferences.getString(getString(R.string.KEY_PREFERENCES_SELF_NAME), getString(R.string.yourNameHere));
        flavour = preferences.getString(getString(R.string.KEY_PREFERENCES_FLAVOUR), getString(R.string.whats_up));
        String savedIconUri = preferences.getString(getString(R.string.KEY_PREFERENCES_SELF_ICON), null);
        selfNameView.setText(savedName);
        selfName = savedName;
        flavourView.setText(flavour);
        if (savedIconUri != null)
            try {
                // Check the permission for MANAGE DOCUMENTS and revert to the default icon
                // if the icon is custom uploaded and the permission is denied
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_DOCUMENTS) == PackageManager.PERMISSION_GRANTED
                        || savedIconUri.contains("android.resource://com.pdt.plume/drawable/")) {
                    Log.v(LOG_TAG, "MANAGE DOCUMENTS = " + Manifest.permission.MANAGE_DOCUMENTS + " PERMISSION GRANTED: " + PackageManager.PERMISSION_GRANTED);
                    selfIconView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(savedIconUri)));
                    selfIconUri = savedIconUri;
                }
                else {
                    selfIconUri = defaultIconUri;
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString(getString(R.string.KEY_PREFERENCES_SELF_ICON), defaultIconUri)
                            .apply();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        // Retrieve the data from the cloud database
        // and set the listener for changes in the cloud
        if (mFirebaseUser != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(mUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // After setting the self data to the saved key values in the phone
                    // The program will check for updated data in the cloud
                    // TODO: Perform check on if the snapshot is a URI or basic String and perform corresponding action
                    // TODO: This is for setting the selfIcon
                    Log.v(LOG_TAG, "Snapshot Value: " + dataSnapshot.getValue());
                    String iconData = dataSnapshot.child("icon").getValue(String.class);
                    String nicknameData = dataSnapshot.child("nickname").getValue(String.class);
                    final Bitmap[] bitmap = {null};
                    Log.v(LOG_TAG, "mUserId: " + mUserId);
                    Log.v(LOG_TAG, "Icon Value: " + iconData);
                    Log.v(LOG_TAG, "Nickname Value: " + nicknameData);
                    // Data snapshot is the icon
                    if (iconData != null) {
                        // Download from storage
                        StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl("gs://plume-academy-assistant.appspot.com");
                        StorageReference pathReference = storageRef.child("images/" + mUserId + ".jpg");
                        StorageReference gsReference = mFirebaseStorage.getReferenceFromUrl("gs://plume-academy-assistant.appspot.com/images/" + mUserId + ".jpg");
                        StorageReference httpsReference = mFirebaseStorage.getReferenceFromUrl
                                ("https://firebasestorage.googleapis.com/b/plume-academy-assistant.appspot.com/o/images%20" + mUserId + ".jpg");

                        final long TEN_MEGABYTE = 1024 * 1024 * 10;
                        gsReference.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                // Data for "images/island.jpg" is returns, use this as needed
                                bitmap[0] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });


                        try {
                            if (bitmap[0] == null)
                                bitmap[0] = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(selfIconUri));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        selfIconView.setImageBitmap(bitmap[0]);
                        selfIconUri = iconData;
                        // Save the data to shared preferences
                        preferences.edit()
                                .putString(getString(R.string.KEY_PREFERENCES_SELF_ICON), iconData)
                                .apply();
                    }

                        // Data snapshot is the name
                    if (nicknameData != null) {
                        selfNameView.setText(nicknameData);
                        selfName = nicknameData;
                        // Save the data to shared preferences
                        preferences.edit()
                                .putString(getString(R.string.KEY_PREFERENCES_SELF_NAME), nicknameData)
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        logInOut = menu.findItem(R.id.action_logout);
        if (loggedIn)
            logInOut.setTitle(getString(R.string.action_logout));
        else logInOut.setTitle(getString(R.string.action_login));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_MANAGE_DOCUMENTS:
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Execute upload intent
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(intent, REQUEST_IMAGE_GET_ICON);
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
            Bitmap setImageBitmap = null;

            try {
                setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dataUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            selfIconView.setImageBitmap(setImageBitmap);

            // Store the image in Firebase
            String filePath = dataUri.toString();
            StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl("gs://plume-academy-assistant.appspot.com");
            StorageReference iconRef = storageRef.child(mUserId + ".jpg");
            StorageReference iconsImagesRef = storageRef.child("images/" + mUserId + ".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            setImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteData = baos.toByteArray();

            UploadTask uploadTask = iconRef.putBytes(byteData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    mDatabase.child("users").child(mUserId).child("icon").setValue(downloadUrl.toString());
                }
            });

            // Save the icon uri
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(getString(R.string.KEY_PREFERENCES_SELF_ICON), dataUri.toString())
                    .apply();

        }

        if (requestCode == REQUEST_SCAN_QR_CODE) {
            if (resultCode == RESULT_OK) {
                // Check if the QR Code is a user's Firebase ID
                // Then get the id and send the intent to AddPeerActivity
                String contents = data.getStringExtra("SCAN_RESULT");
                DatabaseReference peerRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(contents);

                if (contents.equals(mUserId)) {
                    new AlertDialog.Builder(this).setTitle(getString(R.string.nice_try_title))
                            .setMessage(getString(R.string.nice_try_message))
                            .setPositiveButton(getString(R.string.ok), null)
                            .show();
                    return;
                }

                if (peerRef != null) {
                    // QR CODE IS LEGIT - SEND THE INTENT
                    Intent intent = new Intent(this, AddPeerActivity.class);
                    intent.putExtra("id", contents);
                    startActivity(intent);
                }
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        // Conduct permission check
//        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_DOCUMENTS);
//        if (permissionCheck == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            // Execute intent
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("image/*");
//            if (intent.resolveActivity(getPackageManager()) != null)
//                startActivityForResult(intent, REQUEST_IMAGE_GET_ICON);
//        } else {
//            // Prompt user for permission
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.MANAGE_DOCUMENTS},
//                    REQUEST_PERMISSION_MANAGE_DOCUMENTS);
//        }
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

        gridView.setAdapter(new BuiltInIconsAdapter(this));
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
                String flavour = dataSnapshot.child("flavour").getValue(String.class);
                uidList.add(dataSnapshot.getKey());
                nameList.add(name);
                flavourList.add(flavour);
                iconList.add(iconUri);
                arrayList.add(new Peer(iconUri, name));
                PeerAdapter adapter = new PeerAdapter(PeopleActivity.this, R.layout.list_item_peer, arrayList);
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
