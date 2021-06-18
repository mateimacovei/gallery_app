package com.example.gallery_app.storageAccess.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.ByteArrayOutputStream


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

    @Ignore
    var selected: Boolean = false
    var ignore: Boolean = false
    @Ignore
    var nrPhotos: Int = 0
    @Ignore
    var nrVideos: Int = 0

    var thumbnailPath: String = ""

    constructor(albumFullPath: String, mediaObjects: ArrayList<MyMediaObject>){
        this.albumFullPath = albumFullPath
        this.mediaObjects = mediaObjects
    }

    constructor(){}

    constructor(albumFullPath: String, albumName: String, ignore: Boolean, thumbnailPath: String) {
        this.albumFullPath = albumFullPath
        this.albumName = albumName
        this.ignore = ignore
        this.thumbnailPath = thumbnailPath
    }

    fun getNrSelected(): Int{
        var selectedNr = 0
        for (mediaObject in mediaObjects)
            if (mediaObject.selected)
                selectedNr++
        return selectedNr
    }

    override fun toString(): String {
        return "MyPhotoAlbum(albumFullPath='$albumFullPath', albumName='$albumName', ignore=$ignore, thumbnailPath='$thumbnailPath')"
    }

    fun encodeTobase64(image: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 90, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    fun decodeBase64(input: String?): Bitmap? {
        val decodedByte: ByteArray = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }
}