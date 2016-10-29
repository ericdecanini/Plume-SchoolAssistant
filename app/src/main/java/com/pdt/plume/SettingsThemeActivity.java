package com.pdt.plume;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;


public class SettingsThemeActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    String LOG_TAG = SettingsActivity.class.getSimpleName();

    private AppCompatDelegate mDelegate;

    int mPrimaryColor;
    int mDarkColor;

    private Drawable[] mThumbIds = {
            new ColorDrawable(Color.parseColor("#F44336")),
            new ColorDrawable(Color.parseColor("#E91E63")),
            new ColorDrawable(Color.parseColor("#9C27B0")),
            new ColorDrawable(Color.parseColor("#673AB7")),
            new ColorDrawable(Color.parseColor("#3F51B5")),
            new ColorDrawable(Color.parseColor("#2196F3")),
            new ColorDrawable(Color.parseColor("#03A9F4")),
            new ColorDrawable(Color.parseColor("#00BCD4")),
            new ColorDrawable(Color.parseColor("#009688")),
            new ColorDrawable(Color.parseColor("#4CAF50")),
            new ColorDrawable(Color.parseColor("#8BC34A")),
            new ColorDrawable(Color.parseColor("#CDDC39")),
            new ColorDrawable(Color.parseColor("#FFEB3B")),
            new ColorDrawable(Color.parseColor("#FFC107")),
            new ColorDrawable(Color.parseColor("#FF9800")),
            new ColorDrawable(Color.parseColor("#FF5722")),
            new ColorDrawable(Color.parseColor("#795548")),
            new ColorDrawable(Color.parseColor("#9E9E9E")),
            new ColorDrawable(Color.parseColor("#607D8B")),
            new ColorDrawable(Color.parseColor("#212121"))
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.preference_theme);

        // Add the listeners to the preferences
        findPreference(getString(R.string.KEY_SETTINGS_THEME_PRIMARY)).setOnPreferenceClickListener(onPreferenceClickListener());
        findPreference(getString(R.string.KEY_SETTINGS_THEME_SECONDARY)).setOnPreferenceClickListener(onPreferenceClickListener());

        // Initialise the theme variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), R.color.colorPrimary);
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    private Preference.OnPreferenceClickListener onPreferenceClickListener() {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String prefKey = preference.getKey();

                // If 'Primary Color'
                if (prefKey.equals(getString(R.string.KEY_SETTINGS_THEME_PRIMARY))) {
                    showColorsDialog(0);
                    return true;
                }
                // If 'Secondary Color'
                else if (prefKey.equals(getString(R.string.KEY_SETTINGS_THEME_SECONDARY))) {
                    showColorsDialog(1);
                    return true;
                }
                return false;
            }
        };
    }

    private void showColorsDialog(final int setting) {
        // Prepare grid view
        GridView gridView = new GridView(this);
        final AlertDialog dialog;

        gridView.setAdapter(new ColorsAdapter(this));
        gridView.setNumColumns(4);
        gridView.setPadding(8, 16, 8, 16);
        gridView.setGravity(Gravity.CENTER);
        // Set grid view to alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(gridView);
        builder.setTitle(getString(R.string.color_dialog_title));
        dialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Drawable drawable = mThumbIds[position];
                int color = ((ColorDrawable) drawable).getColor();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsThemeActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                if (setting == 0) {
                    editor.putInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), color);
                    mPrimaryColor = color;
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
                else if (setting == 1) {
                    editor.putInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), color);
                }
                editor.apply();
                dialog.dismiss();
            }
        });
        dialog.show();
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
