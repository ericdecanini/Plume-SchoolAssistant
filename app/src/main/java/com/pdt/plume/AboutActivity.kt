package com.pdt.plume

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.menu_counter.*


class AboutActivity : AppCompatActivity() {

    // UI Variables
    internal var mPrimaryColor: Int = 0
    internal var mDarkColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Initialise the theme variables
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), resources.getColor(R.color.colorPrimary))
        val hsv = FloatArray(3)
        val tempColor = mPrimaryColor
        Color.colorToHSV(tempColor, hsv)
        hsv[2] *= 0.8f // value component
        mDarkColor = Color.HSVToColor(hsv)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(mPrimaryColor))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = mDarkColor
        }
    }

}
