package com.example.gallery_app.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
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
@Suppress("DEPRECATION")
class FullscreenImageActivity : AppCompatActivity(), MyFlingListener {
    private lateinit var fullscreenContent: ZoomImageView
    private lateinit var fullscreenContentControls: LinearLayout
    private var isFullscreen: Boolean = true

    private lateinit var myMediaObjectsArray: ArrayList<MyMediaObject>
    private var currentPosition: Int = 0
    private var inSplitView = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen_image)
//        setSupportActionBar(fullscreen_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.transparent, theme)))
        detail_split_view.visibility = View.GONE

        frameLayoutZoomImage.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_ImageView)
        fullscreenContent.setOnClickListener { toggle() }
        fullscreenContent.setMyFlingListener(this)
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

        hideControls()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i("Activity", "onOptionsItemSelected entered")
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.fullscreen_menu, menu)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES ->
                for (i in 0 until menu!!.size()) {
                    val item = menu.getItem(i)
                    val spanString = SpannableString(menu.getItem(i).title.toString())
                    spanString.setSpan(ForegroundColorSpan(Color.WHITE), 0, spanString.length, 0) //fix the color to white
                    item.title = spanString
                }
            Configuration.UI_MODE_NIGHT_NO -> {
            }
        }
        return true
    }

    fun detailsMenuButtonClicked(item: MenuItem){
        startDetailsActivity()
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

    private fun toggle() {
        Log.i("Activity", "toggle entry, isFullscreen:$isFullscreen")
        if (!inSplitView)
            if (isFullscreen)
                showControls()
            else
                hideControls()
    }

    private fun hideControls() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        isFullscreen = true
    }

    private fun showControls() {
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        isFullscreen = false
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

    private fun startDetailsActivity(){
        val intentDetailsPage = Intent(this, ImageDetailActivity::class.java)
        Box.Add(intentDetailsPage, IMAGE_DETAILS, this.myMediaObjectsArray[currentPosition])
        this.startActivity(intentDetailsPage)
    }

    override fun swipeUp() {
        if (!inSplitView) {
            inSplitView = true
            if (!isFullscreen)
                hideControls()
            detail_split_view.visibility = View.VISIBLE
        }
        else
            startDetailsActivity()
    }

    override fun swipeDown() {
        onBackPressed()
    }

    override fun onBackPressed() {
        if (!inSplitView)
            super.onBackPressed()
        else {
            inSplitView = false
            detail_split_view.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        //if I don't call hide(), when I go back from the detail activity, the bottom bar is still there
        hideControls()
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