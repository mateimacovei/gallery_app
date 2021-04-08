package com.example.gallery_app.storageAccess


class MyPhotoAlbum(val albumFullPath: String, var mediaObjects: ArrayList<MyMediaObject>) {
    val albumName: String
    var selected: Boolean = false
    var nrPhotos: Int = 0
    var nrVideos: Int = 0

    init {
        val splitPath = albumFullPath.split('/')
        albumName = splitPath.last()
    }

    fun getNrSelected(): Int{
        var selectedNr = 0
        for (mediaObject in mediaObjects)
            if (mediaObject.selected)
                selectedNr++
        return selectedNr
    }
}