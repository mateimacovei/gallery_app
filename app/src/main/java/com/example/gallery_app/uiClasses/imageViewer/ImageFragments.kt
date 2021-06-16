package com.example.gallery_app.uiClasses.imageViewer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.gallery_app.R
import com.example.gallery_app.activities.FullscreenImageActivity
import com.example.gallery_app.activities.SplitScreeViewModel
import com.example.gallery_app.storageAccess.MyMediaObject
import com.example.gallery_app.uiClasses.gestureListeners.MyGestureListener
import java.nio.charset.Charset
import java.security.MessageDigest


abstract class ImageFragment : Fragment() {
    lateinit var mediaObject: MyMediaObject
    lateinit var parentActivity: FullscreenImageActivity
    protected var isFullscreen = true

    protected open fun loadFullScreenPicture(forceLoad: Boolean = false) {
        Log.i("Activity", "entered loadSplitScreenPicture")
        isFullscreen = true
    }

    protected open fun loadSplitScreenPicture() {
        Log.i("Activity", "entered loadSplitScreenPicture")
        isFullscreen = false
    }

    protected var observer = Observer<Long> { id ->
        Log.i("LifeCycle", "self id: ${mediaObject.uriId}; idReceived: $id")
        if (mediaObject.uriId == id)
            loadSplitScreenPicture()
        if (id == -1L)
            loadFullScreenPicture()
    }
}


class SubsamplingImagePageFragment : ImageFragment() {
    private val modelSplitScreen: SplitScreeViewModel by activityViewModels()
    private lateinit var fullscreenContent: SubsamplingScaleImageView
    private var fullscreenFragmentLayout: MyConstraintLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.item_subsampling_image, container, false)
        fullscreenContent = view.findViewById(R.id.fullscreen_ImageView)
        val imageViewPlayButton = view.findViewById<ImageView>(R.id.imageViewPlayButton)

        loadFullScreenPicture(true)
        if (mediaObject.isVideo)
            imageViewPlayButton.setOnClickListener {
                val openIntent = Intent(Intent.ACTION_VIEW)
                if (!mediaObject.isVideo)
                    openIntent.setDataAndType(mediaObject.uri, "image/*")
                else
                    openIntent.setDataAndType(mediaObject.uri, "video/*")
                openIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(Intent.createChooser(openIntent, "Open with"))
            }
        else imageViewPlayButton.visibility = View.GONE


        fullscreenContent.maxScale = 5000.0F

        fullscreenFragmentLayout = view.findViewById(R.id.fullscreenFragmentLayout)
        fullscreenFragmentLayout?.setOnClickListener { parentActivity.toggle() }
        val gestureDetector =
            GestureDetector(parentActivity, object : MyGestureListener(parentActivity) {
                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float
                ): Boolean {
                    if (fullscreenContent.scale - fullscreenContent.minScale < 0.001F || !isFullscreen)
                        return super.onFling(e1, e2, velocityX, velocityY)
                    return false
                }
            })

        fullscreenFragmentLayout?.setOnClickListener {}
        fullscreenFragmentLayout?.gestureDetector = gestureDetector
        //the setOnClickListener is the same fun fact as the others
        //I need to call gestureDetector.onTouchEvent(event) both in setOnTouchListener and in onInterceptTouchEvent
        fullscreenFragmentLayout?.setOnTouchListener(
            View.OnTouchListener(
                fun(
                    _: View,
                    event: MotionEvent,
                ): Boolean {
                    Log.i("Activity","entered fullscreenFragmentLayout.TouchListener")
                    gestureDetector.onTouchEvent(event)
                    return false
                })
        )

        modelSplitScreen.currentSplitScreen.observe(viewLifecycleOwner, observer)
        return view
    }

    override fun loadFullScreenPicture(forceLoad: Boolean) {
        if (!isFullscreen || forceLoad) {
            super.loadFullScreenPicture(forceLoad)
            fullscreenContent.isZoomEnabled = true
            fullscreenFragmentLayout?.intercept = false
            if(forceLoad){
                if (!mediaObject.isVideo) {
                    mediaObject.uri?.let {
                        ImageSource.uri(it)
                    }?.let { fullscreenContent.setImage(it) }
                } else {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, mediaObject.uri)
                    val bmFrame = mediaMetadataRetriever.frameAtTime
                    bmFrame?.let { ImageSource.bitmap(it) }?.let { fullscreenContent.setImage(it) }
                }
            }
        }
    }

    private var oldCenter: PointF? = null
    private val handler = Handler(Looper.getMainLooper())
    private val loadSplitScreenPictureRunnable = Runnable {
        Log.i("Activity", "entered loadSplitScreenPictureRunnable")
        Log.i("Center","oldCenter: $oldCenter; center = ${fullscreenContent.center}")
        fullscreenContent.setScaleAndCenter(fullscreenContent.scale, oldCenter)
        fullscreenFragmentLayout?.intercept = true
    }


    override fun loadSplitScreenPicture() {
        if (isFullscreen) {
            super.loadSplitScreenPicture()
            fullscreenContent.isZoomEnabled = false

            oldCenter = fullscreenContent.center
//            fullscreenContent.center
            handler.post(loadSplitScreenPictureRunnable)
        }
    }

