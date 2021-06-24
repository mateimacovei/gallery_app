package com.example.gallery_app.storageAccess.repo

import android.app.Application
import android.media.MediaScannerConnection
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum
import kotlin.concurrent.thread

class AlbumListViewModel(application: Application) : AndroidViewModel(application) {
    val albumList: MutableLiveData<List<MyPhotoAlbum>> = MutableLiveData<List<MyPhotoAlbum>>()
    private val repo: Repo = Repo(application)

    fun refreshAlbums() {
        thread {
            var start = System.currentTimeMillis()
            val roomAlbums: MutableList<MyPhotoAlbum> = repo.getRoomAlbums().toMutableList()
            albumList.postValue(roomAlbums)
            Log.i(
                "AlbumListViewModel",
                "room albums posted. time: ${System.currentTimeMillis() - start}"
            )

            start = System.currentTimeMillis()
            val shouldUpdate = repo.updateAlbums(roomAlbums)
            if (shouldUpdate)
                albumList.postValue(roomAlbums)
            Log.i(
                "AlbumListViewModel",
                "MediaStore albums posted. time: ${System.currentTimeMillis() - start}"
            )

            start = System.currentTimeMillis()
            val paths = arrayOf("/storage/emulated/0/")
            val mimeTypes = arrayOf("video/*", "image/*")
            MediaScannerConnection.scanFile(
                getApplication(),
                paths,
                mimeTypes
            ) { path, uri ->
                Log.i(
                    "AlbumListViewModel",
                    "MediaScanner scan completed. time: ${System.currentTimeMillis() - start}"
                )
                val shouldUpdate2 = repo.updateAlbums(roomAlbums)
                if (shouldUpdate2)
                    albumList.postValue(roomAlbums)
            }

            /*
            I/AlbumListViewModel: room albums posted. time: 26
            I/AlbumListViewModel: MediaStore albums posted. time: 112
            I/AlbumListViewModel: MediaScanner scan completed. time: 384 //this is without the actual update
             */
        }
    }
}