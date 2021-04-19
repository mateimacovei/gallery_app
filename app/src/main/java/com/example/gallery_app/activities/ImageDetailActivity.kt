package com.example.gallery_app.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gallery_app.IMAGE_DETAILS
import com.example.gallery_app.R
import com.example.gallery_app.adapter.gestureListeners.MyFlingListener
import com.example.gallery_app.adapter.gestureListeners.MyGestureListener
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyMediaObject
import kotlinx.android.synthetic.main.activity_image_detail.*
import java.util.*


class ImageDetailActivity : AppCompatActivity(), MyFlingListener {
    lateinit var photo: MyMediaObject
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        this.title = "Details"

        photo = Box.Get(intent, IMAGE_DETAILS)
        Box.Remove(intent)

        loadDetails(photo)

        detailsConstraintLayout.setOnClickListener{}
        //FUN FACT: if I don't set a separate onClickListener, it will never reach fling
        val myGestureListener = MyGestureListener(this)
        val gestureDetector = GestureDetector(this, myGestureListener)
        detailsConstraintLayout.setOnTouchListener(View.OnTouchListener(fun(
                _: View,
                event: MotionEvent
        ): Boolean {
            Log.i("Gestures", "OnTouchListener called")
            return gestureDetector.onTouchEvent(event)
        }))
    }

    private fun loadDetails(mediaObject: MyMediaObject){
        textViewDate.text = SimpleDateFormat("dd MMMM yyyy").format(
                mediaObject.DATE_MODIFIED?.toLong()?.times(1000)?.let { Date(it) })

//        textViewTitle.text = photo.name
        if (mediaObject.name.length <= 30)
            textViewTitle.text = mediaObject.name
        else
            textViewTitle.text = (mediaObject.name.subSequence(0, 30).toString() + "...")

        textViewPath.text = mediaObject.albumFullPath

//        (photo.SIZE + " b").also { textViewSize.text = it }
        // TO DO : make switch case for kb,mb; also use B, not b

        if(mediaObject.SIZE!=null) {
            var size: Double = mediaObject.SIZE!!
            val rate =1025

            if (size / rate < rate)
                ("${"%.2f".format(size / rate)} KB").also { textViewSize.text = it }
            else {
                size /= rate
                if (size / rate < rate)
                    ("${"%.2f".format(size / rate)} MB").also { textViewSize.text = it }
                else {
                    size /= rate
                    ("${"%.2f".format(size / rate)} GB").also { textViewSize.text = it }
                }
            }
        }

        chipCopyNameToClipboard.setOnClickListener {
            Toast.makeText(this, "Name copied to clipboard", Toast.LENGTH_SHORT).show()
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText(mediaObject.name, mediaObject.name)
            clipboard.setPrimaryClip(clip)
        }

//        Log.i("Data", "width: ${mediaObject.WIDTH}, height: ${mediaObject.HEIGHT}")
//        if (mediaObject.HEIGHT == null) {
//            mediaObject.reloadDimensions(this)
//            //TO DO : reload dimensions. works on android 11 for both png and gif, not on android 10
//        }
//
        if (mediaObject.HEIGHT != null)
            (mediaObject.WIDTH + "x" + mediaObject.HEIGHT).also { textViewResolution.text = it }
    }

    override fun swipeLeft() {
    }

    override fun swipeRight() {
    }

    override fun swipeUp() {
    }

    override fun swipeDown() {
        onBackPressed()
    }

}