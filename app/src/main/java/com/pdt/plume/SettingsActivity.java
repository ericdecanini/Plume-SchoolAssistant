package com.pdt.plume;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    String LOG_TAG = SettingsActivity.class.getSimpleName();

    private AppCompatDelegate mDelegate;

    int mPrimaryColor;
    int mDarkColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.preference_main);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_SETTINGS_WEEK_NUMBER)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_SETTINGS_DATE_FORMAT)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_SETTINGS_BLOCK_FORMAT)));

        // For other preferences, simply an OnClickListener is needed
        findPreference(getString(R.string.KEY_SETTINGS_THEME)).setOnPreferenceClickListener(onPreferenceClickListener());
        findPreference(getString(R.string.KEY_SETTINGS_ABOUT_PLUME)).setOnPreferenceClickListener(onPreferenceClickListener());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor  = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }

    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof NumberPickerPreference)
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getInt(preference.getKey(), 0));
        else onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "0"));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String stringValue = value.toString();
        String prefKey = preference.getKey();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
                Log.v(LOG_TAG, preference.getKey() + " entry at index " + prefIndex + " = " + listPreference.getEntries()[prefIndex]);

                // If 'Week number'
                if (prefKey.equals(getString(R.string.KEY_SETTINGS_WEEK_NUMBER))){
                    editor.putString(getString(R.string.KEY_WEEK_NUMBER), ((String)listPreference.getEntryValues()[prefIndex]));
                    Log.v(LOG_TAG, "Setting week number to " + prefIndex);
                }

                // If 'Date format'
                else if (prefKey.equals(getString(R.string.KEY_SETTINGS_DATE_FORMAT))){
                    editor.putString("dateFormat", ((String)listPreference.getEntryValues()[prefIndex]));
                }
            }

        }
        else {
            // Roll a check for what preference has been selected
            // If 'Notification before class starts'
            if (prefKey.equals(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION))) {
                NumberPickerPreference numberPickerPreference = (NumberPickerPreference) preference;
                editor.putInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), numberPickerPreference.getValue());
                numberPickerPreference.setSummary(stringValue + " " + getString(R.string.settings_class_notification_subtitle));
            }

            // If 'Mute phone during classes'
            else if (prefKey.equals(getString(R.string.KEY_SETTINGS_CLASS_MUTE))){
                editor.putBoolean(getString(R.string.KEY_SETTINGS_CLASS_MUTE), ((CheckBoxPreference)preference).isChecked());
            }

            else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }

        }

        editor.commit();
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private Preference.OnPreferenceClickListener onPreferenceClickListener() {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String prefKey = preference.getKey();

                // If 'Theme'
                if (prefKey.equals(getString(R.string.KEY_SETTINGS_THEME))) {
                    Intent intent = new Intent(SettingsActivity.this, SettingsThemeActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (prefKey.equals(getString(R.string.KEY_SETTINGS_ABOUT_PLUME))) {
                    Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * ACTION BAR METHODS
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
