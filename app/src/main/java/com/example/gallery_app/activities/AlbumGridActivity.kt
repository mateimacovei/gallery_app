package com.example.gallery_app.activities

import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gallery_app.IMAGE_GRID_MESSAGE
import com.example.gallery_app.R
import com.example.gallery_app.uiClasses.AbstractMediaObjectHolder
import com.example.gallery_app.uiClasses.AlbumGridAdapter
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum
import com.example.gallery_app.storageAccess.StaticMethods
import com.example.gallery_app.storageAccess.repo.Repo
import kotlinx.android.synthetic.main.activity_album_grid.*

class AlbumListViewModel(application: Application) : AndroidViewModel (application) {
    val albumList: MutableLiveData<ArrayList<MyPhotoAlbum>>
    val repo: Repo

    init {
        Log.i("AlbumListViewModel","init entered")
        albumList = MutableLiveData<ArrayList<MyPhotoAlbum>>()
        albumList.value = ArrayList()

        repo = Repo(application)
    }
}

class AlbumGridActivity : AbstractGridActivity() {
    val holders: ArrayList<AbstractMediaObjectHolder> = ArrayList()
    private val albumListModel: AlbumListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_grid)

        this.onConfigurationChanged(this.resources.configuration)

        loadContentInAlbumList(onCreate = true)

        this.title = "Albums"
        Log.i("AlbumListViewModel","will call size")
        Log.i("AlbumListViewModel","size: ${albumListModel.albumList.value?.size}")

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

    private fun loadContentInAlbumList(onCreate: Boolean = false){
        Log.i("Activity", "loadContentInAlbumList entered: nrLoaded: $nrLoaded, onCreate = $onCreate")
        if (nrLoaded < 3) {
            nrLoaded++
            if (nrLoaded == 2)
                return
        }
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
//        loadContentInAlbumList()
    }

}