//        companion object {
//            fun create(mediaObject: MyMediaObject) =
//                SubsamplingImagePageFragment().apply {
//                    arguments = Bundle(1).apply {
//                        putStringArrayList(KEY_Small_MY_MEDIA_OBJECT, mediaObject.toStringArrayListForSmallMyMediaObj())
//                    }
//                }
//        }
}

class ZoomImagePageFragment : ImageFragment() {
    private val modelSplitScreen: SplitScreeViewModel by activityViewModels()
    private lateinit var fullscreenContent: MyZoomImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.item_zoom_image, container, false)
        fullscreenContent = view.findViewById(R.id.fullscreen_ImageView)
        loadFullScreenPicture(true)
        fullscreenContent.setOnClickListener { parentActivity.toggle() }
        fullscreenContent.setMyFlingListener(parentActivity)

        modelSplitScreen.currentSplitScreen.observe(viewLifecycleOwner, observer)
        return view
    }

    override fun loadFullScreenPicture(forceLoad: Boolean) {
        if (!isFullscreen || forceLoad) {
            super.loadFullScreenPicture(forceLoad)
            Glide.with(this)
                .load(mediaObject.uri)
                .error(R.mipmap.ic_launcher_round)
//                .fitCenter()
                .into(fullscreenContent)
        }
    }


    private val handler = Handler(Looper.getMainLooper())

    inner class MyGlideTransformation : BitmapTransformation() {
        private val id: String = "com.bumptech.glide.transformations.MyGlideTransformation"
        private val idBytes: ByteArray = id.toByteArray(Charset.forName("UTF-8"))

        override fun equals(other: Any?): Boolean {
            return other is MyGlideTransformation
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(idBytes)
        }

        override fun transform(
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int
        ): Bitmap {
            Log.i("MyGlideTransformation", "outWidth: $outWidth, outHeight: $outHeight")
//            Log.i("MyGlideTransformation","original bitmap: width:${toTransform.width}, height: ${toTransform.height}")
            val centerFitted =
                TransformationUtils.fitCenter(pool, toTransform, outWidth, outHeight)
//            Log.i("MyGlideTransformation","centerFitted bitmap: width:${centerFitted.width}, height: ${centerFitted.height}")

            if (centerFitted.height < outHeight)
                return centerFitted
            return TransformationUtils.centerCrop(pool, centerFitted, outWidth, outHeight)
        }
    }

    private val loadSplitScreenPictureRunnable = Runnable {
        Log.i("Activity", "entered loadSplitScreenPictureRunnable")
        Glide.with(this)
            .load(mediaObject.uri)
            .transform(MyGlideTransformation())
            .error(R.mipmap.ic_launcher_round)
            .into(fullscreenContent)
    }

    override fun loadSplitScreenPicture() {
        if (isFullscreen) {
            super.loadSplitScreenPicture()
            handler.post(loadSplitScreenPictureRunnable)
        }
    }
}