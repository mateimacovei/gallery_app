package com.example.gallery_app.storageAccess

class MyPhotoAlbum(var albumFullPath: String, var photos: ArrayList<MyPhoto>) {
    val albumName: String
    val albumCount: Int = photos.size
    var selected: Boolean = false

    init {
        val splitPath = albumFullPath.split('/')
        albumName = splitPath.last()
    }
}