package com.example.gallery_app.uiClasses.imageViewer

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatImageView
import com.example.gallery_app.uiClasses.gestureListeners.MyFlingListener
import com.example.gallery_app.uiClasses.gestureListeners.MyGestureListener
import kotlin.math.abs


open class MyZoomImageView : AppCompatImageView {
    var matri: Matrix? = null
    var mode = NONE

    // Remember some things for zooming
    private var last = PointF()
    private var start = PointF()
    var minScale = 1f
    var maxScale = 60f
    private lateinit var m: FloatArray
    var viewWidth = 0
    var viewHeight = 0
    var saveScale = 1f
    protected var origWidth = 0f
    protected var origHeight = 0f
    private var oldMeasuredWidth = 0
    private var oldMeasuredHeight = 0
    private var mScaleDetector: ScaleGestureDetector? = null
    private var contex: Context? = null

    //needed for gestureListener
    var isFullscreen: Boolean = true
    private val zoomMyGestureListener: ZoomMyGestureListener = ZoomMyGestureListener()


    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        sharedConstructing(context)
    }

    private fun sharedConstructing(context: Context) {
        super.setClickable(true)
        this.contex = context
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())

        val gestureDetector = GestureDetector(context, zoomMyGestureListener)

        matri = Matrix()
        m = FloatArray(9)
        imageMatrix = matri
        scaleType = ScaleType.MATRIX
        setOnTouchListener { _, event ->
            mScaleDetector!!.onTouchEvent(event)
            val curr = PointF(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }
                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    val deltaX = curr.x - last.x
                    val deltaY = curr.y - last.y
                    val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(), origWidth * saveScale)
                    val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(), origHeight * saveScale)
                    matri!!.postTranslate(fixTransX, fixTransY)
                    fixTrans()
                    last[curr.x] = curr.y
                }
                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = abs(curr.x - start.x).toInt()
                    val yDiff = abs(curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK) performClick()
                }
                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            imageMatrix = matri
            gestureDetector.onTouchEvent(event)
            invalidate()

            true // indicate event was handled
        }
    }

    fun setMyFlingListener(listener: MyFlingListener) {
        zoomMyGestureListener.listener = listener
    }

    fun setMaxZoom(x: Float) {
        maxScale = x
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return scale(detector.scaleFactor, detector.focusX,detector.focusY)
        }
    }

    private fun scale(mScaleFactorr: Float, focusX: Float, focusY: Float): Boolean{
//        Log.i("Gestures","resize: ${mScaleFactorr}, ${focusX},${focusY}")
        var mScaleFactor = mScaleFactorr
        val origScale = saveScale
        saveScale *= mScaleFactor
        isFullscreen = false
        if (saveScale > maxScale) {
            saveScale = maxScale
            mScaleFactor = maxScale / origScale
        } else if (saveScale < minScale) {
            isFullscreen = true
            saveScale = minScale
            mScaleFactor = minScale / origScale
        }
        if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) matri!!.postScale(mScaleFactor, mScaleFactor, viewWidth / 2.toFloat(), viewHeight / 2.toFloat()) else matri!!.postScale(mScaleFactor, mScaleFactor, focusX, focusY)
        fixTrans()
        return true
    }

    inner class ZoomMyGestureListener() : MyGestureListener() {
        private val timeOut = ViewConfiguration.getDoubleTapTimeout()
        private var lastTapTime: Long = 0

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            Log.i("Gestures", "onFling called, isFullscreen= $isFullscreen")
            if (isFullscreen)
                super.onFling(e1, e2, velocityX, velocityY)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            val tapTime = System.currentTimeMillis()
            if(tapTime - lastTapTime<timeOut){
                Log.i("Gestures", "double tap")
                return false
            }
            else
                lastTapTime=tapTime
            return false
        }
    }

//    }

    fun fixTrans() {
        matri!!.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]
        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), origWidth * saveScale)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), origHeight * saveScale)
        if (fixTransX != 0f || fixTransY != 0f) matri!!.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        if (trans < minTrans) return -trans + minTrans
        if (trans > maxTrans) return -trans + maxTrans
        return 0f
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            0f
        } else {
            delta
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight || viewWidth == 0 || viewHeight == 0) return
        oldMeasuredHeight = viewHeight
        oldMeasuredWidth = viewWidth
        if (saveScale == 1f) {
            //Fit to screen.
            val scale: Float
            val drawable = drawable
            if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) return
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight
            Log.d("bmSize", "bmWidth: $bmWidth bmHeight : $bmHeight")
            val scaleX = viewWidth.toFloat() / bmWidth.toFloat()
            val scaleY = viewHeight.toFloat() / bmHeight.toFloat()
            scale = scaleX.coerceAtMost(scaleY)
            matri!!.setScale(scale, scale)
            // Center the image
            var redundantYSpace = viewHeight.toFloat() - scale * bmHeight.toFloat()
            var redundantXSpace = viewWidth.toFloat() - scale * bmWidth.toFloat()
            redundantYSpace /= 2.toFloat()
            redundantXSpace /= 2.toFloat()
            matri!!.postTranslate(redundantXSpace, redundantYSpace)
            origWidth = viewWidth - 2 * redundantXSpace
            origHeight = viewHeight - 2 * redundantYSpace
            imageMatrix = matri
        }
        fixTrans()
    }

    companion object {
        // We can be in one of these states
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
        const val CLICK = 3
    }
}