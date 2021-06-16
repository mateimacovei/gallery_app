package com.example.gallery_app.uiClasses.imageViewer

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

class MyConstraintLayout : ConstraintLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context,attrs: AttributeSet?) : super(context,attrs) {}

    constructor(context: Context,attrs: AttributeSet?, defStyleAttr: Int) : super(context,attrs,defStyleAttr) {}

    var intercept = false
    var gestureDetector: GestureDetector? = null
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.i("Activity","entered onInterceptTouchEvent")
        gestureDetector?.onTouchEvent(ev)
        if(!intercept)
            return false
        return true
    }
}