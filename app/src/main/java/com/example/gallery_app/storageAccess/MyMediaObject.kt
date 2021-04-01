package com.example.gallery_app.storageAccess

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.example.gallery_app.activities.ImageDetailActivity
import java.io.File
import java.io.IOException

abstract class MyMediaObject (
        open var uri: Uri,
        open var DATA: String,
        open var RELATIVE_PATH: String?,
        open var DATE_MODIFIED: String?,
        SIZE: String?,
        open var WIDTH: String?,
        open var HEIGHT: String?
){

    abstract fun reloadDimensions(activity: Activity)

    var SIZE: Double? = SIZE?.toDouble()
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

}
