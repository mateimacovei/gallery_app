package com.example.gallery_app.adapter.customViews

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import android.graphics.Matrix
import android.view.GestureDetector
import android.view.View
import com.example.gallery_app.activities.FullscreenImageActivity

const val VELOCITY_THRESHOLD: Long = 150
//TO DO: adjust this

open class ZoomImageView : AppCompatImageView,GestureDetector.OnGestureListener {
    var matri: Matrix? = null
    var mode = NONE

    // Remember some things for zooming
    var last = PointF()
    var start = PointF()
    var minScale = 1f
    var maxScale = 3f
    lateinit var m: FloatArray
    var viewWidth = 0
    var viewHeight = 0
    var saveScale = 1f
    protected var origWidth = 0f
    protected var origHeight = 0f
    var oldMeasuredWidth = 0
    var oldMeasuredHeight = 0
    var mScaleDetector: ScaleGestureDetector? = null
    var contex: Context? = null

    //needed for gestureListener
    var isFullscreen: Boolean = true
    lateinit var fullscreenImageActivity: FullscreenImageActivity
    //

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

        val gestureDetector: GestureDetector = GestureDetector(context, this)

        matri = Matrix()
        m = FloatArray(9)
        imageMatrix = matri
        scaleType = ScaleType.MATRIX
        setOnTouchListener { v, event ->
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
                    val xDiff = Math.abs(curr.x - start.x).toInt()
                    val yDiff = Math.abs(curr.y - start.y).toInt()
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

    fun setMaxZoom(x: Float) {
        maxScale = x
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
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
            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) matri!!.postScale(mScaleFactor, mScaleFactor, viewWidth / 2.toFloat(), viewHeight / 2.toFloat()) else matri!!.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)
            fixTrans()
            return true
        }
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

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.i("Gestures", "onFling called, isFullscreen= $isFullscreen")
        //I will pass this a refrence to the parent activity
        if (isFullscreen) {
            // callback onFling
            if (Math.abs(velocityX) < VELOCITY_THRESHOLD
                    && Math.abs(velocityY) < VELOCITY_THRESHOLD)
                return false //if the fling is not fast enough then it's just like drag

            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX >= 0) {
                    Log.i("Gestures", "swipe right")
                    fullscreenImageActivity.swipeRight()

                } else {
                    Log.i("Gestures", "swipe left")
                    fullscreenImageActivity.swipeLeft()

                }
            } else {
                if (velocityY >= 0) {
                    Log.i("Gestures", "swipe down")
                    fullscreenImageActivity.swipeDown()
                } else {
                    Log.i("Gestures", "swipe up")
                    fullscreenImageActivity.swipeUp()

                }
            }

            return true

        }
        return true
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

    fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
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

    fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        if (contentSize <= viewSize) {
            return 0f
        } else {
            return delta
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = View.MeasureSpec.getSize(widthMeasureSpec)
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
            scale = Math.min(scaleX, scaleY)
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