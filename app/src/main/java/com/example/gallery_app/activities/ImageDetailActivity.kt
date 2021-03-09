package com.example.gallery_app.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.gallery_app.IMAGE_DETAILS
import com.example.gallery_app.R
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto
import kotlinx.android.synthetic.main.activity_image_detail.*
import java.time.ZoneId
import java.util.*


class ImageDetailActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    lateinit var photo: MyPhoto
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        this.title = "Details"

        photo = Box.Get(intent, IMAGE_DETAILS)
        Box.Remove(intent)

        textViewDate.text = SimpleDateFormat("dd MMMM yyyy").format(
                photo.DATE_MODIFIED?.toLong()?.times(1000)?.let { Date(it) })

        textViewTitle.text = photo.name

        textViewPath.text = photo.albumFullPath

        (photo.SIZE + " Kb").also { textViewSize.text = it }
        // TO DO : make switch case for kb,mb; also use B, not b

        Log.i("Data", "width: ${photo.WIDTH}, height: ${photo.HEIGHT}")
        if (photo.HEIGHT == null) {
            photo.reloadDimensions(this)
            //TO DO : reload dimensions. works on android 11 for both png and gif, not on android 10
        }

        if (photo.HEIGHT != null)
            (photo.WIDTH + "x" + photo.HEIGHT).also { textViewResolution.text = it }

        detailsConstraintLayout.setOnClickListener{nothing()}
        //FUN FACT: if I don't set a separate onClickListener, it will never reach fling
        val gestureDetector: GestureDetector = GestureDetector(this, this)
        detailsConstraintLayout.setOnTouchListener(View.OnTouchListener(fun(
                view: View,
                event: MotionEvent
        ): Boolean {
            Log.i("Gestures", "OnTouchListener called")
            return gestureDetector.onTouchEvent(event)
        }))
    }

    private fun nothing(){

    }

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

    // TO DO : not working
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
//        if (Math.abs(velocityX) < VELOCITY_THRESHOLD
//                && Math.abs(velocityY) < VELOCITY_THRESHOLD)
//            return false //if the fling is not fast enough then it's just like drag
        Log.i("Gestures", "onFling")

        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX >= 0) {
                Log.i("Gestures", "swipe right")
            } else { //if velocityX is negative, then it's towards left
                Log.i("Gestures", "swipe left")
            }
        } else {
            if (velocityY >= 0) {
                Log.i("Gestures", "swipe down")
                onBackPressed()
            } else {
                Log.i("Gestures", "swipe up")
            }
        }

        return true
    }


}