package com.example.gallery_app.storageAccess.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity
class MyPhotoAlbum {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    var rowId: Long? = null

    var albumFullPath: String = ""

    var albumName: String = ""
        get() {
            if (field == "")
                field = albumFullPath.split('/').last()
            return field
        }
    var ignore: Boolean = false
    var size: Int = 0
    var position: Int = -1
    var uriLong: Long? = null

    @Ignore
    var mediaObjects = ArrayList<MyMediaObject>()

    @Ignore
    var selected: Boolean = false

    @Ignore
    var nrPhotos: Int = 0

    @Ignore
    var nrVideos: Int = 0


    constructor(albumFullPath: String, mediaObjects: ArrayList<MyMediaObject>) {
        this.albumFullPath = albumFullPath
        this.mediaObjects = mediaObjects
        this.size = mediaObjects.size
        this.uriLong = mediaObjects[0].uriId
    }

    constructor() {}
    constructor(
        albumFullPath: String,
        albumName: String,
        ignore: Boolean,
        size: Int,
        position: Int = -1
    ) {
        this.albumFullPath = albumFullPath
        this.albumName = albumName
        this.ignore = ignore
        this.size = size
        this.position = position
    }

    fun getNrSelected(): Int {
        var selectedNr = 0
        for (mediaObject in mediaObjects)
            if (mediaObject.selected)
                selectedNr++
        return selectedNr
    }

    /**
     * checks if the size and thumbnail uriLong are different. It does not check the name or the full path
     */
    fun isDifferent(other: MyPhotoAlbum): Boolean {
        return this.size != other.size || this.uriLong != other.uriLong
    }

    override fun toString(): String {
        return "MyPhotoAlbum(rowId=$rowId, albumFullPath='$albumFullPath', albumName='$albumName', ignore=$ignore,size=$size, position=$position, uriLong=$uriLong)"
    }

    /**
     * finds a album with the same full path, or returns null
     */
    fun findSimilar(list: List<MyPhotoAlbum>): MyPhotoAlbum? {
        for (album in list)
            if (this.albumFullPath.contentEquals(album.albumFullPath))
                return album
        return null
    }
}