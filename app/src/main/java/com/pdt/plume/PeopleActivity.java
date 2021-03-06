package com.pdt.plume;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.pdt.plume.StaticRequestCodes.REQUEST_IMAGE_GET_ICON;
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
    ArrayList<String> flavourList = new ArrayList<>();
    AlertDialog dialog;

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

    private Integer[] mThumbIdsHalloween = {
            R.drawable.art_profile_catgirl,
            R.drawable.art_profile_jason,
            R.drawable.art_profile_morty,
            R.drawable.art_profile_pennywise,
            R.drawable.art_profile_pumpkin,
            R.drawable.art_profile_skull,
            R.drawable.art_profile_vampire,
            R.drawable.art_profile_witch,
            R.drawable.art_profile_zombie
    };

    private CharSequence[] addPeerMethodsArray = {"", ""};
    boolean isTablet;
    boolean isLandscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) savedInstanceState.clear();

        if (!getResources().getBoolean(R.bool.isTablet)) setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_people);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isTablet = getResources().getBoolean(R.bool.isTablet);
        isLandscape = getResources().getBoolean(R.bool.isLandscape);

        if (isTablet) {
            if (!isLandscape)
                getSupportActionBar().setElevation(0f);
        } else {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.95f; // value component
        int actionColor = Color.HSVToColor(hsv);

        int backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), getResources().getColor(R.color.backgroundColor));

        Color.colorToHSV(backgroundColor, hsv);
        hsv[2] *= 0.9f;
        int darkBackgroundColor = Color.HSVToColor(hsv);
        int textColor = preferences.getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), getResources().getColor(R.color.black_0_54));

        ((TextView) findViewById(R.id.textView1)).setTextColor(textColor);
        ((TextView) findViewById(R.id.textView2)).setTextColor(textColor);
        ((TextView) findViewById(R.id.textView3)).setTextColor(textColor);

        if (isTablet) {
            if (isLandscape) {
                findViewById(R.id.cardview).setBackgroundColor(backgroundColor);
                findViewById(R.id.container).setBackgroundColor(darkBackgroundColor);
                findViewById(R.id.gradient_overlay).setBackgroundColor(mPrimaryColor);
            } else {
                findViewById(R.id.activity_people).setBackgroundColor(backgroundColor);
            }
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionColor));
            findViewById(R.id.extended_appbar).setBackgroundColor(mPrimaryColor);
        } else {
            findViewById(R.id.appbar).setBackgroundColor(mPrimaryColor);
            if (isLandscape) findViewById(R.id.master_layout).setBackgroundColor(backgroundColor);
            else findViewById(R.id.activity_people).setBackgroundColor(backgroundColor);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        addPeersTextview.setTextColor(mPrimaryColor);

        // Set the click listeners of the views
        // PROFILE VIEWS
        selfIconView.setOnClickListener(showIconDialog());
        selfNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NameDialogFragment fragment = NameDialogFragment.newInstance(selfName);
                Bundle args = new Bundle();
                args.putString("title", selfName);
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

                    // Data snapshot is the category
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
                Peer peer = arrayList.get(i);
                String uid = peer.id;
                String name = peer.peerName;
                String flavour = flavourList.get(i);
                String iconUri = peer.peerIcon;
                // Make the intent to the profile activity
                Intent intent = new Intent(PeopleActivity.this, PeerProfileActivity.class);
                intent.putExtra("uid", uid)
                        .putExtra("title", name)
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

        // Save the category to SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.KEY_PREFERENCES_SELF_NAME), name)
                .apply();

        // Save the category to the cloud database
        mDatabase.child("users").child(mUserId).child("nickname").setValue(name);
    }

    @Override
    public void onFlavourSelected(String flavour) {
        // Set the flavour on the TextView
        this.flavour = flavour;
        flavourView.setText(flavour);

        // Save the category to SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.KEY_PREFERENCES_FLAVOUR), flavour)
                .apply();

        // Save the category to the cloud database
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
        // Cancel online notifications
        Utility.rescheduleNotifications(this, false);

        // Execute the Sign Out Operation
        mFirebaseAuth.signOut();
        loggedIn = false;
        logInOut.setTitle(getString(R.string.action_login));
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        // Prepare an array list of grid categories containing a string and a grid adapter
        ArrayList<GridCategory> gridCategories = new ArrayList<>();

        // Initialise the adapters and listeners and add them to a list of grid categories
        BuiltInProfileIconsAdapter adapter = new BuiltInProfileIconsAdapter(this, 0);
        BuiltInProfileIconsAdapter adapterHalloween = new BuiltInProfileIconsAdapter(this, 1);

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
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

                if (dialog != null)
                    dialog.dismiss();
            }
        };

        AdapterView.OnItemClickListener listenerHalloween = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Set the image resource and save the URI as a global variable
                int resId = mThumbIdsHalloween[position];
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

                if (dialog != null)
                    dialog.dismiss();
            }
        };

        gridCategories.add(new GridCategory(getString(R.string.Default), adapter, listener));
        gridCategories.add(new GridCategory(getString(R.string.halloween), adapterHalloween, listenerHalloween));

        // Create the listview and set its adapter
        ListView listview = new ListView(this);
        listview.setAdapter(new GridCategoryAdapter(this, R.layout.list_item_grid, gridCategories));

        // Initialise the dialog and add the listview to the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.new_schedule_icon_builtin_title));
        builder.setView(listview);
        dialog = builder.show();

        // Show the dialog
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
                    final String flavour = peerSnapshot.child("flavour").getValue(String.class);
                    String uid = peerSnapshot.getKey();
                    final String iconUri = peerSnapshot.child("icon").getValue(String.class).replace("icon", uid);

                    // Check if iconUri points to a valid file
                    final File file = new File(getFilesDir(), uid + ".jpg");
                    if (file.canRead() || iconUri.contains("art_")) {
                        flavourList.add(flavour);
                        arrayList.add(new Peer(iconUri, name, peerSnapshot.getKey()));
                        adapter.notifyDataSetChanged();
                    } else {
                        // FILE DOESN'T EXIST: DOWNLOAD FROM CLOUD STORAGE
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference iconRef = storageRef.child(uid).child("icon");

                        long ONE_MEGABYTE = 1024 * 1024;
                        iconRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                saveInternalFile(bytes, file.getName() + ".jpg");
                                Log.v(LOG_TAG, "Download successful: " + file.getPath() + ".jpg");
                                flavourList.add(flavour);
                                arrayList.add(new Peer(iconUri, name, peerSnapshot.getKey()));
                                adapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.v(LOG_TAG, "Download failed: " + e.getMessage());
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

    private void saveInternalFile(byte[] bytes, String filepath) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(filepath, MODE_PRIVATE);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
