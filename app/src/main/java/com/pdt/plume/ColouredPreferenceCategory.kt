package com.pdt.plume

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceCategory
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

class ColouredPreferenceCategory : PreferenceCategory {

    internal var c: Context
    lateinit var v: TextView
    private var initialised = false

    constructor(context: Context) : super(context) {
        c = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        c = context
    }

    constructor(context: Context, attrs: AttributeSet,
                defStyle: Int) : super(context, attrs, defStyle) {
        c = context
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        val titleView = view.findViewById(android.R.id.title) as TextView
        v = titleView
        this.initialised = true
        val color: Int
        val preferences = PreferenceManager.getDefaultSharedPreferences(c)
        color = preferences.getInt(c.getString(R.string.KEY_THEME_SECONDARY_COLOR), R.color.colorAccent)
        titleView.setTextColor(color)
    }

    fun setColor(color: Int) {
        if (this.initialised)
            this.v.setTextColor(color)
    }

}
