package com.example.gallery_app.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.gallery_app.FULLSCREEN_IMAGE_ARRAY
import com.example.gallery_app.FULLSCREEN_IMAGE_POSITION
import com.example.gallery_app.IMAGE_DETAILS
import com.example.gallery_app.R
import com.example.gallery_app.uiClasses.gestureListeners.MyFlingListener
import com.example.gallery_app.uiClasses.gestureListeners.MyGestureListener
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyMediaObject
import kotlinx.android.synthetic.main.activity_fullscreen_image.*
import kotlinx.android.synthetic.main.activity_image_detail.*
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.gallery_app.uiClasses.imageViewer.MyZoomImageView
import com.example.gallery_app.uiClasses.imageViewer.ScreenSlidePagerAdapter
import com.example.gallery_app.uiClasses.imageViewer.ZoomImagePageFragment


class SplitScreeViewModel : ViewModel() {
    val currentSplitScreen: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
}


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@Suppress("DEPRECATION")
class FullscreenImageActivity : AppCompatActivity(), MyFlingListener {
    //    private lateinit var fullscreenContent: SubsamplingScaleImageView
    private lateinit var viewPager: ViewPager2
    private lateinit var fullscreenContentControls: LinearLayout
    private var isFullscreen: Boolean = true
    private val modelSplitScreen: SplitScreeViewModel by viewModels()

    lateinit var myMediaObjectsArray: ArrayList<MyMediaObject>

    //    private var currentPosition: Int = 0
    private var inSplitView = false


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen_image)
//        setSupportActionBar(fullscreen_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                resources.getColor(
                    R.color.transparent,
                    theme
                )
            )
        )
        myMediaObjectsArray = Box.Get(intent, FULLSCREEN_IMAGE_ARRAY)
        val currentPosition: Int = Box.Get(intent, FULLSCREEN_IMAGE_POSITION)
        Box.Remove(intent)

        detail_split_view.visibility = View.GONE
        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        frameLayoutZoomImage.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true


        viewPager = findViewById(R.id.pager)
        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.setCurrentItem(currentPosition, false)

        //touch handlers for the details layout
        detailsConstraintLayout.setOnClickListener {}
        //FUN FACT: if I don't set a separate onClickListener, it will never reach fling
        val gestureDetector2 = GestureDetector(this, MyGestureListener(this))
        detailsConstraintLayout.setOnTouchListener(View.OnTouchListener(fun(
            _: View,
            event: MotionEvent,
        ): Boolean {
            Log.i("Gestures", "details layout OnTouchListener called")
            return gestureDetector2.onTouchEvent(event)
        }))

        modelSplitScreen.currentSplitScreen.value = -1L
        hideControls()
        title = ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i("Activity", "onOptionsItemSelected entered")
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.fullscreen_menu, menu)
        return true
    }

    fun detailsMenuButtonClicked(item: MenuItem) {
        startDetailsActivity()
    }

    fun toggle() {
        Log.i("Activity", "toggle entry, isFullscreen:$isFullscreen")
//        Log.i("ZOOM","scale: ${fullscreenContent.scale}; minScale:${fullscreenContent.minScale}")
        if (!inSplitView)
            if (isFullscreen)
                showControls()
            else
                hideControls()
    }

    private fun hideControls() {
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

    private fun hideDetails() {
        inSplitView = false
        detail_split_view.visibility = View.GONE
//        loadFullscreenPicture()
        viewPager.isUserInputEnabled = true
        modelSplitScreen.currentSplitScreen.value = -1L
    }

    private fun showDetails() {
        inSplitView = true
        if (!isFullscreen)
            hideControls()
        detail_split_view.visibility = View.VISIBLE
        viewPager.isUserInputEnabled = false
        updateDetails()
        modelSplitScreen.currentSplitScreen.value = myMediaObjectsArray[viewPager.currentItem].uriId
    }

    //    these are handled by the viewPager
    override fun swipeLeft() {}
    override fun swipeRight() {}

    private fun updateDetails() {
        val mediaObject = myMediaObjectsArray[viewPager.currentItem]
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
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText(mediaObject.name, mediaObject.name)
            clipboard.setPrimaryClip(clip)
        }

        if (mediaObject.HEIGHT != null)
            (mediaObject.WIDTH + "x" + mediaObject.HEIGHT).also { textViewResolution.text = it }
        else textViewResolution.text = ""
    }

    private fun startDetailsActivity() {
        val intentDetailsPage = Intent(this, ImageDetailActivity::class.java)
        Box.Add(intentDetailsPage, IMAGE_DETAILS, this.myMediaObjectsArray[viewPager.currentItem])
        this.startActivity(intentDetailsPage)
    }

    override fun swipeUp() {
        if (!inSplitView)
            showDetails()
        else
            startDetailsActivity()
    }

    override fun swipeDown() {
        onBackPressed()
    }

    override fun onBackPressed() {
        if (!inSplitView)
            super.onBackPressed()
        else
            hideDetails()
    }

    override fun onResume() {
        super.onResume()
        //if I don't call hide(), when I go back from the detail activity, the bottom bar is still there
        hideControls()
    }

    private fun openWith() {
        val mediaObject = myMediaObjectsArray[viewPager.currentItem]
        val openIntent = Intent(Intent.ACTION_VIEW)

        if (!mediaObject.isVideo)
            openIntent.setDataAndType(mediaObject.uri, "image/*")
        else
            openIntent.setDataAndType(mediaObject.uri, "video/*")

        openIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(openIntent, "Open with"))
    }

    fun openWithMenuButtonClicked(item: MenuItem) {
        openWith()
    }

    fun editChipClicked(view: View) {
        val mediaObject = myMediaObjectsArray[viewPager.currentItem]
        val editIntent = Intent(Intent.ACTION_EDIT)

        if (!mediaObject.isVideo)
            editIntent.setDataAndType(mediaObject.uri, "image/*")
        else
            editIntent.setDataAndType(mediaObject.uri, "video/*")

        editIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(editIntent, null))
    }

    fun shareChipClicked(view: View) {
        val mediaObject = myMediaObjectsArray[viewPager.currentItem]

        val sharingIntent = Intent(Intent.ACTION_SEND)
        if (!mediaObject.isVideo)
            sharingIntent.type = "image/*"
        else
            sharingIntent.type = "video/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, mediaObject.uri)
//        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(sharingIntent, "Share image using"))

    }

    fun deleteChipClicked(view: View) {
        val currentPosition = viewPager.currentItem
        Log.i("ViewPager","currentPosition: $currentPosition")
        myMediaObjectsArray[currentPosition].uri?.let { contentResolver.delete(it, null, null) }
        if (myMediaObjectsArray.size == 1)
            onBackPressed()

        myMediaObjectsArray.removeAt(currentPosition)
        viewPager.adapter?.notifyItemRemoved(currentPosition) //TO DO make this work

//        if(currentPosition == myMediaObjectsArray.size)
//            viewPager.currentItem = currentPosition-1
//        else viewPager.currentItem = currentPosition
//        if (currentPosition == myMediaObjectsArray.size)
//            //TO DO
//        else
//            updateCurrentDisplayedPicture()
    }
}