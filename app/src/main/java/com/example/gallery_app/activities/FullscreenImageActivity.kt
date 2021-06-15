package com.example.gallery_app.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
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
import com.example.gallery_app.adapter.gestureListeners.MyFlingListener
import com.example.gallery_app.adapter.gestureListeners.MyGestureListener
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyMediaObject
import kotlinx.android.synthetic.main.activity_fullscreen_image.*
import kotlinx.android.synthetic.main.activity_image_detail.*
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*


private const val DEBUG_TAG_GESTURES = "Gestures"

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@Suppress("DEPRECATION")
class FullscreenImageActivity : AppCompatActivity(), MyFlingListener {
    private lateinit var fullscreenContent: SubsamplingScaleImageView
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
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                resources.getColor(
                    R.color.transparent,
                    theme
                )
            )
        )
        detail_split_view.visibility = View.GONE
        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        frameLayoutZoomImage.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_ImageView)
        //astea sunt pt  MyZoomImageView
//        fullscreenContent.setOnClickListener { toggle() }
//        fullscreenContent.setMyFlingListener(this)
        fullscreenContent.maxScale = 5000.0F
        fullscreenContent.setOnClickListener { toggle() }


        val gestureDetector = GestureDetector(this, object : MyGestureListener(this) {
            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float
            ): Boolean {
                if (fullscreenContent.scale - fullscreenContent.minScale < 0.001F )
                    return super.onFling(e1, e2, velocityX, velocityY)
                return false
            }
        })
        fullscreenContent.setOnTouchListener(View.OnTouchListener(fun(
            _: View,
            event: MotionEvent,
        ): Boolean {
            gestureDetector.onTouchEvent(event)
            return false
        }))


        detailsConstraintLayout.setOnClickListener {}
        //FUN FACT: if I don't set a separate onClickListener, it will never reach fling
        val gestureDetector2 = GestureDetector(this, MyGestureListener(this))
        detailsConstraintLayout.setOnTouchListener(View.OnTouchListener(fun(
            _: View,
            event: MotionEvent,
        ): Boolean {
            Log.i("Gestures", "details fragment OnTouchListener called")
            return gestureDetector2.onTouchEvent(event)
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
        return true
    }

    fun detailsMenuButtonClicked(item: MenuItem) {
        startDetailsActivity()
    }


    private val handler = Handler(Looper.getMainLooper())

    private fun loadFullscreenPicture() {
        Log.i("Activity", "entered loadFullscreenPicture")
        if (!myMediaObjectsArray[currentPosition].isVideo) {
            myMediaObjectsArray[currentPosition].uri?.let {
                ImageSource.uri(it)
            }?.let { fullscreenContent.setImage(it) }
        } else {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(this, myMediaObjectsArray[currentPosition].uri)
            val bmFrame = mediaMetadataRetriever.frameAtTime
            bmFrame?.let { ImageSource.bitmap(it) }?.let { fullscreenContent.setImage(it) }
        }
//        Glide.with(this)
//                .load(myMediaObjectsArray[currentPosition].uri)
//                .error(R.mipmap.ic_launcher_round)
////                .fitCenter()
//                .into(fullscreenContent)
    }

    inner class MyGlideTransformation : BitmapTransformation() {
        private val id: String = "com.bumptech.glide.transformations.MyGlideTransformation"
        private val idBytes: ByteArray = id.toByteArray(Charset.forName("UTF-8"))

        override fun equals(o: Any?): Boolean {
            return o is MyGlideTransformation
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(idBytes);
        }

        override fun transform(
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int
        ): Bitmap {
            Log.i("MyGlideTransformation", "outWidth: $outWidth, outHeight: $outHeight")
//            Log.i("MyGlideTransformation","original bitmap: width:${toTransform.width}, height: ${toTransform.height}")
            val centerFitted = TransformationUtils.fitCenter(pool, toTransform, outWidth, outHeight)
//            Log.i("MyGlideTransformation","centerFitted bitmap: width:${centerFitted.width}, height: ${centerFitted.height}")

            if (centerFitted.height < outHeight)
                return centerFitted
            return TransformationUtils.centerCrop(pool, centerFitted, outWidth, outHeight)
        }
    }

    private val loadSplitScreenPictureRunnable = Runnable {
        Log.i("Activity", "entered loadSplitScreenPictureRunnable")
        if (!myMediaObjectsArray[currentPosition].isVideo) {
            myMediaObjectsArray[currentPosition].uri?.let {
                ImageSource.uri(it)
            }?.let { fullscreenContent.setImage(it) }
        } else {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(this, myMediaObjectsArray[currentPosition].uri)
            val bmFrame = mediaMetadataRetriever.frameAtTime
            bmFrame?.let { ImageSource.bitmap(it) }?.let { fullscreenContent.setImage(it) }
        }
//        Glide.with(this)
//                .load(myMediaObjectsArray[currentPosition].uri)
//                .transform(MyGlideTransformation())
//                .error(R.mipmap.ic_launcher_round)
//                .into(fullscreenContent)
    }

    private fun loadSplitScreenPicture() {
        handler.post(loadSplitScreenPictureRunnable)
    }

    private fun updateCurrentDisplayedPicture() {
        Log.i("Activity", "updating current picture, inSplitView:$inSplitView")
//        this.title = myPhotoArray[currentPosition].name
        title = ""

        if (myMediaObjectsArray[currentPosition].isVideo)
            imageViewPlayButton.visibility = View.VISIBLE
        else
            imageViewPlayButton.visibility = View.GONE

        if (!inSplitView)
            loadFullscreenPicture()
        else loadSplitScreenPicture()
        updateDetails()
    }

    private fun toggle() {
        Log.i("Activity", "toggle entry, isFullscreen:$isFullscreen")
        Log.i("ZOOM","scale: ${fullscreenContent.scale}; minScale:${fullscreenContent.minScale}")
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

    private fun hideDetails() {
        inSplitView = false
        detail_split_view.visibility = View.GONE
        loadFullscreenPicture()
    }

    private fun showDetails() {
        inSplitView = true
        if (!isFullscreen)
            hideControls()
        detail_split_view.visibility = View.VISIBLE
        loadSplitScreenPicture()
    }

    /**
     * goes to the next image
     */
    override fun swipeLeft() {
        if (currentPosition < myMediaObjectsArray.size - 1 && !inSplitView) {
            currentPosition++
            updateCurrentDisplayedPicture()
        }
    }

    /**
     * goes to the previous image
     */
    override fun swipeRight() {
        if (currentPosition > 0 && !inSplitView) {
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
        Box.Add(intentDetailsPage, IMAGE_DETAILS, this.myMediaObjectsArray[currentPosition])
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
        val uri = myMediaObjectsArray[currentPosition].uri
        val openIntent = Intent(Intent.ACTION_VIEW)

        if (!myMediaObjectsArray[currentPosition].isVideo)
            openIntent.setDataAndType(uri, "image/*")
        else
            openIntent.setDataAndType(uri, "video/*")

        openIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//        startActivity(openIntent)
        startActivity(Intent.createChooser(openIntent, "Open with"))
    }

    fun openWithMenuButtonClicked(item: MenuItem) {
        openWith()
    }

    fun editChipClicked(view: View) {
        val uri = myMediaObjectsArray[currentPosition].uri

        val editIntent = Intent(Intent.ACTION_EDIT)

        if (!myMediaObjectsArray[currentPosition].isVideo)
            editIntent.setDataAndType(uri, "image/*")
        else
            editIntent.setDataAndType(uri, "video/*")

        editIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(editIntent, null))
    }

    fun shareChipClicked(view: View) {
        val uri = myMediaObjectsArray[currentPosition].uri

        val sharingIntent = Intent(Intent.ACTION_SEND)
        if (!myMediaObjectsArray[currentPosition].isVideo)
            sharingIntent.type = "image/*"
        else
            sharingIntent.type = "video/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
//        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(sharingIntent, "Share image using"))

    }

    fun deleteChipClicked(view: View) {
        myMediaObjectsArray[currentPosition].uri?.let { contentResolver.delete(it, null, null) }
        if (myMediaObjectsArray.size == 1)
            onBackPressed()
        myMediaObjectsArray.removeAt(currentPosition)
        if (currentPosition == myMediaObjectsArray.size)
            swipeRight()
        else
            updateCurrentDisplayedPicture()
    }

    fun imagePlayButtonClicked(view: View) {
        openWith()
    }
}