package com.example.gallery_app.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gallery_app.IMAGE_GRID_MESSAGE
import com.example.gallery_app.R
import com.example.gallery_app.adapter.AlbumGridAdapter
import com.example.gallery_app.adapter.clickListenerInterfaces.AlbumItemClickListener
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhotoAlbum
import com.example.gallery_app.storageAccess.StaticMethods
import kotlinx.android.synthetic.main.activity_album_grid.*

class AlbumGridActivity : AppCompatActivity(),
    AlbumItemClickListener {
    var selectionMode: Boolean = false
    val holders: ArrayList<AlbumGridAdapter.ColorViewHolder> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_grid)

        val sglm = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recycleViewerForAlbums.layoutManager = sglm

        val albums: ArrayList<MyPhotoAlbum> = StaticMethods.getAllAlbums(this)

//        var i = 0
//        while (i < albums.size && !selectionMode) {
//            selectionMode = albums[i].selected
//            i++
//        }
        if(albums.size!=0) {
            val aga = AlbumGridAdapter(this, albums)
            aga.setClickListener(this)
            recycleViewerForAlbums.adapter = aga
        }
        else{
            Log.w("Files","NO ALBUMS RECEIVED")
        }

        this.onConfigurationChanged(this.resources.configuration)
        this.title = "Albums"
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
            Configuration.ORIENTATION_PORTRAIT -> recycleViewerForAlbums.layoutManager =
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            Configuration.ORIENTATION_LANDSCAPE -> recycleViewerForAlbums.layoutManager =
                StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
            else -> { // Note the block
                Log.w(
                    "Orientation",
                    "Orientation in AlbumGridActivity was undefined at configuration change"
                )
            }
        }
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onItemClick(
        view: View?,
        position: Int,
        colorViewHolder: AlbumGridAdapter.ColorViewHolder
    ) {
        if (selectionMode) {
            colorViewHolder.reverseSelection()
        } else {
            val album = colorViewHolder.album
            val intentImageGrid = Intent(this, ImageGridActivity::class.java)
            Box.Add(intentImageGrid, IMAGE_GRID_MESSAGE,album)
            this.startActivity(intentImageGrid)
        }
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onLongItemClick(
        view: View?,
        position: Int,
        colorViewHolder: AlbumGridAdapter.ColorViewHolder
    ) {
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