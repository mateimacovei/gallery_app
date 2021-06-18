package com.example.gallery_app.storageAccess

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.gallery_app.storageAccess.domain.MyMediaObject
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum

class StaticMethods {
    companion object {
        private val projectionImages = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATA
        )

        private val projectionVideos = arrayOf(
            MediaStore.Video.Media._ID,
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
        fun getNewPhotoArrayForAlbum(context: Context, album: MyPhotoAlbum): Pair<Boolean, List<MyMediaObject>> {
            val oldMediaObjects = album.mediaObjects
            var changed = false


//            val imageCursor: Cursor? = getImageCursor(imageGridActivity, path = imageGridActivity.album.albumFullPath, sortOrder = getSortOrderString(imageGridActivity.sortBy, imageGridActivity.sortOrder))
//            if (imageCursor == null) {
//                Log.w("Files", "failed to reload album: cursor was null")
//                return Pair(true, ArrayList())
//            }
//            val picturesMap: MutableMap<String, MyMediaObject> = getPhotosFromCursor(imageCursor)

            val sortOrderString = getSortOrderString(
                sortBy = PreferencesFileHandler.getSortBy(context),
                sortOrder = PreferencesFileHandler.getSortOrder(context)
            )
            val picturesMap = getPictures(
                context,
                albumPath = album.albumFullPath,
                sortOrder = sortOrderString
            )
            val videosMap = getVideos(
                context,
                albumPath = album.albumFullPath,
                sortOrder = sortOrderString
            )

            album.nrPhotos = picturesMap.size
            album.nrVideos = videosMap.size
            picturesMap.putAll(videosMap)

            for (mediaObject in oldMediaObjects)
                if (picturesMap.containsKey(mediaObject.fullPath)) {
                    if (mediaObject.selected)
                        picturesMap[mediaObject.fullPath]?.selected = true
                } else
                    changed = true

            val newPhotos: List<MyMediaObject> = picturesMap.map { x -> x.value }
            //TO DO now videos are always at the end. SORT IT

            if (oldMediaObjects.size != newPhotos.size)
                changed = true

            return Pair(changed, newPhotos)
        }

//        /**
//         * filter the map, emininating the mediaObjects in subfolders of the album
//         */
//        private fun fitAlbumPathMap(mediaObjectsMap: MutableMap<String, MyMediaObject>, albumFullPath: String): MutableMap<String, MyMediaObject> {
//            val result = mutableMapOf<String, MyMediaObject>()
//            for (photoFullPath in mediaObjectsMap.keys) {
//                val candidate = mediaObjectsMap[photoFullPath]
//                if (candidate?.albumFullPath == albumFullPath)
//                    result[photoFullPath] = candidate
//                else
//                    Log.i("Storage","eliminated ${candidate?.albumFullPath}, not fitted with $albumFullPath")
//            }
//            return result
//        }

        /**
         * returns a map Key=picture name with full path | Value = MyPhoto object
         */
        private fun getPictures(
            context: Context,
            sortOrder: String,
            albumPath: String? = null
        ): MutableMap<String, MyMediaObject> {
            val imageCursor: Cursor? =
                getImageCursor(context, sortOrder = sortOrder, path = albumPath)
            if (imageCursor == null) {
                Log.w("Files", "getImageCursor returned null")
                return mutableMapOf()
            }
            return getPhotosFromCursor(imageCursor, albumPath)
        }

        /**
         * returns a map Key=video name with full path | Value = MyVideo object
         */
        private fun getVideos(
            context: Context,
            sortOrder: String,
            albumPath: String? = null
        ): MutableMap<String, MyMediaObject> {
            val videoCursor: Cursor? =
                getVideoCursor(context, sortOrder = sortOrder, path = albumPath)
            if (videoCursor == null) {
                Log.w("Files", "getVideoCursor returned null")
                return mutableMapOf()
            }
            return getVideosFromCursor(videoCursor, albumPath)
        }

        /**
         * return an List containing all the photo albums
         */
        fun getAllAlbums(context: Context): ArrayList<MyPhotoAlbum> {
            val sortOrderString = getSortOrderString(
                sortBy = PreferencesFileHandler.getSortBy(context),
                sortOrder = PreferencesFileHandler.getSortOrder(context)
            )
            val picturesMap: MutableMap<String, MyMediaObject> =
                getPictures(context, sortOrder = sortOrderString)
            val videosMap: MutableMap<String, MyMediaObject> =
                getVideos(context, sortOrder = sortOrderString)

            val provAlbumMap: MutableMap<String, ArrayList<MyMediaObject>> =
                createAlbums(picturesMap, videosMap)

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
        private fun createAlbums(
            photos: MutableMap<String, MyMediaObject>,
            videos: MutableMap<String, MyMediaObject>
        ): MutableMap<String, ArrayList<MyMediaObject>> {
            val albumMap: MutableMap<String, ArrayList<MyMediaObject>> = mutableMapOf()

            for (key in photos.keys) {
                val photo: MyMediaObject? = photos[key]

                if (photo != null) {
                    if (!albumMap.containsKey(photo.albumFullPath))
                        albumMap[photo.albumFullPath] = ArrayList()
                    albumMap[photo.albumFullPath]?.add(photo)
                } else
                    Log.w("Files", "photo was null for path key: $key")
            }
            for (key in videos.keys) {
                val video: MyMediaObject? = videos[key]

                if (video != null) {
                    if (!albumMap.containsKey(video.albumFullPath))
                        albumMap[video.albumFullPath] = ArrayList()
                    albumMap[video.albumFullPath]?.add(video)
                } else
                    Log.w("Files", "video was null for path key: $key")
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
        private fun getImageCursor(
            context: Context,
            sortOrder: String?,
            path: String? = null
        ): Cursor? {
            return if (path == null)
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projectionImages,
                    null, null,
                    sortOrder
                )
            else
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projectionImages,
                    MediaStore.Files.FileColumns.DATA + " LIKE ?", arrayOf("$path/%"),
                    sortOrder
                )
        }

        /**
         * return a video cursor using the static projection
         * WARNING: filtering by path will also return sub-folders content
         */
        private fun getVideoCursor(
            context: Context,
            sortOrder: String?,
            path: String? = null
        ): Cursor? {
            return if (path == null)
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projectionVideos,
                    null, null,
                    sortOrder
                )
            else
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projectionVideos,
                    MediaStore.Files.FileColumns.DATA + " LIKE ?", arrayOf("$path%"),
                    sortOrder
                )
        }

