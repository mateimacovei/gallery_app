package com.example.gallery_app.storageAccess

import android.app.Activity
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.gallery_app.activities.ImageGridActivity


class StaticMethods {
    companion object {
        private val projectionImages = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.DATA
        )

        private val projectionVideos = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DATA
        )

        /**
         * returns a pair
         * First: Boolean, true if there were changes to the album
         * Second: list of pictures from the album, taking into account the sorting parameters
         * KEEPS the selection model
         */
        fun getNewPhotoArrayForAlbum(imageGridActivity: ImageGridActivity): Pair<Boolean, ArrayList<MyMediaObject>> {
            val oldMediaObjects = imageGridActivity.album.mediaObjects
            var changed = false


//            val imageCursor: Cursor? = getImageCursor(imageGridActivity, path = imageGridActivity.album.albumFullPath, sortOrder = getSortOrderString(imageGridActivity.sortBy, imageGridActivity.sortOrder))
//            if (imageCursor == null) {
//                Log.w("Files", "failed to reload album: cursor was null")
//                return Pair(true, ArrayList())
//            }
//            val picturesMap: MutableMap<String, MyMediaObject> = getPhotosFromCursor(imageCursor)

            val picturesMap= getPictures(imageGridActivity, path = imageGridActivity.album.albumFullPath, sortOrder = getSortOrderString(imageGridActivity.sortBy, imageGridActivity.sortOrder))
            picturesMap.putAll(getVideos(imageGridActivity, path = imageGridActivity.album.albumFullPath, sortOrder = getSortOrderString(imageGridActivity.sortBy, imageGridActivity.sortOrder)))


            for (mediaObject in oldMediaObjects)
                if (picturesMap.containsKey(mediaObject.DATA)) {
                    if (mediaObject.selected)
                        picturesMap[mediaObject.DATA]?.selected = true
                } else
                    changed = true

            val newPhotos = fitAlbumPath(picturesMap,imageGridActivity.album.albumFullPath)
            //TO DO now videos are always at the end. SORT IT

            if (oldMediaObjects.size != newPhotos.size)
                changed = true

            return Pair(changed, newPhotos)
        }

        fun fitAlbumPath(mediaObjectsMap: MutableMap<String, MyMediaObject>, albumFullPath: String): ArrayList<MyMediaObject> {
            val result = ArrayList<MyMediaObject>()
            for (photoFullPath in mediaObjectsMap.keys) {
                val candidate = mediaObjectsMap[photoFullPath]
                if (candidate?.albumFullPath == albumFullPath)
                    result.add(candidate)
            }
            return result
        }

        /**
         * returns a map Key=picture name with full path | Value = MyPhoto object
         * WARNING: filtering by path will also return sub-folders content
         */
        private fun getPictures(activity: Activity,sortOrder: String, path: String?=null): MutableMap<String, MyMediaObject> {
            val imageCursor: Cursor? = getImageCursor(activity, sortOrder = sortOrder, path = path)
            if (imageCursor == null) {
                Log.w("Files", "getImageCursor returned null")
                return mutableMapOf()
            }
            return getPhotosFromCursor(imageCursor)
        }

        /**
         * returns a map Key=video name with full path | Value = MyVideo object
         * WARNING: filtering by path will also return sub-folders content
         */
        private fun getVideos(activity: Activity,sortOrder: String, path: String?=null): MutableMap<String, MyMediaObject> {
            val videoCursor: Cursor? = getVideoCursor(activity, sortOrder = sortOrder, path = path)
            if (videoCursor == null) {
                Log.w("Files", "getVideoCursor returned null")
                return mutableMapOf()
            }
            return getVideosFromCursor(videoCursor)
        }

        /**
         * return an List containing all the photo albums
         */
        fun getAllAlbums(activity: Activity): ArrayList<MyPhotoAlbum> {
            val picturesMap: MutableMap<String, MyMediaObject> = getPictures(activity,sortOrder = getSortOrderString(SortBy.DATE_MODIFIED, SortOrder.DESC))
            val videosMap: MutableMap<String, MyMediaObject> = getVideos(activity,sortOrder = getSortOrderString(SortBy.DATE_MODIFIED, SortOrder.DESC))

            val provAlbumMap: MutableMap<String, ArrayList<MyMediaObject>> = createAlbums(picturesMap,videosMap)

            return buildAlbumListFromPathMap(provAlbumMap)
        }

        /**
         * param: map Key=path, without picture name | Value = List of Photos in the same folder
         * return an List containing the photo albums
         */
        private fun buildAlbumListFromPathMap(provAlbumMap: MutableMap<String, ArrayList<MyMediaObject>>): ArrayList<MyPhotoAlbum> {
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
        private fun createAlbums(photos: MutableMap<String, MyMediaObject>,
                                 videos: MutableMap<String,MyMediaObject>? = null
                                ): MutableMap<String, ArrayList<MyMediaObject>> {
            val albumMap: MutableMap<String, ArrayList<MyMediaObject>> = mutableMapOf()

            for (key in photos.keys) {
                val photo: MyMediaObject? = photos[key]

                if (photo != null) {
                    if (!albumMap.containsKey(photo.albumFullPath))
                        albumMap[photo.albumFullPath] = ArrayList()
                    albumMap[photo.albumFullPath]?.add(photo)
                } else
                    Log.w("Files", "photo was null for path key key: $key")
            }
            if (videos != null)
                for (key in videos.keys) {
                    val video: MyMediaObject? = videos[key]

                    if (video != null) {
                        if (!albumMap.containsKey(video.albumFullPath))
                            albumMap[video.albumFullPath] = ArrayList()
                        albumMap[video.albumFullPath]?.add(video)
                    } else
                        Log.w("Files", "photo was null for path key key: $key")
                }
            return albumMap
        }

        private fun getSortOrderString(sortBy: SortBy, sortOrder: SortOrder): String {
            var res: String = when (sortBy) {
                SortBy.NAME -> MediaStore.Images.Media.DISPLAY_NAME
                SortBy.DATE_MODIFIED -> MediaStore.Images.Media.DATE_MODIFIED
            }

            when (sortOrder) {
                SortOrder.DESC -> res += " DESC"
                SortOrder.ASC -> {
                }
            }
            return res
        }

        /**
         * return a image cursor using the static projection,
         * WARNING: filtering by path will also return sub-folders content
         */
        private fun getImageCursor(activity: Activity, sortOrder: String?, path: String?=null): Cursor? {
            return if (path == null)
                activity.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionImages,
                        null, null,
                        sortOrder)
            else
                activity.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projectionImages,
                        MediaStore.Files.FileColumns.DATA + " LIKE ?", arrayOf("$path%"),
                        sortOrder)
        }

        /**
         * return a video cursor using the static projection
         * WARNING: filtering by path will also return sub-folders content
         */
        private fun getVideoCursor(activity: Activity, sortOrder: String?, path: String?=null): Cursor? {
            return if (path == null)
                activity.contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projectionImages,
                        null, null,
                        sortOrder)
            else
                activity.contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projectionImages,
                        MediaStore.Files.FileColumns.DATA + " LIKE ?", arrayOf("$path%"),
                        sortOrder)
        }

        /**
         * returns a map Key=picture name with full path | Value = MyPhoto object
         */
        private fun getPhotosFromCursor(cursor: Cursor): MutableMap<String, MyMediaObject> {
            val pictures: MutableMap<String, MyMediaObject> = mutableMapOf()


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

            Log.i("Files", "IMAGE EXTERNAL_URI cursor size: ${cursor.count}")

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

        /**
         * returns a map Key=Video name with full path | Value = MyVideo object
         */
        private fun getVideosFromCursor(cursor: Cursor): MutableMap<String, MyMediaObject> {
            val videos: MutableMap<String, MyMediaObject> = mutableMapOf()


            val actualVideoColumnIndex__ID: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val actualVideoColumnIndexRELATIVE_PATH: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)
            val actualVideoColumnIndexDATE_MODIFIED: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val actualVideoColumnIndexSIZE: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val actualVideoColumnIndexWIDTH: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val actualVideoColumnIndexHEIGHT: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val actualVideoColumnIndexDATA: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            Log.i("Files", "VIDEO EXTERNAL_URI cursor size: ${cursor.count}")

            while (cursor.moveToNext()) {
//                    val uri = actualVideoColumnIndex__ID?.let { cursor1.getString(it).toUri() }
                val id: Long = actualVideoColumnIndex__ID.let { cursor.getLong(it) }
                val uri: Uri = id.let {
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, it)
                }

                val data: String? =
                        actualVideoColumnIndexDATA.let { cursor.getString(it) }
                val relativePath: String? =
                        actualVideoColumnIndexRELATIVE_PATH.let { cursor.getString(it) }
                val dateModified: String? =
                        actualVideoColumnIndexDATE_MODIFIED.let { cursor.getString(it) }
                val size: String? = actualVideoColumnIndexSIZE.let { cursor.getString(it) }
                val width: String? = actualVideoColumnIndexWIDTH.let { cursor.getString(it) }
                val height: String? = actualVideoColumnIndexHEIGHT.let { cursor.getString(it) }


                if (data != null) {
                    videos[data] = MyVideo(
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
            return videos
        }



    }
}