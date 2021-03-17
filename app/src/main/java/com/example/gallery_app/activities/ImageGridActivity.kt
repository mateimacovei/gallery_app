package com.example.gallery_app.activities

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.Log
import android.view.View
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
    //I need both, because I can have selection mode on with 0 selected
    var selectionMode: Boolean = false
    var selected = 0

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

        Log.i("Files","album being viewed: ${album.albumName}")
        this.title = album.albumName
        image_grid_toolbar.subtitle = album.photos.size.toString()
        image_grid_toolbar.navigationIcon

        loadPicturesFromAlbum()

        this.onConfigurationChanged(this.resources.configuration)

        Log.i("Activity", "onCreate exit")
    }

    private fun selectAll(){
        for(holder in holderImages)
            holder.setAsSelected()
        for(photo in album.photos)
            photo.selected=true
        //both are needed, as there could be pictures selected that are in a holder that has been removed due to scrolling

        selected=album.photos.size
        toolbarCheckBox.text=selected.toString()
    }

    private fun unselectAll()
    {
        for(holder in holderImages)
            holder.setAsUnselected()
        for(photo in album.photos)
            photo.selected=false
        selected=0
        toolbarCheckBox.text=selected.toString()
    }

    private fun enableSelectionMode(){
        selectionMode=true
        image_grid_toolbar.visibility = View.VISIBLE
        toolbarCheckBox.visibility = View.VISIBLE
        if(selected == album.photos.size)
            toolbarCheckBox.isChecked = true

        this.title=""
        image_grid_toolbar.subtitle=""
        for (holder in holderImages)
            holder.enableSelectionMode()
    }

    private fun disableSelectionMode(){
        this.title = album.albumName
        image_grid_toolbar.subtitle = album.photos.size.toString()
        selectionMode=false
        selected=0
        toolbarCheckBox.isChecked = false
        toolbarCheckBox.visibility = View.GONE
        for(holder in holderImages)
            holder.disableSelectionMode() //it's different from unselect all
        for(photo in album.photos)
            photo.selected=false
        selected=0
    }

    private fun loadPicturesFromAlbum(){
        selected=0
        for (picture in album.photos)
            if(picture.selected)
                selected++
        if(selected>0)
            enableSelectionMode()
        else{
            toolbarCheckBox.visibility = View.GONE
        }

        val iga = ImageGridAdapter(this, album.photos)
        iga.setClickListener(this)
        recycleViewerForImages.adapter = iga
    }

    inner class MyOnFlingListener : RecyclerView.OnFlingListener() {
        override fun onFling(velocityX: Int, velocityY: Int): Boolean {
            if (!selectionMode) {
                if (velocityY > VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD) {
                    Log.i("Scroll", "scroll down")
                    image_grid_toolbar.visibility = View.GONE
                }
                if (velocityY < VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD) {
                    Log.i("Scroll", "scroll up")
                    image_grid_toolbar.visibility = View.VISIBLE
                }
            }
            return false //it was not handled. If I set it to true, it will not pass the event on to the inertia method
        }

    }

    override fun onBackPressed() {
        if (!this.selectionMode)
            super.onBackPressed()
        else {
            disableSelectionMode()
        }
    }

    /**
     * sets grid_size by orientation ??TO DO have a member defining the desired column number
     */
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
        Log.i("Files", "Image to open: ${imageColorViewHolder.myPhoto}")
        val intentFullScreenImage = Intent(this, FullscreenImageActivity::class.java)

        Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_ARRAY, this.album.photos)
        Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_POSITION, imageColorViewHolder.photoPositionInMyArray)

        this.startActivity(intentFullScreenImage)
    }

    /**
     * +- selected
     * update toolbarCheckBox
     * calls imageColorViewHolder.reverseSelection()
     */
    private fun reverseSelection(imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder) {
        if (imageColorViewHolder.myPhoto.selected)
            selected--
        else selected++
        toolbarCheckBox.text = selected.toString()
        toolbarCheckBox.isChecked = (selected == album.photos.size)

        imageColorViewHolder.reverseSelection()
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
                reverseSelection(imageColorViewHolder)
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
        if (!selectionMode) {
            enableSelectionMode()
        }
        reverseSelection(imageColorViewHolder)
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

    fun toolbarCheckBoxClicked(view: View) {
        if (selectionMode) {
            if (toolbarCheckBox.isChecked)
                selectAll()
            else
                unselectAll()
        } else Log.w("Error", "checkbox was clicked outside of selectionMode")
    }

//    override fun onActivityReenter(resultCode: Int, data: Intent?) {
//        Log.i("Activity","onActivityReenter called")
//        super.onActivityReenter(resultCode, data)
//    }

}