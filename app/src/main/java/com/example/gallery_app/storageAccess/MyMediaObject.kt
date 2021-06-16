package com.example.gallery_app.storageAccess

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

class MyMediaObject (
        var uriId: Long? = null,
        // I will store this one in the database, in order to get the uri from it

        var uri: Uri? = null,

        var DATA: String?,

        var DATE_MODIFIED: String?,

        SIZE: String?,

        var WIDTH: String?,

        var HEIGHT: String?,

        var isVideo: Boolean = false
){
    var SIZE: Double? = SIZE?.toDouble()
    var selected: Boolean = false
    var name: String = ""
    var albumFullPath: String = ""


    init {
        var splitPath = this.DATA?.split('/')

        this.name = splitPath?.last() ?: ""
        splitPath = splitPath?.dropLast(1)
        this.albumFullPath = splitPath?.joinToString(separator = "/") ?: ""
    }

    override fun toString(): String {
        return "uriId: $uriId, isVideo: $isVideo, uri: $uri |  name:$name | path/data: ${this.DATA} | date modified:$DATE_MODIFIED | size:$SIZE | width:$WIDTH | height:$HEIGHT"
    }

    fun toStringArrayListForSmallMyMediaObj(): ArrayList<String>{
        val list = ArrayList<String>()
        list.add(uri.toString())
        list.add(name)
        list.add(isVideo.toString())
        return list
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


class SmallMyMediaObject (stringArrayList: ArrayList<String>){
    init {
        val uri : Uri = stringArrayList[0].toLong().let {
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
        }
        val name = stringArrayList[1]
        val extension = name.substringAfterLast('.')
        val isVideo : Boolean = stringArrayList[2].toBoolean()
    }
}
