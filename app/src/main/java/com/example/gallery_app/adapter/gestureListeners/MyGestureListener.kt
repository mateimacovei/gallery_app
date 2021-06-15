package com.example.gallery_app.adapter.gestureListeners

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

const val VELOCITY_THRESHOLD: Long = 200
//TO DO: adjust this

open class MyGestureListener(var listener : MyFlingListener? = null) : GestureDetector.OnGestureListener {

    override fun onDown(e: MotionEvent?): Boolean {
        Log.i("Gestures", "onDown called")
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.i("Gestures", "onShowPress called")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.i("Gestures", "onSingleTapUp called")
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        Log.i("Gestures", "onScroll called")
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.i("Gestures", "onLongPress called")
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.i("Gestures", "onFling called, listener: $listener")
        if (abs(velocityX) < VELOCITY_THRESHOLD
                && abs(velocityY) < VELOCITY_THRESHOLD) {
            Log.i("Gestures", "Fling below threshold, nothing done")
            return false //if the fling is not fast enough then it's just like drag
        }

        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX >= 0) {
                Log.i("Gestures", "swipe right")
                listener?.swipeRight()

            } else {
                Log.i("Gestures", "swipe left")
                listener?.swipeLeft()

            }
        } else {
            if (velocityY >= 0) {
                Log.i("Gestures", "swipe down")
                listener?.swipeDown()
            } else {
                Log.i("Gestures", "swipe up")
                listener?.swipeUp()

            }
        }

        return true
    }
}