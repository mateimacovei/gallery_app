package com.example.gallery_app.storageAccess

import android.app.Activity
import android.content.ContentUris
import android.content.IntentSender.SendIntentException
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat.startIntentSenderForResult


class StaticMethods {
    companion object {

        fun getAllPictures(activity: Activity, albumPath: String? = null): ArrayList<MyPhoto> {
            val pictures = ArrayList<MyPhoto>()
            val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.RELATIVE_PATH,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.DATA
            )


            var cursor1: Cursor? = null
            if (albumPath == null)
                cursor1 = activity.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null, null,
                        MediaStore.Images.Media.DATE_MODIFIED)
            else cursor1 = activity.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Files.FileColumns.DATA + " LIKE ?", arrayOf("$albumPath%"),
                    MediaStore.Images.Media.DATE_MODIFIED)
            //the selection is not exclusive, because it also returns pictures in the sub-albums of the target

            val actualImageColumnIndex__ID: Int? =
                    cursor1?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val actualImageColumnIndexRELATIVE_PATH: Int? =
                    cursor1?.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            val actualImageColumnIndexDATE_MODIFIED: Int? =
                    cursor1?.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val actualImageColumnIndexSIZE: Int? =
                    cursor1?.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val actualImageColumnIndexWIDTH: Int? =
                    cursor1?.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val actualImageColumnIndexHEIGHT: Int? =
                    cursor1?.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val actualImageColumnIndexDATA: Int? =
                    cursor1?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor1 != null) {
                Log.i("Files", "EXTERNAL_URI cursor size: ${cursor1.count}")

                while (cursor1.moveToNext()) {
//                    val uri = actualImageColumnIndex__ID?.let { cursor1.getString(it).toUri() }
                    val id: Long? = actualImageColumnIndex__ID?.let { cursor1.getLong(it) }
                    val uri: Uri? = id?.let {
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
                    }

                    val data: String? =
                            actualImageColumnIndexDATA?.let { cursor1.getString(it) }
                    val relativePath =
                            actualImageColumnIndexRELATIVE_PATH?.let { cursor1.getString(it) }
                    val dateModified =
                            actualImageColumnIndexDATE_MODIFIED?.let { cursor1.getString(it) }
                    val size = actualImageColumnIndexSIZE?.let { cursor1.getString(it) }
                    val width = actualImageColumnIndexWIDTH?.let { cursor1.getString(it) }
                    val height = actualImageColumnIndexHEIGHT?.let { cursor1.getString(it) }


                    if (uri != null && data != null)
                        pictures.add(MyPhoto(
                                uri = uri,
                                DATA = data,
                                RELATIVE_PATH = relativePath,
                                DATE_MODIFIED = dateModified,
                                SIZE = size,
                                WIDTH = width,
                                HEIGHT = height
                        ))
                    else
                        Log.w("Files", "load failed : failed null-check in getAllPictures(): data: $data | uri:$uri")
                }
            } else
                Log.w("Files", "cursor is null")
            cursor1?.close()
            return pictures
        }

        fun getAllAlbums(activity: Activity): ArrayList<MyPhotoAlbum> {
            val pictures: ArrayList<MyPhoto> = getAllPictures(activity)
//        for (picture in pictures){
//            Log.i("Files", "Found picture: $picture")
//        }

            val albumMap: MutableMap<String, ArrayList<MyPhoto>> = mutableMapOf()
            for (picture in pictures) {
                if (!albumMap.containsKey(picture.albumFullPath))
                    albumMap[picture.albumFullPath] = ArrayList()
                albumMap[picture.albumFullPath]?.add(picture)
            }

            val albums = ArrayList<MyPhotoAlbum>()

            for (key in albumMap.keys) {
                Log.i("Files", "Key: $key | Nr of items in list:${albumMap[key]?.size}")
                albumMap[key]?.let { MyPhotoAlbum(key, it) }?.let { albums.add(it) }
            }
            return albums
        }

//        fun getOneAlbum(activity: Activity, fullPath: String): MyPhotoAlbum {
//            val pictures: ArrayList<MyPhoto> = getAllPictures(activity, fullPath)
//            val result = ArrayList<MyPhoto>()
//            for (picture in pictures) {
//                if (picture.albumFullPath == fullPath)
//                    result.add(picture)
//            }
//
//            return MyPhotoAlbum(fullPath, result)
//        }
    }
}