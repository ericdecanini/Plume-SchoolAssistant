package com.pdt.plume

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.LayoutRes
import android.support.design.widget.FloatingActionButton
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class PresetThemesAdapter(internal var c: Context, @param:LayoutRes internal var resource: Int, internal var objects: ArrayList<Pair<String, Theme>>)
    : ArrayAdapter<Pair<String, Theme>>(c, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        var holder: ViewHolder? = null
        val item = objects[position]

        if (row == null) {
            val inflater = (c as Activity).layoutInflater
            row = inflater.inflate(resource, parent, false)

            holder = ViewHolder()
            holder.title = row!!.findViewById(R.id.title) as TextView
            holder.mock_appbar = row.findViewById(R.id.mock_appbar) as TextView
            holder.mock_appbody = row.findViewById(R.id.mock_appbody) as TextView
            holder.mock_fab = row.findViewById(R.id.mock_fab) as ImageView
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        holder.title!!.text = item.first
        holder.mock_appbar!!.setBackgroundColor(item.second.primaryColour)
        holder.mock_appbody!!.setBackgroundColor(item.second.backgroundColor)
        holder.mock_appbody!!.setTextColor(item.second.textColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.mock_fab!!.backgroundTintList = ColorStateList.valueOf(item.second.secondaryColor)
        } else holder.mock_fab!!.visibility = View.GONE

        // Apply the theme variables
        val textColour = PreferenceManager.getDefaultSharedPreferences(c)
                .getInt(c.getString(R.string.KEY_THEME_TEXT_COLOUR), c.resources.getColor(R.color.mainTextColor))
        holder.title!!.setTextColor(textColour)

        return row
    }

    internal class ViewHolder {
        var title: TextView? = null
        var mock_appbar: TextView? = null
        var mock_appbody: TextView? = null
        var mock_fab: ImageView? = null
    }

}