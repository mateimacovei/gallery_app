package com.example.gallery_app.storageAccess

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException


class MyVideo(
        uri: Uri,
        DATA: String,
        RELATIVE_PATH: String?,
        DATE_MODIFIED: String?,
        SIZE: String?,
        WIDTH: String?,
        HEIGHT: String?
) : MyMediaObject(uri, DATA, RELATIVE_PATH, DATE_MODIFIED, SIZE, WIDTH, HEIGHT) {

    override fun reloadDimensions(activity: Activity) {

    }

    override fun toString(): String {
        return "VIDEO: ${super.toString()}"
    }
}



