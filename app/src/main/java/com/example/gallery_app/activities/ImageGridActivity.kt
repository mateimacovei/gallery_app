package com.example.gallery_app.activities

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gallery_app.*
import com.example.gallery_app.adapter.ImageGridAdapter
import com.example.gallery_app.adapter.clickListenerInterfaces.ImageItemClickListener
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto
import com.example.gallery_app.storageAccess.MyPhotoAlbum
import kotlinx.android.synthetic.main.activity_image_grid.*


class ImageGridActivity : AppCompatActivity(),
    ImageItemClickListener {
    var selectionMode: Boolean = false
    val holderImages: ArrayList<ImageGridAdapter.ImageColorViewHolder> = ArrayList()
    lateinit var album: MyPhotoAlbum

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_grid)

        val sglm = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recycleViewerForImages.layoutManager = sglm


//        val pictures: ArrayList<MyPhoto> = intent.extras?.get(
//            IMAGE_GRID_MESSAGE
//        ) as ArrayList<MyPhoto>

//        val picture: MyPhoto = intent.getSerializableExtra(
//            IMAGE_GRID_MESSAGE
//        ) as MyPhoto
//        val pictures = ArrayList<MyPhoto>()
//        pictures.add(picture)

        album = Box.Get(intent, IMAGE_GRID_MESSAGE)
        Box.Remove(intent)

        this.title=album.albumName
        loadPicturesFromAlbum()

        this.onConfigurationChanged(this.resources.configuration)

//        supportActionBar.
//        supportActionBar?.isHideOnContentScrollEnabled = true
        Log.i("Activity", "onCreate exit")
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