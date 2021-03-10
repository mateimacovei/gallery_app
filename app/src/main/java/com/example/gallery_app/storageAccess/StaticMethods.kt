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
        val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.DATA
        )

        /**
         * return an List containing all the photo albums
         */
        fun getAllAlbums(activity: Activity): ArrayList<MyPhotoAlbum>{
            val imageCursor: Cursor? = getBasicImageCursor(activity)
            if(imageCursor==null)
            {
                Log.w("Files", "getBasicImageCursor returned null")
                return ArrayList()
            }
            val picturesMap: MutableMap<String, MyPhoto> = getPhotosFromCursor(imageCursor)

            val provAlbumMap: MutableMap<String, ArrayList<MyPhoto>> = convertFullPathPhotoMapToAlbumPathPhotoArrayMap(picturesMap)

            return buildAlbumListFromPathMap(provAlbumMap)

        }

        /**
         * param: map Key=path, without picture name | Value = List of Photos in the same folder
         * return an List containing the photo albums
         */
        private fun buildAlbumListFromPathMap(provAlbumMap: MutableMap<String,ArrayList<MyPhoto>>): ArrayList<MyPhotoAlbum>{
            val albums = ArrayList<MyPhotoAlbum>()

            for (key in provAlbumMap.keys) {
                Log.i("Files", "Key: $key | Nr of items in list:${provAlbumMap[key]?.size}")
                provAlbumMap[key]?.let { MyPhotoAlbum(key, it) }?.let { albums.add(it) }
            }
            return albums
        }

        /**
         * param: map: Key=picture name with full path | Value = MyPhoto object
         * returns: map Key=path, without picture name | Value = List of Photos in the same folder
         */
        private fun convertFullPathPhotoMapToAlbumPathPhotoArrayMap(photos: MutableMap<String, MyPhoto>): MutableMap<String,ArrayList<MyPhoto>> {
            val provAlbumMap: MutableMap<String, ArrayList<MyPhoto>> = mutableMapOf()
            for (key in photos.keys) {
                val photo: MyPhoto? = photos[key]

                if (photo != null) {
                    if (!provAlbumMap.containsKey(photo.albumFullPath))
                        provAlbumMap[photo.albumFullPath] = ArrayList()
                    provAlbumMap[photo.albumFullPath]?.add(photo)
                } else
                    Log.w("Files", "photo was null for path key key: $key")
            }
            return provAlbumMap
        }

        /**
         * return a image cursor using the static projection, with no other parameters other than sort order DATE_MODIFIED
         */
        private fun getBasicImageCursor(activity: Activity): Cursor? {
            return activity.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null, null,
                    MediaStore.Images.Media.DATE_MODIFIED)
        }

        /**
         * return a cursor over the images containing the given path
         * it will return pictures in a folder AND IN ITS SUB-FOLDERS
         */
        fun getPathFilterImageCursor(activity: Activity,path:String): Cursor?{
            return activity.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Files.FileColumns.DATA + " LIKE ?", arrayOf("$path%"),
                    MediaStore.Images.Media.DATE_MODIFIED)
        }

        /**
         * returns a map Key=picture name with full path | Value = MyPhoto object
         */
        private fun getPhotosFromCursor(cursor: Cursor): MutableMap<String, MyPhoto> {
            val pictures: MutableMap<String, MyPhoto> = mutableMapOf()


            val actualImageColumnIndex__ID: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val actualImageColumnIndexRELATIVE_PATH: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            val actualImageColumnIndexDATE_MODIFIED: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val actualImageColumnIndexSIZE: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val actualImageColumnIndexWIDTH: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val actualImageColumnIndexHEIGHT: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val actualImageColumnIndexDATA: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            Log.i("Files", "EXTERNAL_URI cursor size: ${cursor.count}")

            while (cursor.moveToNext()) {
//                    val uri = actualImageColumnIndex__ID?.let { cursor1.getString(it).toUri() }
                val id: Long = actualImageColumnIndex__ID.let { cursor.getLong(it) }
                val uri: Uri = id.let {
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
                }

                val data: String? =
                        actualImageColumnIndexDATA.let { cursor.getString(it) }
                val relativePath: String? =
                        actualImageColumnIndexRELATIVE_PATH.let { cursor.getString(it) }
                val dateModified: String? =
                        actualImageColumnIndexDATE_MODIFIED.let { cursor.getString(it) }
                val size: String? = actualImageColumnIndexSIZE.let { cursor.getString(it) }
                val width: String? = actualImageColumnIndexWIDTH.let { cursor.getString(it) }
                val height: String? = actualImageColumnIndexHEIGHT.let { cursor.getString(it) }


                if (data != null) {
                    pictures[data] = MyPhoto(
                            uri = uri,
                            DATA = data,
                            RELATIVE_PATH = relativePath,
                            DATE_MODIFIED = dateModified,
                            SIZE = size,
                            WIDTH = width,
                            HEIGHT = height)
                } else
                    Log.w("Files", "load failed : failed null-check in getAllPictures(): data: $data | uri:$uri")
            }
            cursor.close()
            return pictures
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