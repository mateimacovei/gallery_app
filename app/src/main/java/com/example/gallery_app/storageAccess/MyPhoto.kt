package com.example.gallery_app.storageAccess

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException


class MyPhoto(
        uri: Uri,
        DATA: String,
        RELATIVE_PATH: String?,
        DATE_MODIFIED: String?,
        SIZE: String?,
        WIDTH: String?,
        HEIGHT: String?
) : MyMediaObject(uri, DATA, RELATIVE_PATH, DATE_MODIFIED, SIZE, WIDTH, HEIGHT) {

    override fun toString(): String {
        return "IMAGE: ${super.toString()}"
    }

    override fun reloadDimensions(activity: Activity) {
        reloadDimensionsFromExif(activity)
        if (this.HEIGHT == null)
            reloadDimensionsFromBitmap()
    }

    private fun reloadDimensionsFromExif(activity: Activity) {
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



