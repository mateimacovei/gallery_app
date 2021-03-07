package com.example.gallery_app.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gallery_app.FULLSCREEN_IMAGE_MESSAGE
import com.example.gallery_app.R
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto


private const val DEBUG_TAG = "Gestures"
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenImageActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var fullscreenContent: ImageView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(DEBUG_TAG, "ACTION DOWN")
                if (AUTO_HIDE) {
                    delayedHide(AUTO_HIDE_DELAY_MILLIS)
                }
            }
            MotionEvent.ACTION_UP -> {
                Log.i(DEBUG_TAG, "ACTION UP")
                view.performClick()
            }
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen_image)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_ImageView)
        fullscreenContent.setOnClickListener { toggle() }

        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById<Button>(R.id.dummy_button).setOnTouchListener(delayHideTouchListener)
        val picture: MyPhoto = Box.Get(intent, FULLSCREEN_IMAGE_MESSAGE)
        Box.Remove(intent)


        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .error(R.mipmap.ic_launcher_round)

        Glide.with(this)
            .load(picture.uri)
            .apply(options)
            .fitCenter()
            .into(fullscreenContent)


        val gestureDetector: GestureDetector = GestureDetector(this, this)

        fullscreenContent.setOnTouchListener(OnTouchListener(fun(
            view: View,
            event: MotionEvent
        ): Boolean {
            return gestureDetector.onTouchEvent(event)
        }))


//
//        fullscreenContent.setOnTouchListener(object : View.OnTouchListener {
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                return gestureDetector.onTouchEvent(event)
//            }
//        })
//        val mDetector = GestureDetectorCompat(this, this)
//        mDetector.setOnDoubleTapListener(this)
        toggle()    //I shuld modify the rest of onCreate to start with fullscreen mode
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(5)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
//        private const val UI_ANIMATION_DELAY = 300
        private const val UI_ANIMATION_DELAY = 300
        //if I have this lower that 300, the image will not re-center when the upper bar is hidden

        private const val VELOCITY_THRESHOLD: Long = 3000
        //TO DO: adjust this
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

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.i("Gestures", "onScroll called")
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.i("Gestures", "onLongPress called")

    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.i("Gestures", "onFling called")
//        if (Math.abs(velocityX) < VELOCITY_THRESHOLD
//                && Math.abs(velocityY) < VELOCITY_THRESHOLD) {
//            return false //if the fling is not fast enough then it's just like drag
//        }

        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX >= 0) {
                Log.i("Files", "swipe right")
                Toast.makeText(this, "swiped right", Toast.LENGTH_LONG).show()

            } else { //if velocityX is negative, then it's towards left
                Toast.makeText(this, "swiped left", Toast.LENGTH_LONG).show()
                Log.i("Files", "swipe left")
            }
        } else {
            if (velocityY >= 0) {
                Log.i("Files", "swipe down")
                onBackPressed()
            } else {
                Toast.makeText(
                    this, "swiped up", Toast.LENGTH_LONG
                ).show()
                Log.i("Files", "swipe up")
            }
        }

        return true
    }

}