package com.pdt.plume

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.support.annotation.LayoutRes
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder


class SettingsThemeActivity : PreferenceActivity(), Preference.OnPreferenceChangeListener {

    private var mDelegate: AppCompatDelegate? = null

    internal var mPrimaryColor: Int = 0
    internal var darkColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.installViewFactory()
        delegate.onCreate(savedInstanceState)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        super.onCreate(savedInstanceState)
        // Add 'general' preferences, defined in the XML file.
        addPreferencesFromResource(R.xml.preference_theme)

        // Add the listeners to the preferences
        findPreference(getString(R.string.KEY_SETTINGS_THEME_PRIMARY)).onPreferenceClickListener = onPreferenceClickListener()
        findPreference(getString(R.string.KEY_SETTINGS_THEME_SECONDARY)).onPreferenceClickListener = onPreferenceClickListener()
        findPreference(getString(R.string.KEY_THEME_BACKGROUND_COLOUR)).onPreferenceClickListener = onPreferenceClickListener()
        findPreference(getString(R.string.KEY_THEME_TEXT_COLOUR)).onPreferenceClickListener = onPreferenceClickListener()
        findPreference(getString(R.string.KEY_SETTINGS_THEME_PRESETS)).onPreferenceClickListener = onPreferenceClickListener()
    }

    private fun bindPreferenceSummaryToValue(preference: Preference) {
        // Set the ItemClickListener to watch for value changes.
        preference.onPreferenceChangeListener = this

        // Trigger the ItemClickListener immediately with the preference's
        // current value.
        if (preference is NumberPickerPreference)
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getInt(preference.getKey(), 0))
        else
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, "0"))
    }


    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return false
    }

    private fun onPreferenceClickListener(): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener { preference ->
            val prefKey = preference.key

            when (prefKey) {

                getString(R.string.KEY_SETTINGS_THEME_PRIMARY) -> {
                    showColorsDialog(0)
                    return@OnPreferenceClickListener true
                }

                getString(R.string.KEY_SETTINGS_THEME_SECONDARY) -> {
                    showColorsDialog(1)
                    return@OnPreferenceClickListener true
                }

                getString(R.string.KEY_THEME_BACKGROUND_COLOUR) -> {
                    showColorsDialog(2)
                    return@OnPreferenceClickListener true
                }

                getString(R.string.KEY_THEME_TEXT_COLOUR) -> {
                    showColorsDialog(3)
                    return@OnPreferenceClickListener true
                }

                getString(R.string.KEY_SETTINGS_THEME_PRESETS) -> {
                    startActivity(Intent(this, PresetThemesActivity::class.java))
                    return@OnPreferenceClickListener true
                }

                else -> false
            }
        }
    }

    private fun showColorsDialog(setting: Int) {
        // Prepare grid view
        val initialColour: Int
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()

        when (setting) {
            0 -> initialColour = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), resources.getColor(R.color.colorPrimary))
            1 -> initialColour = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), resources.getColor(R.color.colorAccent))
            2 -> initialColour = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), resources.getColor(R.color.white))
            3 -> initialColour = preferences.getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), resources.getColor(R.color.gray_900))
            else -> initialColour = preferences.getInt(getString(R.string.KEY_SETTINGS_THEME_PRIMARY), resources.getColor(R.color.colorPrimary))
        }



        ColorPickerDialogBuilder.with(this)
                .setTitle(getString(R.string.choose_colour))
                .initialColor(initialColour)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton(getString(R.string.ok), {i0, i1, i2 ->})
                .setNegativeButton(getString(R.string.cancel), { dialogInterface, i ->
                    when (setting) {
                        0 -> {
                            editor.putInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), initialColour)
                            supportActionBar.setBackgroundDrawable(ColorDrawable(initialColour))
                            val hsv = FloatArray(3)
                            Color.colorToHSV(initialColour, hsv)
                            hsv[2] *= 0.8f // value component
                            darkColor = Color.HSVToColor(hsv)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                window.statusBarColor = darkColor
                        }
                        1 -> {
                            editor.putInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), initialColour)
                            (findPreference(getString(R.string.KEY_THEME_CATEGORY_GENERAL)) as ColouredPreferenceCategory)
                                    .setColor(initialColour)
                            (findPreference(getString(R.string.KEY_THEME_CATEGORY_PLANNER)) as ColouredPreferenceCategory)
                                    .setColor(initialColour)
                            (findPreference(getString(R.string.KEY_THEME_CATEGORY_TASKS)) as ColouredPreferenceCategory)
                                    .setColor(initialColour)
                        }
                        2 -> editor.putInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), initialColour)
                        3 -> editor.putInt(getString(R.string.KEY_THEME_TEXT_COLOUR), initialColour)

                    }
                    editor.apply()
                })
                .setOnColorChangedListener { selectedColor ->
                    when (setting) {
                        0 -> {
                            editor.putInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), selectedColor)
                            supportActionBar.setBackgroundDrawable(ColorDrawable(selectedColor))
                            val hsv = FloatArray(3)
                            Color.colorToHSV(selectedColor, hsv)
                            hsv[2] *= 0.8f // value component
                            darkColor = Color.HSVToColor(hsv)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                window.statusBarColor = darkColor
                        }
                        1 -> {
                            editor.putInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), selectedColor)
                            (findPreference(getString(R.string.KEY_THEME_CATEGORY_GENERAL)) as ColouredPreferenceCategory)
                                    .setColor(selectedColor)
                            (findPreference(getString(R.string.KEY_THEME_CATEGORY_PLANNER)) as ColouredPreferenceCategory)
                                    .setColor(selectedColor)
                            (findPreference(getString(R.string.KEY_THEME_CATEGORY_TASKS)) as ColouredPreferenceCategory)
                                    .setColor(selectedColor)
                        }
                        2 -> editor.putInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), selectedColor)
                        3 -> editor.putInt(getString(R.string.KEY_THEME_TEXT_COLOUR), selectedColor)

                    }
                    editor.apply()

                }
                .build()
                .show()
    }

    /**
     * ACTION BAR METHODS
     */

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delegate.onPostCreate(savedInstanceState)
    }

    val supportActionBar: ActionBar
        get() = delegate.supportActionBar!!

    fun setSupportActionBar(toolbar: Toolbar?) {
        delegate.setSupportActionBar(toolbar)
    }

    override fun getMenuInflater(): MenuInflater {
        return delegate.menuInflater
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        delegate.setContentView(layoutResID)
    }

    override fun setContentView(view: View) {
        delegate.setContentView(view)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        delegate.setContentView(view, params)
    }

    override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        delegate.addContentView(view, params)
    }

    override fun onPostResume() {
        super.onPostResume()
        delegate.onPostResume()
    }

    override fun onTitleChanged(title: CharSequence, color: Int) {
        super.onTitleChanged(title, color)
        delegate.setTitle(title)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        delegate.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    override fun onStart() {
        super.onStart()

        // Initialise the theme variables
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), R.color.colorPrimary)
        val hsv = FloatArray(3)
        val tempColor = mPrimaryColor
        Color.colorToHSV(tempColor, hsv)
        hsv[2] *= 0.8f // value component
        darkColor = Color.HSVToColor(hsv)

        supportActionBar.setBackgroundDrawable(ColorDrawable(mPrimaryColor))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = darkColor
        }

        val secondaryColour = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), resources.getColor(R.color.colorAccent))
        (findPreference(getString(R.string.KEY_THEME_CATEGORY_GENERAL)) as ColouredPreferenceCategory)
                .setColor(secondaryColour)
        (findPreference(getString(R.string.KEY_THEME_CATEGORY_PLANNER)) as ColouredPreferenceCategory)
                .setColor(secondaryColour)
        (findPreference(getString(R.string.KEY_THEME_CATEGORY_TASKS)) as ColouredPreferenceCategory)
                .setColor(secondaryColour)
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
    }

    override fun invalidateOptionsMenu() {
        delegate.invalidateOptionsMenu()
    }

    private val delegate: AppCompatDelegate
        get() {
            if (mDelegate == null) {
                mDelegate = AppCompatDelegate.create(this, null)
            }
            return mDelegate!!
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

}