        /**
         * returns a map Key=picture name with full path | Value = MyPhoto object
         */
        private fun getPhotosFromCursor(
            cursor: Cursor,
            albumPath: String?
        ): MutableMap<String, MyMediaObject> {
            val pictures: MutableMap<String, MyMediaObject> = mutableMapOf()


            val actualImageColumnIndex__ID: Int =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
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
                val dateModified: String? =
                    actualImageColumnIndexDATE_MODIFIED.let { cursor.getString(it) }
                val size: String? = actualImageColumnIndexSIZE.let { cursor.getString(it) }
                val width: String? = actualImageColumnIndexWIDTH.let { cursor.getString(it) }
                val height: String? = actualImageColumnIndexHEIGHT.let { cursor.getString(it) }


                if (data != null) {
                    if(albumPath == null)
                        pictures[data] = MyMediaObject(
                            uriId = id,
                            uri = uri,
                            fullPath = data,
                            dateModified = dateModified,
                            size = size,
                            width = width,
                            height = height
                        )
                    else {
                        var splitPath = data.split('/')
                        val name = splitPath.last() ?: ""
                        splitPath = splitPath.dropLast(1)
                        val albumFullPath = splitPath.joinToString(separator = "/") ?: ""

                        if (albumFullPath == albumPath)
                            pictures[data] = MyMediaObject(
                                uriId = id,
                                uri = uri,
                                fullPath = data,
                                dateModified = dateModified,
                                size = size,
                                width = width,
                                height = height,
                                name = name,
                                albumFullPath = albumFullPath
                            )
                        else Log.i("Files", "rejected data:$data, albumPath:$albumPath")
                    }
                } else
                    Log.w(
                        "Files",
                        "load failed : failed null-check in getPhotosFromCursor(): data: $data | uri:$uri"
                    )

            }
            cursor.close()
            return pictures
        }

        /**
         * returns a map Key=Video name with full path | Value = MyVideo object
         */
        private fun getVideosFromCursor(
            cursor: Cursor,
            albumPath: String?
        ): MutableMap<String, MyMediaObject> {
            val videos: MutableMap<String, MyMediaObject> = mutableMapOf()


            val actualVideoColumnIndex__ID: Int =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
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
                val dateModified: String? =
                    actualVideoColumnIndexDATE_MODIFIED.let { cursor.getString(it) }
                val size: String? = actualVideoColumnIndexSIZE.let { cursor.getString(it) }
                val width: String? = actualVideoColumnIndexWIDTH.let { cursor.getString(it) }
                val height: String? = actualVideoColumnIndexHEIGHT.let { cursor.getString(it) }


                if (data != null) {
                    if(albumPath == null)
                        videos[data] = MyMediaObject(
                            uriId = id,
                            uri = uri,
                            fullPath = data,
                            dateModified = dateModified,
                            size = size,
                            width = width,
                            height = height,
                            isVideo = true
                        )
                    else {
                        var splitPath = data.split('/')
                        val name = splitPath.last() ?: ""
                        splitPath = splitPath.dropLast(1)
                        val albumFullPath = splitPath.joinToString(separator = "/") ?: ""

                        if (albumFullPath == albumPath)
                            videos[data] = MyMediaObject(
                                uriId = id,
                                uri = uri,
                                fullPath = data,
                                dateModified = dateModified,
                                size = size,
                                width = width,
                                height = height,
                                name = name,
                                albumFullPath = albumFullPath,
                                isVideo = true
                            )
                        else Log.i("Files", "rejected data:$data, albumPath:$albumPath")
                    }
                } else
                    Log.w(
                        "Files",
                        "load failed : failed null-check in getVideosFromCursor(): data: $data | uri:$uri"
                    )
            }
            cursor.close()
            return videos
        }


    }
}