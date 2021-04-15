package com.example.gallery_app.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractMediaObjectHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener,
    View.OnLongClickListener {


    abstract val imageView: ImageView
    abstract val checkBox: CheckBox

    init {
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)

    }

    abstract fun disableSelectionMode()
    abstract fun enableSelectionMode()
    abstract fun setAsSelected()
    abstract fun setAsUnselected()
}