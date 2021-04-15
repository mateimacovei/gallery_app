package com.example.gallery_app.adapter

import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
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
    abstract fun isSelected(): Boolean

    /**
     * enableSelectionMode() should have been previously called.
     * calls setAsSelected() is the picture is unselected, setAsUnselected() otherwise
     */
    fun reverseSelection(){
        if (isSelected())
            setAsUnselected()
        else setAsSelected()
    }

    protected fun logClickedView(v: View?){
        //important to check ImageButton first, as ImageButton extends ImageView
        when (v) {
            is ImageButton -> Log.i("Files", "short clicked ImageButton")
            is ImageView -> Log.i("Files", "short clicked ImageView")
            is CheckBox -> Log.i("Files", "short clicked CheckBox")
            else -> Log.i("Files", "short clicked unidentified")
        }
    }
}