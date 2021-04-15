package com.example.gallery_app.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.gallery_app.IMAGE_GRID_MESSAGE
import com.example.gallery_app.R
import com.example.gallery_app.adapter.AbstractMediaObjectHolder
import com.example.gallery_app.adapter.AlbumGridAdapter
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.GridSize
import com.example.gallery_app.storageAccess.MyPhotoAlbum
import com.example.gallery_app.storageAccess.StaticMethods
import kotlinx.android.synthetic.main.activity_album_grid.*

class AlbumGridActivity : AbstractGridActivity() {
    val holders: ArrayList<AbstractMediaObjectHolder> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_grid)

        loadPreferences()
        this.gridSize= GridSize.S1

        this.onConfigurationChanged(this.resources.configuration)

//        loadContent()

        this.title = "Albums"
    }

    override fun enableSelectionMode() {
        holders.forEach {
            run {
                it.enableSelectionMode()
            }
        }
        selectionMode = true
    }

    override fun disableSelectionMode(){
        for (holder in holders)
            holder.disableSelectionMode()
        this.selectionMode = false
    }



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recycleViewerForAlbums.layoutManager = getNewLayoutManager(newConfig)
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onItemClick(
            view: View?,
            position: Int,
            colorViewHolder: AbstractMediaObjectHolder,
    ) {
        if (selectionMode) {
            colorViewHolder.reverseSelection()
        } else {
            colorViewHolder as AlbumGridAdapter.ColorViewHolder
            val album = colorViewHolder.album
            val intentImageGrid = Intent(this, ImageGridActivity::class.java)
            Box.Add(intentImageGrid, IMAGE_GRID_MESSAGE, album)
            this.startActivity(intentImageGrid)
        }
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onLongItemClick(
            view: View?,
            position: Int,
            colorViewHolder: AbstractMediaObjectHolder,
    ) {
        if (selectionMode) {
            colorViewHolder.reverseSelection()
        } else {
            enableSelectionMode()
            colorViewHolder.setAsSelected()
        }
    }

    fun loadContent(){
        val albums: ArrayList<MyPhotoAlbum> = StaticMethods.getAllAlbums(this)
        if(albums.size!=0) {
            val aga = AlbumGridAdapter(this, albums)
            aga.setClickListener(this)
            recycleViewerForAlbums.adapter = aga
        }
        else{
            Log.w("Files", "NO ALBUMS RECEIVED")
        }
    }

    override fun onResume() {
        super.onResume()
        loadContent()
    }

}