package com.example.gallery_app.activities

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gallery_app.*
import com.example.gallery_app.adapter.ImageGridAdapter
import com.example.gallery_app.adapter.clickListenerInterfaces.ImageItemClickListener
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto
import com.example.gallery_app.storageAccess.MyPhotoAlbum
import kotlinx.android.synthetic.main.activity_image_grid.*


const val VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD: Long = 150

class ImageGridActivity : AppCompatActivity(),
    ImageItemClickListener{
    var selectionMode: Boolean = false
    val holderImages: ArrayList<ImageGridAdapter.ImageColorViewHolder> = ArrayList()
    lateinit var album: MyPhotoAlbum

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_grid)
        setSupportActionBar(image_grid_toolbar)

        val sglm = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recycleViewerForImages.layoutManager = sglm
        val onFlingListener = MyOnFlingListener()
        recycleViewerForImages.onFlingListener = onFlingListener

        album = Box.Get(intent, IMAGE_GRID_MESSAGE)
        Box.Remove(intent)

//        image_grid_toolbar.title = album.albumName

        loadPicturesFromAlbum()

        this.onConfigurationChanged(this.resources.configuration)

        Log.i("Activity", "onCreate exit")
    }

    inner class MyOnFlingListener : RecyclerView.OnFlingListener() {
        override fun onFling(velocityX: Int, velocityY: Int): Boolean {
            if (velocityY > VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD) {
                Log.i("Scroll", "scroll down")
                if(!selectionMode)
                    image_grid_toolbar.visibility = View.GONE
            }
            if (velocityY < VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD) {
                Log.i("Scroll", "scroll up")
                image_grid_toolbar.visibility = View.VISIBLE
            }
            return false //it was not handled. If I set it to true, it will not pass the event on to the inertia method
        }

    }

    private fun loadPicturesFromAlbum(){
        val pictures: ArrayList<MyPhoto> = album.photos
        var i = 0
        while (i < pictures.size && !selectionMode) {
            selectionMode = pictures[i].selected
            i++
        }
        val iga = ImageGridAdapter(this, pictures)
        iga.setClickListener(this)
        recycleViewerForImages.adapter = iga
    }

    override fun onBackPressed() {
        if (!this.selectionMode)
            super.onBackPressed()
        else {
            for (holder in holderImages)
                holder.disableSelectionMode()
            this.selectionMode = false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

//        val grid: StaggeredGridLayoutManager = recycleViewerForImages.layoutManager as StaggeredGridLayoutManager
//
//        val firstVisibleItemPositions = IntArray(100)
//        val aux: IntArray = grid.findFirstVisibleItemPositions(firstVisibleItemPositions)
//
//        Log.i("GRID", "offset: $aux, | into: $firstVisibleItemPositions")

//        val v: View? = recycleViewerForImages.layoutManager?.getChildAt(1)
//        var offset=0
//        if(v!=null)
//            offset= v.top

//        Log.i("GRID","offset: $offset")

        when (newConfig.orientation) {
            ORIENTATION_PORTRAIT -> recycleViewerForImages.layoutManager =
                    StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            ORIENTATION_LANDSCAPE -> recycleViewerForImages.layoutManager =
                    StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
            else -> { // Note the block
                Log.w(
                        "Orientation",
                        "Orientation in ImageGridKotlinActivity was undefined at configuration change"
                )
            }
        }
    }

    private fun startFullscreenActivity(imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder){
//        Toast.makeText(
//                this,
//                "Clicked picture: ${imageColorViewHolder.myPhoto}",
//                Toast.LENGTH_LONG
//        ).show()
        Log.i("Files", "Image to open: ${imageColorViewHolder.myPhoto}")
        val intentFullScreenImage = Intent(this, FullscreenImageActivity::class.java)

        Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_ARRAY, this.album.photos)
        Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_POSITION, imageColorViewHolder.photoPositionInMyArray)

        this.startActivity(intentFullScreenImage)
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onItemClick(
            view: View,
            position: Int,
            imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder
    ) {
        if (view is ImageButton) { //important to check ImageButton first, as ImageButton extends ImageView
            startFullscreenActivity(imageColorViewHolder)
        } else {
            if (selectionMode) {
                imageColorViewHolder.reverseSelection()
            } else {
                startFullscreenActivity(imageColorViewHolder)
            }
        }
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onLongItemClick(
            view: View,
            position: Int,
            imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder
    ) {
//        Toast.makeText(this, "Image LONG clicked $position", Toast.LENGTH_SHORT).show()
//        colorViewHolder?.updatePictureBySelection()
        if (selectionMode) {
            imageColorViewHolder.reverseSelection()
        } else {
            holderImages.forEach {
                run {
                    it.enableSelectionMode()
                }
            }
            imageColorViewHolder.setAsSelected()
            selectionMode = true
        }
    }

    override fun onResume() {
        Log.i("Activity", "onResume entered")
        super.onResume()

//        val refreshedAlbum = StaticMethods.getOneAlbum(this, album.albumFullPath)
//        Log.i("Files", "onResume in ImageViewGrid: found ${refreshedAlbum.albumCount} pictures")
//        this.album=refreshedAlbum
//        loadPicturesFromAlbum()
        Log.i("Activity", "onResume exit")
    }

//    override fun onActivityReenter(resultCode: Int, data: Intent?) {
//        Log.i("Activity","onActivityReenter called")
//        super.onActivityReenter(resultCode, data)
//    }

}