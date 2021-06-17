package com.example.gallery_app.storageAccess.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class MyPhotoAlbum{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="rowid")
    var rowId: Long? = null

    var albumFullPath: String = ""

    @Ignore
    lateinit var mediaObjects: ArrayList<MyMediaObject>

    var albumName: String = ""
        get() {
            if(field=="")
                field = albumFullPath.split('/').last()
            return field
        }
    var selected: Boolean = false
    var ignore: Boolean = false
    var nrPhotos: Int = 0
    var nrVideos: Int = 0


    constructor(albumFullPath: String, mediaObjects: ArrayList<MyMediaObject>){
        this.albumFullPath = albumFullPath
        this.mediaObjects = mediaObjects
    }
    constructor(){

    }

    fun getNrSelected(): Int{
        var selectedNr = 0
        for (mediaObject in mediaObjects)
            if (mediaObject.selected)
                selectedNr++
        return selectedNr
    }
}