package com.pdt.plume;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import java.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pdt.plume.data.DbHelper;
import com.pdt.plume.data.DbContract.ScheduleEntry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.data;
import static com.pdt.plume.StaticRequestCodes.REQUEST_IMAGE_GET;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT;

public class NewScheduleActivity extends AppCompatActivity
        implements DaysDialog.OnDaysSelectedListener,
        IconPromptDialog.iconDialogListener {

    // Constantly Used Variables
    String LOG_TAG = NewScheduleActivity.class.getSimpleName();
    Utility utility = new Utility();
    int mPeriodListSize;

    // UI Elements
    AutoCompleteTextView fieldTitle;
    EditText fieldTeacher;
    EditText fieldRoom;
    ImageView fieldIcon;
    ListView classTimeList;
    TextView fieldAddClassTime;

    // UI Data
    String iconUri;
    String title;
    String teacher;
    String room;
    ArrayList<PeriodItem> mPeriodsList = new ArrayList<>();
    PeriodAdapter periodAdapter;
    int defaultIcon = R.drawable.art_class_64dp;

    int mPrimaryColor;
    int mDarkColor;
    int mSecondaryColor;

    // Firebase Variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    String mUserId;

    // Built-in Icons
    // Built-in Icons
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
            R.drawable.art_history_64dp,
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

    // Intent Data
    boolean STARTED_BY_NEWTASKACTIVITY = false;

    // Interface Data
    String basis = "-1";
    String weekType = "-1";
    public static int resourceId = -1;

    // Flags
    boolean isTablet;
    boolean FLAG_EDIT;
    boolean customIconUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule);

        // First check if the basis and week type have been selected
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        basis = preferences.getString(getString(R.string.KEY_PREFERENCE_BASIS), "-1");
        weekType = preferences.getString(getString(R.string.KEY_PREFERENCE_WEEKTYPE), "-1");
        if (basis.equals("-1")) {
            Intent intent = new Intent(this, NewPeriodOneActivity.class);
            startActivity(intent);
            return;
        } else if (weekType.equals("-1") && !basis.equals("2")) {
            Intent intent = new Intent(this, NewPeriodTwoActivity.class);
            startActivity(intent);
            return;
        }

        // Set tablet window properties
        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0f);
        isTablet = getResources().getBoolean(R.bool.isTablet);

        if (isTablet) {
            int height = getWindowManager().getDefaultDisplay().getHeight();
            View cardview = findViewById(R.id.cardview);
            if (cardview != null)
                cardview.setMinimumHeight(height);
        }

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUserId = mFirebaseUser.getUid();

        // Get references to the UI elements
        fieldTitle = (AutoCompleteTextView) findViewById(R.id.field_new_schedule_title);
        fieldTeacher = (EditText) findViewById(R.id.field_new_schedule_teacher);
        fieldRoom = (EditText) findViewById(R.id.field_new_schedule_room);
        fieldAddClassTime = (TextView) findViewById(R.id.field_new_schedule_add_class_time);
        fieldIcon = (ImageView) findViewById(R.id.new_schedule_icon);
        classTimeList = (ListView) findViewById(R.id.field_new_schedule_class_time_list);

        // Set the OnClickListener for the UI elements
        fieldIcon.setOnClickListener(showIconDialogListener());
        fieldAddClassTime.setOnClickListener(addPeriodListener());

        // Set the mTasksAdapter for the title auto-complete text view
        String[] subjects = getResources().getStringArray(R.array.subjects);
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, subjects);
        fieldTitle.setAdapter(autoCompleteAdapter);

        // Check if the activity was started by an intent from an edit action
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            title = extras.getString(getString(R.string.INTENT_EXTRA_TITLE));
            FLAG_EDIT = extras.getBoolean(getResources().getString(R.string.INTENT_FLAG_EDIT), false);
            STARTED_BY_NEWTASKACTIVITY = extras.getBoolean("STARTED_BY_NEWTASKACTIVITY", false);
        }
        // Get schedule data in database based on the schedule title to auto-fill the fields in the UI element
        if (FLAG_EDIT) {
            if (mFirebaseUser != null) {
                // Get the data from Firebase
                DatabaseReference classRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(mUserId).child("classes").child(title);
                classRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get key values
                        teacher = dataSnapshot.child("teacher").getValue(String.class);
                        room = dataSnapshot.child("room").getValue(String.class);
                        iconUri = dataSnapshot.child("icon").getValue(String.class);

                        ArrayList<String> occurrenceList = new ArrayList<>();
                        ArrayList<Integer> timeInList = new ArrayList<>();
                        ArrayList<Integer> timeOutList = new ArrayList<>();
                        ArrayList<Integer> timeInAltList = new ArrayList<>();
                        ArrayList<Integer> timeOutAltList = new ArrayList<>();
                        ArrayList<String> periodsList = new ArrayList<>();

                        // Get listed values
                        DataSnapshot occurrences = dataSnapshot.child("occurrence");
                        for (DataSnapshot occurrenceSnapshot : occurrences.getChildren()) {
                            occurrenceList.add(occurrenceSnapshot.getKey());
                        }
                        DataSnapshot timeins = dataSnapshot.child("timein");
                        for (DataSnapshot timeinSnapshot : timeins.getChildren()) {
                            timeInList.add(timeinSnapshot.getValue(Integer.class));
                        }
                        DataSnapshot timeouts = dataSnapshot.child("timeout");
                        for (DataSnapshot timeoutSnapshot : timeouts.getChildren()) {
                            timeOutList.add(timeoutSnapshot.getValue(Integer.class));
                        }
                        DataSnapshot timeinsalt = dataSnapshot.child("timeinalt");
                        for (DataSnapshot timeinaltSnapshot : timeinsalt.getChildren()) {
                            timeInAltList.add(timeinaltSnapshot.getValue(Integer.class));
                        }
                        DataSnapshot timeoutsalt = dataSnapshot.child("timeoutalt");
                        for (DataSnapshot timeoutaltSnapshot : timeoutsalt.getChildren()) {
                            timeOutAltList.add(timeoutaltSnapshot.getValue(Integer.class));
                        }
                        DataSnapshot periods = dataSnapshot.child("periods");
                        for (DataSnapshot periodsSnapshot : periods.getChildren()) {
                            periodsList.add(periodsSnapshot.getKey());
                        }

                        // These arrays should all be of equal size
                        // Add them to a user viewable list
                        for (int i = 0; i < occurrenceList.size(); i++) {
                            String occurrence = occurrenceList.get(i);
                            if (!occurrence.equals("-1")) {
                                mPeriodsList.add(new PeriodItem(
                                        NewScheduleActivity.this,
                                        timeInList.get(i),
                                        timeOutList.get(i),
                                        timeInAltList.get(i),
                                        timeOutAltList.get(i),
                                        periodsList.get(i), occurrence
                                ));
                            }
                        }

                        // Apply the data to the views
                        fieldTitle.setText(title);
                        fieldTeacher.setText(teacher);
                        fieldRoom.setText(room);
                        Log.v(LOG_TAG, "IconUri: " + iconUri);
                        try {
                            Bitmap setImageBitmap = MediaStore.Images.Media.getBitmap(NewScheduleActivity.this.getContentResolver(),
                                    Uri.parse(iconUri));
                            fieldIcon.setImageBitmap(setImageBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                // Get the data from SQLite
                DbHelper dbHelper = new DbHelper(this);
                Cursor cursor;
                cursor = dbHelper.getScheduleDataByTitle(title);
                if (cursor.moveToFirst()) {
                    teacher = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_TEACHER));
                    room = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ROOM));
                    iconUri = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_ICON));
                    // Get database values to put in activity Array Lists
                    for (int i = 0; i < cursor.getCount(); i++) {
                        String occurrence = cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_OCCURRENCE));
                        if (!occurrence.equals("-1")) {
                            mPeriodsList.add(new PeriodItem(
                                    this,
                                    cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN)),
                                    cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT)),
                                    cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEIN_ALT)),
                                    cursor.getInt(cursor.getColumnIndex(ScheduleEntry.COLUMN_TIMEOUT_ALT)),
                                    cursor.getString(cursor.getColumnIndex(ScheduleEntry.COLUMN_PERIODS)),
                                    occurrence));
                        }

                        if (!cursor.moveToNext())
                            cursor.moveToFirst();
                    }
                }
                cursor.close();

                // Apply the data to the views
                fieldTitle.setText(title);
                fieldTeacher.setText(teacher);
                fieldRoom.setText(room);
                try {
                    Bitmap setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(iconUri));
                    fieldIcon.setImageBitmap(setImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // No edit, a new schedule is being added. Set the iconUri to be the default
            Resources resources = getResources();
            Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(defaultIcon)
                    + '/' + resources.getResourceTypeName(defaultIcon) + '/' + resources.getResourceEntryName(defaultIcon));
            iconUri = drawableUri.toString();
        }

        // Initialise the periods list
        periodAdapter = new PeriodAdapter(this, R.layout.list_item_new_period, mPeriodsList);
        if (basis.equals("2"))
            findViewById(R.id.field_new_schedule_add_class_time).setVisibility(View.GONE);

        // Add one item at init (if not edited)
        if (!FLAG_EDIT) {
            long timein;
            long timeout;
            if (basis.equals("0")) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                if (minute >= 15 && minute < 45)
                    minute = 30;
                if (minute >= 45) {
                    minute = 0;
                    hour++;
                }
                timein = utility.timeToMillis(hour, minute);
                hour += 1;
                timeout = utility.timeToMillis(hour, minute);
            } else {
                timein = -1;
                timeout = -1;
            }

            // Build an occurrence containing the current day
            StringBuilder builder = new StringBuilder();
            Calendar c = Calendar.getInstance();
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            String weekNumber = preferences.getString(getString(R.string.KEY_WEEK_NUMBER), "0");
            Log.v(LOG_TAG, "Day of week: " + dayOfWeek);
            for (int i = 0; i < 7; i++) {
                builder.append(":");
                if (i == dayOfWeek - 1)
                    if (weekNumber.equals("0"))
                        builder.append("1");
                    else builder.append("2");
                else builder.append("0");
            }

            Log.v(LOG_TAG, "Init occurrence: " + builder.toString());

            mPeriodsList.add(new PeriodItem(this, timein, timeout, timein, timeout,
                    "0:0:0:0:0:0:0:0", basis + ":" + weekType + builder.toString()));
        }
        periodAdapter.notifyDataSetChanged();
        classTimeList.setAdapter(periodAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), getResources().getColor(R.color.colorAccent));
        if (fieldAddClassTime != null)
            fieldAddClassTime.setTextColor(mPrimaryColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isTablet)
                fieldTitle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_700)));
            if (fieldTeacher != null)
                fieldTeacher.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_700)));
            if (fieldTeacher != null)
                fieldRoom.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_700)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Without this, the up button will not do anything and return the error 'Cancelling event due to no window focus'
            case android.R.id.home:
                finish();
                break;

            // Validate input fields then
            // Insert inputted data into the database and terminate the activity
            case R.id.action_done:
                // Check if the title field is empty, disallow insertion of it is
                if ((fieldTitle.getText().toString().equals(""))) {
                    Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_title_not_found),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                for (int i = 0; i < mPeriodsList.size(); i++) {
                    String[] occurrenceArray = mPeriodsList.get(i).occurrence.split(":");
                    ArrayList<String> daysList = new ArrayList<>();
                    for (int l = 0; l < occurrenceArray.length; l++)
                        if (l > 1)
                            daysList.add(occurrenceArray[l]);
                    if (!daysList.contains("1") && !daysList.contains("2") && !daysList.contains("3") && !basis.equals("2")) {
                        Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_no_days_selected),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    String[] daysArray = mPeriodsList.get(i).days.split(":");
                    String[] daysAltArray = mPeriodsList.get(i).days_alt.split(":");
                    ArrayList<String> dayList = new ArrayList<>();
                    ArrayList<String> dayAltList = new ArrayList<>();
                    for (int l = 0; l < daysAltArray.length; l++) {
                        dayList.add(daysArray[l]);
                        dayAltList.add(daysAltArray[l]);
                    }
                    boolean[] periodsArray = mPeriodsList.get(i).period;
                    boolean[] periodsAltArray = mPeriodsList.get(i).periodAlt;
                    ArrayList<Boolean> periodsList = new ArrayList<>();
                    ArrayList<Boolean> periodsAltList = new ArrayList<>();
                    for (int l = 0; l < periodsArray.length; l++) {
                        periodsList.add(periodsArray[l]);
                        periodsAltList.add(periodsAltArray[l]);
                    }
                    if (!occurrenceArray[0].equals("2") && !dayList.contains("1") && !dayAltList.contains("1")) {
                        Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_no_days_selected),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!occurrenceArray[0].equals("0") && !periodsList.contains(true) && !periodsAltList.contains(true)) {
                        Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_no_period_selected),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    // Validate timeIn and timeOut
                    long timeIn = mPeriodsList.get(i).timeinValue;
                    long timeOut = mPeriodsList.get(i).timeoutValue;
                    long timeInAlt = mPeriodsList.get(i).timeinaltValue;
                    long timeOutAlt = mPeriodsList.get(i).timeoutaltValue;

                    if (timeOut < timeIn && dayList.contains("1")) {
                        Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_end_before_start),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if (timeOutAlt < timeInAlt && dayAltList.contains("1")) {
                        Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_end_before_start),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                mPeriodListSize = 0;

                // Check for clashing titles from existing classes
                title = fieldTitle.getText().toString();
                if (mFirebaseUser != null) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(mUserId).child("classes")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot classSnapshot: dataSnapshot.getChildren()) {
                                        if (classSnapshot.getKey().equals(title) && !FLAG_EDIT) {
                                            Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_clashing_title),
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }

                                    // No clashes: perform insert function
                                    insertScheduleDataIntoFirebase();
                                    if (!FLAG_EDIT)
                                        Toast.makeText(NewScheduleActivity.this,
                                                getString(R.string.new_schedule_toast_class_inserted, title), Toast.LENGTH_SHORT).show();

                                    if (!STARTED_BY_NEWTASKACTIVITY) {
                                        Intent intent = new Intent(NewScheduleActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                } else {
                    DbHelper dbHelper = new DbHelper(this);
                    Cursor cursor = dbHelper.getScheduleDataByTitle(title);
                    if (cursor.getCount() == 0) {
                        try {
                            insertScheduleDataIntoDatabase();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!FLAG_EDIT)
                            Toast.makeText(NewScheduleActivity.this,
                                    getString(R.string.new_schedule_toast_class_inserted, title), Toast.LENGTH_SHORT).show();

                        if (!STARTED_BY_NEWTASKACTIVITY) {
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                        }
                        finish();
                        return true;
                    }
                    else {
                        Toast.makeText(NewScheduleActivity.this, getString(R.string.new_schedule_toast_validation_clashing_title),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            Uri fullPhotoUri = data.getData();
            Bitmap setImageBitmap = null;

            iconUri = fullPhotoUri.toString();

            try {
                setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fullPhotoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            fieldIcon.setImageBitmap(setImageBitmap);
            customIconUploaded = true;
        }
    }

    // Helper method to create the computer-readable occurrence string
    private String processOccurrenceString(String basis, String weekType, String classDays) {
        return basis + ":" + weekType + ":" + classDays;
    }

    private boolean insertScheduleDataIntoFirebase() {
        // Get the input from the fields
        mPeriodListSize++;
        if (mPeriodListSize > mPeriodsList.size())
            return true;
        title = fieldTitle.getText().toString();
        String teacher = fieldTeacher.getText().toString();
        String room = fieldRoom.getText().toString();

        // Set the key values of the class
        DatabaseReference classRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserId).child("classes").child(title);
        classRef.child("teacher").setValue(teacher);
        classRef.child("room").setValue(room);
        classRef.child("occurrence").removeValue();
        classRef.child("periods").removeValue();

        // Set the listed values of the class
        if (mPeriodsList.size() != 0) {
            for (int i = 0; i < mPeriodsList.size(); i++) {
                // Gather the data to set the values on the cloud
                PeriodItem item = ((PeriodItem) periodAdapter.getItem(i));
                String occurrence = item.occurrence;
                long timeIn = item.timeinValue;
                long timeOut = item.timeoutValue;
                long timeInAlt = item.timeinaltValue;
                long timeOutAlt = item.timeoutaltValue;
                String periods = getItemPeriods(i);

                classRef.child("occurrence").child(occurrence).setValue("");
                classRef.child("timein").child(String.valueOf(i)).setValue(timeIn);
                classRef.child("timeout").child(String.valueOf(i)).setValue(timeOut);
                classRef.child("timeinalt").child(String.valueOf(i)).setValue(timeInAlt);
                classRef.child("timeoutalt").child(String.valueOf(i)).setValue(timeOutAlt);
                classRef.child("periods").child(periods).setValue("");
            }
        } else {
            // Set a class with no listed values, removing any old ones
            classRef.child("occurrence").removeValue();
            classRef.child("timein").removeValue();
            classRef.child("timeout").removeValue();
            classRef.child("timeinalt").removeValue();
            classRef.child("timeoutalt").removeValue();
            classRef.child("periods").removeValue();
        }

        // Upload the custom item to Firebase Storage if set
        // and copy it into the app's local directory
        if (customIconUploaded) {
            // Copy function
            try {
                Uri imageUri = Uri.parse(iconUri);
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                String filename = title + ".jpg";
                byte[] data = getBytes(inputStream);
                File file = new File(getFilesDir(), filename);
                FileOutputStream outputStream;
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(data);
                outputStream.close();

                // Image compression
                Bitmap decodedBitmap = decodeFile(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                decodedBitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
                byte[] bitmapData = baos.toByteArray();
                file.delete();
                File file1 = new File(getFilesDir(), filename);
                FileOutputStream fos = new FileOutputStream(file1);
                fos.write(bitmapData);
                fos.flush();
                fos.close();

                // Get the uri of the file so it can be saved
                iconUri = Uri.fromFile(file1).toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Upload the URI into the cloud database
            classRef.child("icon").setValue(iconUri);

            // Upload function
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference iconRef = storageRef.child(mUserId + "/classes/" + title);

            fieldIcon.setDrawingCacheEnabled(true);
            fieldIcon.buildDrawingCache();
            Bitmap bitmap = fieldIcon.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = iconRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO: Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // TODO: Handle successful upload if any action is required
                }
            });
        } else {
            classRef.child("icon").setValue(iconUri);
        }

        return true;
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

    private boolean insertScheduleDataIntoDatabase() throws IOException {
        // Store data from UI input fields to variables to prepare them for insertion into the database
        String title = fieldTitle.getText().toString();
        this.title = title;
        String teacher = fieldTeacher.getText().toString();
        String room = fieldRoom.getText().toString();

        // Copy the icon into a seperate file if custom
        Uri imageUri = Uri.parse(iconUri);
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        String filename = imageUri.getLastPathSegment();
        byte[] data = getBytes(inputStream);
        File file = new File(getFilesDir(), filename);
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data);
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
        FileOutputStream fos = new FileOutputStream(file1);
        fos.write(bitmapData);
        fos.flush();
        fos.close();

        // Get the path of the file so it can be saved
        iconUri = Uri.fromFile(file1).toString();

        DbHelper dbHelper = new DbHelper(this);
        if (FLAG_EDIT) {
            // Delete the previous all instances of the schedule (based on the title)
            Cursor cursor = dbHelper.getScheduleDataByTitle(this.title);
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    int rowId = cursor.getInt(cursor.getColumnIndex(ScheduleEntry._ID));
                    dbHelper.deleteScheduleItem(rowId);
                }
            }

            // Insert a row for each occurrence item. If there is no occurrence item
            // Insert a single row
            if (mPeriodsList.size() != 0)
                for (int i = 0; i < mPeriodsList.size(); i++) {
                    // Initialise occurrence, time, and period strings
                    PeriodItem item = ((PeriodItem) periodAdapter.getItem(i));
                    String occurrence = item.occurrence;
                    long timeIn = item.timeinValue;
                    long timeOut = item.timeoutValue;
                    long timeInAlt = item.timeinaltValue;
                    long timeOutAlt = item.timeoutaltValue;
                    String periods = getItemPeriods(i);


                    // Database insert function performed as update
                    if (dbHelper.insertSchedule(title, teacher, room, occurrence,
                            timeIn, timeOut, timeInAlt, timeOutAlt,
                            periods, iconUri)) {
                        if (i == mPeriodsList.size() - 1)
                            return true;
                    } else
                        Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
                }
                // Single row edit, no occurrence
            else {
                // Database insert function without any occurrences
                if (dbHelper.insertSchedule(title, teacher, room, "-1",
                        -1, -1, -1, -1,
                        "-1", iconUri)) {
                    return true;
                } else
                    Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
            }

        }
        // If the activity was not started by an edit action, insert a new row into the database
        else {
            // Insert a row for each occurrence item
            if (mPeriodsList.size() != 0)
                for (int i = 0; i < mPeriodsList.size(); i++) {
                    // Initialise occurrence, time, and period strings
                    PeriodItem item = ((PeriodItem) periodAdapter.getItem(i));
                    String occurrence = item.occurrence;
                    Log.v(LOG_TAG, "Item occurrence: " + occurrence);
                    long timeIn = item.timeinValue;
                    long timeOut = item.timeoutValue;
                    long timeInAlt = item.timeinaltValue;
                    long timeOutAlt = item.timeoutaltValue;
                    String periods = getItemPeriods(i);

                    // TODO: Check this function against alarms from ScheduleFragment
//                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                    Intent intent = new Intent(this, MuteAlarmReceiver.class);
//                    intent.putExtra("UNMUTE_TIME", timeOut);
//                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    Log.v(LOG_TAG, "Class notification for " + title + " set for " + timeIn);
//                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeIn, AlarmManager.INTERVAL_DAY, pendingIntent);

                    // Database insert function
                    if (dbHelper.insertSchedule(title, teacher, room, occurrence,
                            timeIn, timeOut, timeInAlt, timeOutAlt,
                            periods, iconUri)) {
                        if (i == mPeriodsList.size() - 1)
                            return true;
                    } else
                        Toast.makeText(NewScheduleActivity.this, "Error creating new schedule", Toast.LENGTH_SHORT).show();
                }
            else {
                // Database insert function without any periods
                if (dbHelper.insertSchedule(title, teacher, room, "-1",
                        -1, -1, -1, -1,
                        "-1", iconUri)) {
                    return true;
                } else
                    Toast.makeText(NewScheduleActivity.this, "Error editing schedule", Toast.LENGTH_SHORT).show();
            }
        }

        // If data insertion functions were not executed, return false by default
        return false;
    }

    private void scheduleNotification(final Date dateTime, final int ID, final String title, final String message) {

        final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(iconUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setBackground(largeIcon);

        Intent contentIntent = new Intent(this, ScheduleDetailActivity.class);
        contentIntent.putExtra(getString(R.string.INTENT_EXTRA_CLASS), title);
        final PendingIntent contentPendingIntent = PendingIntent.getBroadcast(this, REQUEST_NOTIFICATION_INTENT, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Palette.generateAsync(largeIcon, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                builder
                        .setContentIntent(contentPendingIntent)
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setColor(palette.getVibrantColor(mPrimaryColor))
                        .setContentTitle(title)
                        .setContentText(message)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .extend(wearableExtender)
                        .setDefaults(Notification.DEFAULT_ALL);

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(NewScheduleActivity.this, TaskNotificationPublisher.class);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(NewScheduleActivity.this, REQUEST_NOTIFICATION_ALARM, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC, dateTime.getTime(), AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        });
    }

    private View.OnClickListener addPeriodListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String occurrence = basis + ":" + weekType + ":0:0:0:0:0:0:0";
                String periods = "0:0:0:0:0:0:0:0";
                mPeriodsList.add(new PeriodItem(NewScheduleActivity.this, -1, -1, -1, -1, periods, occurrence));
                periodAdapter.notifyDataSetChanged();
            }
        };
    }

    private View.OnClickListener showIconDialogListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new IconPromptDialog();
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        };
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

        gridView.setAdapter(new BuiltInSubjectIconsAdapter(this));
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
                int resId = mThumbIds[position];
                fieldIcon.setImageResource(resId);
                Resources resources = getResources();
                Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                        + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId));
                iconUri = drawableUri.toString();
                customIconUploaded = false;
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void OnIconListItemSelected(int item) {
        switch (item) {
            case 0:
                showBuiltInIconsDialog();
                break;
            case 1:
                if (mFirebaseUser != null) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                }
                break;
        }
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
    public void OnDaysSelected(boolean alternate, int position, String days) {
        Log.v(LOG_TAG, "OnDaysSelected: " + alternate + ", " + position + ", " + days);
        PeriodItem item = mPeriodsList.get(position);
        if (!alternate) item.days = days;
        else item.days_alt = days;

        updateItemOccurrence(position);
        periodAdapter.notifyDataSetChanged();
    }

    private void updateItemOccurrence(int itemPosition) {
        PeriodItem item = mPeriodsList.get(itemPosition);
        String[] daysArray = item.days.split(":");
        String[] daysAltArray = item.days_alt.split(":");
        String[] daysCombinedArray = {"0", "0", "0", "0", "0", "0", "0"};
        for (int i = 0; i < daysCombinedArray.length; i++) {
            if (daysArray[i].equals("0") && daysAltArray[i].equals("0"))
                daysCombinedArray[i] = "0";
            else if (daysArray[i].equals("1") && daysAltArray[i].equals("0"))
                daysCombinedArray[i] = "1";
            else if (daysArray[i].equals("0") && daysAltArray[i].equals("1"))
                daysCombinedArray[i] = "2";
            else daysCombinedArray[i] = "3";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(item.basis);
        builder.append(":");
        builder.append(item.weekType);
        builder.append(":");
        for (int i = 0; i < daysCombinedArray.length; i++) {
            builder.append(daysCombinedArray[i]);
            if (i != daysCombinedArray.length - 1)
                builder.append(":");
        }

        Log.v(LOG_TAG, "New occurrence: " + builder.toString());
        item.occurrence = builder.toString();

        if (!item.days.contains("1"))
            for (int i = 0; i < item.period.length; i++) {
                item.period[i] = false;
            }

        if (!item.days_alt.contains("1"))
            for (int i = 0; i < item.periodAlt.length; i++) {
                item.periodAlt[i] = false;
            }

    }

    private String getItemPeriods(int position) {
        PeriodItem item = mPeriodsList.get(position);
        boolean[] periods = item.period;
        boolean[] periodsAlt = item.periodAlt;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < periods.length; i++) {
            if (i != 0) builder.append(":");
            if (!periods[i] && !periodsAlt[i])
                builder.append("0");
            else if (periods[i] && !periodsAlt[i])
                builder.append("1");
            else if (!periods[i] && periodsAlt[i])
                builder.append("2");
            else builder.append("3");
        }

        return builder.toString();
    }

}