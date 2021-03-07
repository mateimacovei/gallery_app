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
import com.example.gallery_app.FULLSCREEN_IMAGE_MESSAGE
import com.example.gallery_app.IMAGE_GRID_MESSAGE
import com.example.gallery_app.R
import com.example.gallery_app.adapter.ImageGridAdapter
import com.example.gallery_app.adapter.clickListenerInterfaces.ImageItemClickListener
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto
import com.example.gallery_app.storageAccess.MyPhotoAlbum
import kotlinx.android.synthetic.main.activity_image_grid.*


class ImageGridActivity : AppCompatActivity(),
    ImageItemClickListener {
    var selectionMode: Boolean = false
    val holders: ArrayList<ImageGridAdapter.ColorViewHolder> = ArrayList()

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

        val album : MyPhotoAlbum = Box.Get(intent, IMAGE_GRID_MESSAGE)
        this.title=album.albumName

        val pictures: ArrayList<MyPhoto> = album.photos
        Box.Remove(intent)

        Log.i("Files", "after getting pictures from intent in ImageGridActivity")
//        val message = intent?.extras?.getString(IMAGE_GRID_MESSAGE).toString()


//        val pictures = ImageStorageAccess.getAllPictures(this)
//        Toast.makeText(this, "Found ${pictures.size} pictures. Message: $message", Toast.LENGTH_LONG).show()

        var i = 0
        while (i < pictures.size && !selectionMode) {
            selectionMode = pictures[i].selected
            i++
        }
        val iga = ImageGridAdapter(this, pictures)
        iga.setClickListener(this)
        recycleViewerForImages.adapter = iga

        this.onConfigurationChanged(this.resources.configuration)

//        supportActionBar.
//        supportActionBar?.isHideOnContentScrollEnabled = true
    }

    override fun onBackPressed() {
        if (!this.selectionMode)
            super.onBackPressed()
        else {
            for (holder in holders)
                holder.disableSelectionMode()
            this.selectionMode = false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

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

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onItemClick(
        view: View,
        position: Int,
        colorViewHolder: ImageGridAdapter.ColorViewHolder
    ) {
//        Toast.makeText(this, "Image SHORT clicked $position", Toast.LENGTH_SHORT).show()
//        colorViewHolder?.updatePictureBySelection()
        if (view is ImageButton) { //important sa verific ImageButton primul, pt ca ImageButton extinde ImageView si se poate confunda
            Toast.makeText(
                this,
                "Clicked picture: ${colorViewHolder.myPhoto}",
                Toast.LENGTH_LONG
            ).show()
            //am lasat pe cazuri separate pt ca pe viitor tr sa transmit la fullscreenView daca se permite editarea sau nu
            //

            Log.i("Files", "Image to open: ${colorViewHolder.myPhoto}")

            val intentFullScreenImage = Intent(this, FullscreenImageActivity::class.java)
            Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_MESSAGE, colorViewHolder.myPhoto)

            this.startActivity(intentFullScreenImage)


        } else {
            if (selectionMode) {
                colorViewHolder.reverseSelection()
            } else {
                Toast.makeText(
                    this,
                    "Clicked picture: ${colorViewHolder.myPhoto}",
                    Toast.LENGTH_LONG
                ).show()
                Log.i("Files", "Image to open: ${colorViewHolder.myPhoto}")

                val intentFullScreenImage = Intent(this, FullscreenImageActivity::class.java)
                Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_MESSAGE, colorViewHolder.myPhoto)

                this.startActivity(intentFullScreenImage)
            }
        }
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onLongItemClick(
        view: View,
        position: Int,
        colorViewHolder: ImageGridAdapter.ColorViewHolder
    ) {
//        Toast.makeText(this, "Image LONG clicked $position", Toast.LENGTH_SHORT).show()
//        colorViewHolder?.updatePictureBySelection()
        if (selectionMode) {
            colorViewHolder.reverseSelection()
        } else {
            holders.forEach {
                run {
                    it.enableSelectionMode()
                }
            }
            colorViewHolder.setAsSelected()
            selectionMode = true
        }
    }

}