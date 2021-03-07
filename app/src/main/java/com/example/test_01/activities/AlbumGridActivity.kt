package com.example.test_01.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.test_01.IMAGE_GRID_MESSAGE
import com.example.test_01.R
import com.example.test_01.adapter.AlbumGridAdapter
import com.example.test_01.adapter.clickListenerInterfaces.AlbumItemClickListener
import com.example.test_01.storageAccess.Box
import com.example.test_01.storageAccess.MyPhotoAlbum
import com.example.test_01.storageAccess.StaticMethods
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

        this.onConfigurationChanged(this.resources.configuration)
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
            val pictures = colorViewHolder.album.photos
            val intentImageGrid = Intent(this, ImageGridActivity::class.java)
            Box.Add(intentImageGrid, IMAGE_GRID_MESSAGE,pictures)
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