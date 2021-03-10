package com.example.gallery_app.storageAccess

class MyPhotoAlbum(val albumFullPath: String, var photos: ArrayList<MyPhoto>) {
    val albumName: String
    var selected: Boolean = false

    init {
        val splitPath = albumFullPath.split('/')
        albumName = splitPath.last()
    }
}