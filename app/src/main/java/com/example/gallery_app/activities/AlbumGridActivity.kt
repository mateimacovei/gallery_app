package com.example.gallery_app.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.example.gallery_app.IMAGE_GRID_MESSAGE
import com.example.gallery_app.R
import com.example.gallery_app.uiClasses.AbstractMediaObjectHolder
import com.example.gallery_app.uiClasses.AlbumGridAdapter
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.repo.AlbumListViewModel
import kotlinx.android.synthetic.main.activity_album_grid.*



class AlbumGridActivity : AbstractGridActivity() {
    val holders: ArrayList<AbstractMediaObjectHolder> = ArrayList()
    private val albumListModel: AlbumListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_grid)
        setSupportActionBar(album_grid_toolbar)

        this.onConfigurationChanged(this.resources.configuration)

        val adapter = AlbumGridAdapter(this, ArrayList())
        adapter.setClickListener(this)
        recycleViewerForAlbums.adapter = adapter

        albumListModel.albumList.observe(this, {newList->
            //i will check in the viewModel if changes need to be made
            Log.i("Activity","refreshing album grid")
            adapter.albums.clear()
            adapter.albums.addAll(newList)
            adapter.notifyDataSetChanged()
        })
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
            ImageGridActivity.receivedMyPhotoAlbum = colorViewHolder.album
            val intentImageGrid = Intent(this, ImageGridActivity::class.java)
            this.startActivity(intentImageGrid)
            //TO DO: start activity for result. On finish, check if the album became empty, in which case, eliminate it

//            val album = colorViewHolder.album
//            val intentImageGrid = Intent(this, ImageGridActivity::class.java)
//            Box.Add(intentImageGrid, IMAGE_GRID_MESSAGE, album)
//            this.startActivity(intentImageGrid)
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

    override fun onResume() {
        super.onResume()
        albumListModel.refreshAlbums()
    }
}