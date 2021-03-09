package com.example.gallery_app.storageAccess

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException


class MyPhoto(
        var uri: Uri,
        var DATA: String,
        var RELATIVE_PATH: String?,
        var DATE_MODIFIED: String?,
        SIZE: String?,
        var WIDTH: String?,
        var HEIGHT: String?
) {
    var SIZE: String? = "%.2f".format(SIZE?.toDouble()?.div(1024))
    var selected: Boolean = false
    var name: String
    var albumFullPath: String

    init {
        var splitPath = this.DATA.split('/')
        this.name = splitPath.last()
        splitPath = splitPath.dropLast(1)
        this.albumFullPath = splitPath.joinToString(separator = "/")
    }

    override fun toString(): String {
        return "uri:$uri |  name:$name | path(data):${this.DATA} | realtive path:$RELATIVE_PATH | date modified:$DATE_MODIFIED | size:$SIZE | width:$WIDTH | height:$HEIGHT"
    }

    fun reloadDimensions(activity: Activity) {
        reloadDimensionsFromExif(activity)
        if (this.HEIGHT == null)
            reloadDimensionsFromBitmap()
    }

    fun reloadDimensionsFromExif(activity: Activity) {
        try {
            activity.contentResolver.openInputStream(uri).use { inputStream ->
                val exif: ExifInterface? = inputStream?.let { ExifInterface(it) }
                val width: Int? = exif?.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1)
                val height: Int? = exif?.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1)

                Log.i("Data", "reloadDimensionsFromExif: width: ${{ width }}, height: $height")
                if (width != -1 && height != -1 && width != 0 && height != 0 && width != null) {
                    this.HEIGHT = height.toString()
                    this.WIDTH = width.toString()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun reloadDimensionsFromBitmap() {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
            val height = options.outHeight
            val width = options.outWidth

            Log.i("Data", "reloadDimensionsFromBitmap: width: ${{ width }}, height: $height")

            if (width != -1 && height != -1 && width != 0 && height != 0) {
                this.HEIGHT = height.toString()
                this.WIDTH = width.toString()
            }
        } catch (e: NullPointerException) {
            Log.w("Data", "(probably) uri.path was null")
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}



