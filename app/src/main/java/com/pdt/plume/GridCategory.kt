package com.pdt.plume

import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView

class GridCategory {

    var category: String
    var adapter: BaseAdapter
    var listener: AdapterView.OnItemClickListener

    constructor(category: String, adapter: BaseAdapter, listener: AdapterView.OnItemClickListener) : super() {
        this.category = category
        this.adapter = adapter
        this.listener = listener
    }

}