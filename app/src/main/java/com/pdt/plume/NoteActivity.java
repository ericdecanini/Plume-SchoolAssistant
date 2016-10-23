package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

public class NoteActivity extends AppCompatActivity {

    String LOG_TAG = NoteActivity.class.getSimpleName();

    // UI Elements
    EditText fieldTitle;
    EditText fieldNote;

    int mPrimaryColor;
    int mDarkColor;

    int edit_ID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Make references to the UI elements
        fieldTitle = (EditText) findViewById(R.id.notes_title);
        fieldNote = (EditText) findViewById(R.id.notes_note);

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

        // Auto-fill the fields if note is being edited
        Intent intent = getIntent();
        edit_ID = intent.getIntExtra(getString(R.string.NOTES_EXTRA_ID), -1);
        if (edit_ID != -1) {
            // Get a cursor with the note's ID
            DbHelper dbHelper = new DbHelper(this);

            Cursor testCursor = dbHelper.getAllNoteData();
            testCursor.moveToFirst();
            for (int i = 0; i < testCursor.getCount(); i++) {
                int id = testCursor.getInt(testCursor.getColumnIndex(DbContract.NotesEntry._ID));
                Log.v(LOG_TAG, "ID " + i + " = " + id);
                testCursor.moveToNext();
            }

            Cursor cursor = dbHelper.getNoteByID(edit_ID);
            Log.v(LOG_TAG, "NoteByID " + edit_ID + " count: " + cursor.getCount());
            cursor.moveToFirst();
            String title = cursor.getString(cursor.getColumnIndex(DbContract.NotesEntry.COLUMN_TITLE));
            String note = cursor.getString(cursor.getColumnIndex(DbContract.NotesEntry.COLUMN_NOTE));
            fieldTitle.setText(title);
            fieldNote.setText(note);
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
            case R.id.action_done:
                saveNote();
                return true;
        }

        return false;
    }

    private void saveNote() {
        // Get the title and note data
        String title = fieldTitle.getText().toString();
        String note = fieldNote.getText().toString();

        // Get the intent data which should contain the title of the class
        String classTitle = getIntent().getStringExtra(getString(R.string.SCHEDULE_EXTRA_TITLE));

        // Then query a cursor with all of the rows of that title
        DbHelper dbHelper = new DbHelper(this);

        if (edit_ID == -1) {
            // Insert the note into the note table and get its id
            dbHelper.insertNoteItem(title, note, classTitle);
            Log.v(LOG_TAG, "INSERTING (edit_ID = " + edit_ID + ")");
        }
        else {
            dbHelper.updateNoteItem(edit_ID, title, note, classTitle);
            Log.v(LOG_TAG, "UPDATING");
        }


        finish();
    }

}
