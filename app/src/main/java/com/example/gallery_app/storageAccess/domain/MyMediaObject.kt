package com.example.gallery_app.storageAccess.domain

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class MyMediaObject {
    // I will store this one in the database, in order to get the uri from it
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "rowid")
    var uriId: Long? = null
    var fullPath: String? = null
    var dateModified: String? = null

    var size: Double? = null
    var width: String? = null
    var height: String? = null

    @Ignore
    var selected: Boolean = false
    var name: String = ""
    var albumFullPath: String = ""
    var isVideo: Boolean = false

    @Ignore
    var uri: Uri? = null

    constructor(
        uriId: Long? = null,
        uri: Uri? = null,
        fullPath: String?,
        dateModified: String?,
        size: String?,
        width: String?,
        height: String?,
        albumFullPath: String,
        name: String,
        isVideo: Boolean = false
    ) : this(uriId, uri, fullPath, dateModified, size, width, height, isVideo) {
        this.albumFullPath = albumFullPath
        this.name = name
    }

    constructor(
        uriId: Long? = null,
        uri: Uri? = null,
        fullPath: String?,
        dateModified: String?,
        size: String?,
        width: String?,
        height: String?,
        isVideo: Boolean = false
    ) {
        this.uriId = uriId
        this.uri = uri
        this.fullPath = fullPath
        this.dateModified = dateModified
        this.size = size?.toDouble()
        this.width = width
        this.height = height
        this.isVideo = isVideo

        var splitPath = this.fullPath?.split('/')

        this.name = splitPath?.last() ?: ""
        splitPath = splitPath?.dropLast(1)
        this.albumFullPath = splitPath?.joinToString(separator = "/") ?: ""
    }

    constructor() {}

    override fun toString(): String {
        return "uriId: $uriId, isVideo: $isVideo, uri: $uri |  name:$name | path/data: ${this.fullPath} | date modified:$dateModified | size:$size | width:$width | height:$height"
    }

    fun getExtension(): String = name.substringAfterLast('.')

    //    private fun reloadDimensionsFromExif(activity: Activity) {
//        try {
//            activity.contentResolver.openInputStream(uri).use { inputStream ->
//                val exif: ExifInterface? = inputStream?.let { ExifInterface(it) }
//                val width: Int? = exif?.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1)
//                val height: Int? = exif?.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1)
//
//                Log.i("Data", "reloadDimensionsFromExif: width: ${{ width }}, height: $height")
//                if (width != -1 && height != -1 && width != 0 && height != 0 && width != null) {
//                    this.HEIGHT = height.toString()
//                    this.WIDTH = width.toString()
//                }
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun reloadDimensionsFromBitmap() {
//        val options = BitmapFactory.Options()
//        options.inJustDecodeBounds = true
//        try {
//            BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
//            val height = options.outHeight
//            val width = options.outWidth
//
//            Log.i("Data", "reloadDimensionsFromBitmap: width: ${{ width }}, height: $height")
//
//            if (width != -1 && height != -1 && width != 0 && height != 0) {
//                this.HEIGHT = height.toString()
//                this.WIDTH = width.toString()
//            }
//        } catch (e: NullPointerException) {
//            Log.w("Data", "(probably) uri.path was null")
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//    }

}