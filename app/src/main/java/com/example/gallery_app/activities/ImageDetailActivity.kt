package com.example.gallery_app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.gallery_app.IMAGE_DETAILS
import com.example.gallery_app.R
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto
import kotlinx.android.synthetic.main.activity_image_detail.*


class ImageDetailActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    lateinit var photo: MyPhoto
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        this.title = "Details"

        photo = Box.Get(intent, IMAGE_DETAILS)
        Box.Remove(intent)

        textViewDate.text = photo.DATE_MODIFIED

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
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (Math.abs(velocityX) < VELOCITY_THRESHOLD
                && Math.abs(velocityY) < VELOCITY_THRESHOLD)
            return false //if the fling is not fast enough then it's just like drag

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