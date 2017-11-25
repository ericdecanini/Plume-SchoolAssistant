package com.pdt.plume

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.AdapterView
import com.pdt.plume.R.string.presets
import kotlinx.android.synthetic.main.activity_preset_themes.*

class PresetThemesActivity : AppCompatActivity() {

    val LOG_TAG = PresetThemesActivity::class.java.simpleName

    // Theme variables
    var mPrimaryColor = 0
    var mDarkColor = 0
    var mBackgroundColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preset_themes)

        // Set the adapter of the gridview
        val adapter = PresetThemesAdapter(this, R.layout.grid_item_preset, presets())
        presets_grid.adapter = adapter
    }

    override fun onStart() {
        super.onStart()

        // Initialise the theme variables
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), resources.getColor(R.color.colorPrimary))
        mBackgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), resources.getColor(R.color.backgroundColor))
        val hsv = FloatArray(3)
        Color.colorToHSV(mPrimaryColor, hsv)
        hsv[2] *= 0.8f
        mDarkColor = Color.HSVToColor(hsv)

        if (supportActionBar != null)
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(mPrimaryColor))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = mDarkColor
        presets_grid.setBackgroundColor(mBackgroundColor)

         // Set the listener of the preset adapter
        presets_grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            // Save the theme to preferences
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val theme = presets()[i].second

            preferences.edit()
                    .putInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), theme.primaryColour)
                    .putInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), theme.secondaryColor)
                    .putInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), theme.backgroundColor)
                    .putInt(getString(R.string.KEY_THEME_TEXT_COLOUR), theme.textColor)
                    .apply()

            // Apply the theme to the current activity
            (presets_grid.adapter as PresetThemesAdapter).notifyDataSetChanged()
            presets_grid.setBackgroundColor(theme.backgroundColor)

            if (supportActionBar != null) {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(theme.primaryColour))
            }

            val hsv = FloatArray(3)
            Color.colorToHSV(theme.primaryColour, hsv)
            hsv[2] *= 0.8f

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = Color.HSVToColor(hsv)
            }
        }


    }

    private fun presets(): ArrayList<Pair<String, Theme>> {
        val presets = ArrayList<Pair<String, Theme>>()
        presets.add(Pair(getString(R.string.Default), Theme(-14575885,-11309570,-328966,-570425344)))
        presets.add(Pair(getString(R.string.beta), Theme(-14575885, -16725933,-328966,-570425344)))
        presets.add(Pair(getString(R.string.classify_red), Theme(-769226, -28416,-328966,-570425344)))
        presets.add(Pair(getString(R.string.material_dark), Theme(-9505855, -9872440,-11776948,-555095575)))
        presets.add(Pair(getString(R.string.notebook), Theme(-9623522, -7648180,-331828,-11326682)))
        presets.add(Pair(getString(R.string.minimal), Theme(-5000269, -6513508,-1,-568451554)))
        presets.add(Pair(getString(R.string.halloween), Theme(-1012181,-536486,-11447983,-1513240)))

        return presets
    }

}
