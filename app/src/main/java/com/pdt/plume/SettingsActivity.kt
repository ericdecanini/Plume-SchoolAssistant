package com.pdt.plume

import android.annotation.TargetApi
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.support.annotation.LayoutRes
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

class SettingsActivity : PreferenceActivity(), Preference.OnPreferenceChangeListener {

    internal var LOG_TAG = SettingsActivity::class.java.simpleName

    private var mDelegate: AppCompatDelegate? = null

    internal var mPrimaryColor: Int = 0
    internal var mDarkColor: Int = 0
    internal var mSecondaryColor: Int = 0

    lateinit var mainCategory: ColouredPreferenceCategory
    lateinit var appearanceCategory: ColouredPreferenceCategory
    lateinit var blockFormatPreference: Preference
    lateinit var weekFormatPreference: Preference

    public override fun onCreate(savedInstanceState: Bundle?) {
        delegate.installViewFactory()
        delegate.onCreate(savedInstanceState)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        super.onCreate(savedInstanceState)
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.preference_main)

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_PREFERENCE_BASIS)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_PREFERENCE_WEEKTYPE)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_WEEK_NUMBER)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_SETTINGS_DATE_FORMAT)))
        bindPreferenceSummaryToValue(findPreference(getString(R.string.KEY_SETTINGS_BLOCK_FORMAT)))

        // For other preferences, simply an OnClickListener is needed
        findPreference(getString(R.string.KEY_SETTINGS_THEME)).onPreferenceClickListener = onPreferenceClickListener()
        findPreference(getString(R.string.KEY_SETTINGS_ABOUT_PLUME)).onPreferenceClickListener = onPreferenceClickListener()
        //        findPreference(getString(R.string.KEY_SETTINGS_CHANGELOG)).setOnPreferenceClickListener(onPreferenceClickListener());

        mainCategory = findPreference(getString(R.string.KEY_SETTINGS_CATEGORY_GENERAL)) as ColouredPreferenceCategory
        appearanceCategory = findPreference(getString(R.string.KEY_SETTINGS_CATEGORY_APPEARANCE)) as ColouredPreferenceCategory
        blockFormatPreference = findPreference(getString(R.string.KEY_SETTINGS_BLOCK_FORMAT))
        weekFormatPreference = findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT))

        // Disable 'Alternating Weeks' if Schedule type is block-based
        val scheduleType = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.KEY_PREFERENCE_BASIS), "0")
        if (scheduleType == "2") {
            findPreference(getString(R.string.KEY_PREFERENCE_WEEKTYPE)).isEnabled = false
            findPreference(getString(R.string.KEY_WEEK_NUMBER)).isEnabled = false
            findPreference(getString(R.string.KEY_SETTINGS_BLOCK_FORMAT)).isEnabled = true
            findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT)).isEnabled = false
        } else {
            findPreference(getString(R.string.KEY_SETTINGS_BLOCK_FORMAT)).isEnabled = false
            findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT)).isEnabled = true

            val weekType = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.KEY_PREFERENCE_WEEKTYPE), "0")
            if (weekType == "0") {
                findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT)).isEnabled = false
                findPreference(getString(R.string.KEY_WEEK_NUMBER)).isEnabled = false
            } else {
                findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT)).isEnabled = true
                findPreference(getString(R.string.KEY_WEEK_NUMBER)).isEnabled = true
            }
        }
        if (scheduleType != "0") {
            findPreference(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION)).isEnabled = false
            findPreference(getString(R.string.KEY_SETTINGS_CLASS_MUTE)).isEnabled = false
        }

    }

    override fun onStart() {
        super.onStart()

        // Initialise the theme variables
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), resources.getColor(R.color.colorPrimary))
        val hsv = FloatArray(3)
        val tempColor = mPrimaryColor
        Color.colorToHSV(tempColor, hsv)
        hsv[2] *= 0.8f // value component
        mDarkColor = Color.HSVToColor(hsv)
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), resources.getColor(R.color.colorAccent))

        supportActionBar.setBackgroundDrawable(ColorDrawable(mPrimaryColor))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = mDarkColor
        }

        mainCategory.setColor(mSecondaryColor)
        appearanceCategory.setColor(mSecondaryColor)
    }

    /**
     * Attaches a ItemClickListener so the summary is always updated with the preference value.
     * Also fires the ItemClickListener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
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

    override fun onPreferenceChange(preference: Preference, value: Any): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        val stringValue = value.toString()
        val prefKey = preference.key

        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            val listPreference = preference
            val prefIndex = listPreference.findIndexOfValue(stringValue)
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.entries[prefIndex])

                // 'Schedule type'
                if (prefKey == getString(R.string.KEY_PREFERENCE_BASIS)) {
                    // Disable 'Alternating Weeks' if Schedule type is block-based
                    val scheduleType = listPreference.entryValues[prefIndex] as String
                    if (scheduleType == "2") {
                        findPreference(getString(R.string.KEY_PREFERENCE_WEEKTYPE)).isEnabled = false
                        findPreference(getString(R.string.KEY_WEEK_NUMBER)).isEnabled = false
                        findPreference(getString(R.string.KEY_SETTINGS_BLOCK_FORMAT)).isEnabled = true
                        findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT)).isEnabled = false
                    } else {
                        findPreference(getString(R.string.KEY_PREFERENCE_WEEKTYPE)).isEnabled = true
                        findPreference(getString(R.string.KEY_WEEK_NUMBER)).isEnabled = true
                        findPreference(getString(R.string.KEY_SETTINGS_BLOCK_FORMAT)).isEnabled = false
                        findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT)).isEnabled = true
                    }
                    if (scheduleType != "0") {
                        findPreference(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION)).isEnabled = false
                        findPreference(getString(R.string.KEY_SETTINGS_CLASS_MUTE)).isEnabled = false
                    } else {
                        findPreference(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION)).isEnabled = true
                        findPreference(getString(R.string.KEY_SETTINGS_CLASS_MUTE)).isEnabled = true
                    }
                }

                if (prefKey == getString(R.string.KEY_PREFERENCE_WEEKTYPE)) {
                    val weekType = listPreference.entryValues[prefIndex] as String
                    if (weekType == "0") {
                        findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT)).isEnabled = false
                        findPreference(getString(R.string.KEY_WEEK_NUMBER)).isEnabled = false
                    } else {
                        findPreference(getString(R.string.KEY_SETTINGS_WEEK_FORMAT)).isEnabled = true
                        findPreference(getString(R.string.KEY_WEEK_NUMBER)).isEnabled = true
                    }
                }

                // If 'Week number'
                if (prefKey == getString(R.string.KEY_WEEK_NUMBER)) {
                    editor.putString(getString(R.string.KEY_WEEK_NUMBER), listPreference.entryValues[prefIndex] as String)
                } else if (prefKey == getString(R.string.KEY_SETTINGS_DATE_FORMAT)) {
                    editor.putString("dateFormat", listPreference.entryValues[prefIndex] as String)
                }// If 'Date format'
            }

        } else {
            // Roll a check for what preference has been selected
            // If 'Notification before class starts'
            if (prefKey == getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION)) {
                val numberPickerPreference = preference as NumberPickerPreference
                editor.putInt(getString(R.string.KEY_SETTINGS_CLASS_NOTIFICATION), numberPickerPreference.value)
                numberPickerPreference.summary = getString(R.string.settings_class_notification_subtitle, stringValue)
                numberPickerPreference.dialogMessage = getString(R.string.settings_class_notification_dialog_message)
            } else if (prefKey == getString(R.string.KEY_SETTINGS_CLASS_MUTE)) {
                editor.putBoolean(getString(R.string.KEY_SETTINGS_CLASS_MUTE), (preference as CheckBoxPreference).isChecked)
            } else if (prefKey == getString(R.string.KEY_SETTINGS_TASK_NOTIFICATION)) {
                editor.putBoolean(getString(R.string.KEY_SETTINGS_TASK_NOTIFICATION), (preference as CheckBoxPreference).isChecked)
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.summary = stringValue
            }// If 'Mute phone during classes'

        }

        editor.commit()
        return true
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getParentActivityIntent(): Intent? {
        return super.getParentActivityIntent()!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    private fun onPreferenceClickListener(): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener { preference ->
            val prefKey = preference.key

            // If 'Theme'
            if (prefKey == getString(R.string.KEY_SETTINGS_THEME)) {
                val intent = Intent(this@SettingsActivity, SettingsThemeActivity::class.java)
                startActivity(intent)
                return@OnPreferenceClickListener true
            } else if (prefKey == getString(R.string.KEY_SETTINGS_ABOUT_PLUME)) {
                val intent = Intent(this@SettingsActivity, AboutActivity::class.java)
                startActivity(intent)
                return@OnPreferenceClickListener true
            } else if (prefKey == getString(R.string.KEY_SETTINGS_CHANGELOG)) {
                AlertDialog.Builder(preference.context)
                        .setTitle("Version 0.4 Changelog")
                        .setMessage("+ You can now log in with Facebook\n\n" +
                                "+ You can now upload your own icons for your classes and tasks\n\n" +
                                "+ See how many peer requests you have with an unseen request counter in the side menu\n\n" +
                                "+ You can now add photos with your tasks\n")
                        .setPositiveButton(getString(R.string.ok), null)
                        .show()
            }
            false
        }
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
