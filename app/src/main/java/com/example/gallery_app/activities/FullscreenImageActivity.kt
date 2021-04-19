package com.example.gallery_app.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gallery_app.FULLSCREEN_IMAGE_ARRAY
import com.example.gallery_app.FULLSCREEN_IMAGE_POSITION
import com.example.gallery_app.IMAGE_DETAILS
import com.example.gallery_app.R
import com.example.gallery_app.adapter.customViews.ZoomImageView
import com.example.gallery_app.adapter.gestureListeners.MyFlingListener
import com.example.gallery_app.adapter.gestureListeners.MyGestureListener
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyMediaObject
import com.example.gallery_app.storageAccess.MyPhoto
import com.example.gallery_app.storageAccess.MyVideo
import kotlinx.android.synthetic.main.activity_fullscreen_image.*
import kotlinx.android.synthetic.main.activity_image_detail.*
import java.util.*
import kotlin.collections.ArrayList


private const val DEBUG_TAG = "Gestures"

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenImageActivity : AppCompatActivity(), MyFlingListener {
    private lateinit var fullscreenContent: ZoomImageView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    private lateinit var myMediaObjectsArray: ArrayList<MyMediaObject>
    private var currentPosition: Int = 0
    private var inSplitView = false

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
        detail_split_view.visibility = View.GONE

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_ImageView)
        fullscreenContent.setOnClickListener { toggle() }
        fullscreenContent.setListener(this)
        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

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

        myMediaObjectsArray = Box.Get(intent, FULLSCREEN_IMAGE_ARRAY)
        currentPosition = Box.Get(intent, FULLSCREEN_IMAGE_POSITION)
        Box.Remove(intent)

        updateCurrentDisplayedPicture()

//        val gestureDetector: GestureDetector = GestureDetector(this, this)
//        fullscreenContent.setOnTouchListener(OnTouchListener(fun(
//            view: View,
//            event: MotionEvent
//        ): Boolean {
//            return gestureDetector.onTouchEvent(event)
//        }))

        toggle()    //I shuld modify the rest of onCreate to start with fullscreen mode
    }

    private fun updateCurrentDisplayedPicture() {
        Log.i("Activity", "updating current picture")
//        this.title = myPhotoArray[currentPosition].name
        title = ""

        if (myMediaObjectsArray[currentPosition] is MyVideo)
            imageViewPlayButton.visibility = View.VISIBLE
        else
            imageViewPlayButton.visibility = View.GONE

        val options: RequestOptions = RequestOptions()
                .centerCrop()
                .error(R.mipmap.ic_launcher_round)
        Glide.with(this)
                .load(myMediaObjectsArray[currentPosition].uri)
                .apply(options)
                .fitCenter()
                .into(fullscreenContent)
        updateDetails()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(0)
    }

    private fun toggle() {
        if (!inSplitView)
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
    }

    /**
     * goes to the next image
     */
    override fun swipeLeft() {
        if (currentPosition < myMediaObjectsArray.size - 1) {
            currentPosition++
            updateCurrentDisplayedPicture()
        }
    }

    /**
     * goes to the previous image
     */
    override fun swipeRight() {
        if (currentPosition > 0) {
            currentPosition--
            updateCurrentDisplayedPicture()
        }
    }

    private fun updateDetails() {
        val mediaObject = myMediaObjectsArray[currentPosition]
        textViewDate.text = SimpleDateFormat("dd MMMM yyyy").format(
                mediaObject.DATE_MODIFIED?.toLong()?.times(1000)?.let { Date(it) })

        if (mediaObject.name.length <= 30)
            textViewTitle.text = mediaObject.name
        else
            textViewTitle.text = (mediaObject.name.subSequence(0, 30).toString() + "...")

        textViewPath.text = mediaObject.albumFullPath

        if (mediaObject.SIZE != null) {
            var size: Double = mediaObject.SIZE!!
            val rate = 1025

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

        if (mediaObject.HEIGHT != null)
            (mediaObject.WIDTH + "x" + mediaObject.HEIGHT).also { textViewResolution.text = it }
        else textViewResolution.text = ""
    }

    override fun swipeUp() {
        if (!inSplitView) {
            inSplitView = true
            if (isFullscreen)
                hide()
            detail_split_view.visibility = View.VISIBLE
        }
        else {
            val intentDetailsPage = Intent(this, ImageDetailActivity::class.java)
            Box.Add(intentDetailsPage, IMAGE_DETAILS, this.myMediaObjectsArray[currentPosition])
            this.startActivity(intentDetailsPage)
        }
    }

    override fun swipeDown() {
        if (!inSplitView)
            onBackPressed()
        else {
            inSplitView = false
            detail_split_view.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        //if I don't call hide(), when I go back from the detail activity, the bottom bar is still there
        hide()
    }

    fun leftChipClicked(view: View) {
        val uri = myMediaObjectsArray[currentPosition].uri

        val editIntent = Intent(Intent.ACTION_EDIT)

        if (myMediaObjectsArray[currentPosition] is MyPhoto)
//            editIntent.setDataAndType(uri, "image/*")
            editIntent.type = "image/*"
        else
//            editIntent.setDataAndType(uri, "video/*")
            editIntent.type = "video/*"

        editIntent.putExtra(Intent.EXTRA_STREAM, uri)
        editIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(editIntent, null))
    }

    fun middleChipClicked(view: View) {
        val uri = myMediaObjectsArray[currentPosition].uri

        val sharingIntent = Intent(Intent.ACTION_SEND)
        if (myMediaObjectsArray[currentPosition] is MyPhoto)
            sharingIntent.type = "image/*"
        else
            sharingIntent.type = "video/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
//        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(sharingIntent, "Share image using"))

    }

    fun deleteChipClicked(view: View) {
        contentResolver.delete(myMediaObjectsArray[currentPosition].uri, null, null)
        if (myMediaObjectsArray.size == 1)
            onBackPressed()
        myMediaObjectsArray.removeAt(currentPosition)
        if (currentPosition == myMediaObjectsArray.size)
            swipeRight()
        else
            updateCurrentDisplayedPicture()
    }

    fun imagePlayButtonClicked(view: View) {
        val uri = myMediaObjectsArray[currentPosition].uri
        val playIntent = Intent(Intent.ACTION_VIEW)
//        playIntent.type = "video/*"
//        playIntent.putExtra(Intent.EXTRA_STREAM, uri)
//        startActivity(Intent.createChooser(playIntent, "Play video using"))
        playIntent.setDataAndType(uri, "video/*");
        startActivity(playIntent);
    }
}