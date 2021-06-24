package com.example.gallery_app.storageAccess.repo

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.gallery_app.storageAccess.StaticMethods
import com.example.gallery_app.storageAccess.database.AlbumDAO
import com.example.gallery_app.storageAccess.database.AppDatabase
import com.example.gallery_app.storageAccess.domain.MyMediaObject
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum
import com.example.gallery_app.storageAccess.domain.Tag
import com.example.gallery_app.storageAccess.domain.TagToMediaObject
import kotlin.concurrent.thread

class Repo(private val application: Application) {
    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "my-gallery-database"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val tagDAO = database.tagDao()
    private val tagToMediaObjectDAO = database.tagToMediaObjectDAO()
    private val mediaObjectDAO = database.mediaObjectsDAO()
    private val albumDAO: AlbumDAO = database.albumDAO()

    init {
//        testAlbum()

//        thread {
//            albumDAO.deleteAll()
//        }
    }

    fun getTagsForMediaObject(mediaObject: MyMediaObject): List<Tag> {
        val tags = ArrayList<Tag>()
        val picture =
        return tags
    }

    fun getRoomAlbums(): List<MyPhotoAlbum> = albumDAO.getAll()

    private fun getMediaStoreAlbums(): ArrayList<MyPhotoAlbum> = StaticMethods.getAllAlbums(application)

    /**
     * updates the input list
     * I use this method instead of simply creating a new list in order to keep the album's order
     * @return if the ui should be updated
     */
    fun updateAlbums(roomAlbums: MutableList<MyPhotoAlbum>): Boolean {
//        albumDAO.deleteAll()
        Log.i("Room","first album: ${roomAlbums[0]}")
        var shouldUpdate = false
        val mediaStoreAlbums = getMediaStoreAlbums()

        val toDelete = ArrayList<Long>()

        for(roomAlbum in roomAlbums){
            val mediaStoreAlbum = roomAlbum.findSimilar(mediaStoreAlbums)
            if(mediaStoreAlbum!=null){
                mediaStoreAlbums.remove(mediaStoreAlbum)
                roomAlbum.mediaObjects = mediaStoreAlbum.mediaObjects
                if(roomAlbum.isDifferent(mediaStoreAlbum)){
                    shouldUpdate = true
                    roomAlbum.size = mediaStoreAlbum.mediaObjects.size
                    roomAlbum.uriLong = mediaStoreAlbum.uriLong
                }

            }
            else
                toDelete.add(roomAlbum.rowId!!)
        }

        if(toDelete.size>0 || mediaStoreAlbums.size>0)
            shouldUpdate = true

        roomAlbums.removeIf { toDelete.contains(it.rowId) }
        //for the albums stored in the db that have since been deleted

        roomAlbums.addAll(mediaStoreAlbums)
        if(shouldUpdate)
            asyncStoreAlbums(roomAlbums)
        return shouldUpdate
    }

    private fun asyncStoreAlbums(mediaStoreAlbums: List<MyPhotoAlbum>, deletePrevious: Boolean = true) {
        thread {
            Log.i("Room","entered asyncStoreAlbums")
            if (deletePrevious)
                albumDAO.deleteAll()
            for ((index, album) in mediaStoreAlbums.withIndex()) {
                //uriLong and size are already set
                album.position = index
            }
            albumDAO.insertAll(*mediaStoreAlbums.toTypedArray())
        }
    }


//    fun getAlbums(): List<MyPhotoAlbum>?{
//        val mediaStoreAlbums = StaticMethods.getAllAlbums(application)
//        val roomAlbums = albumDAO.getAll()
//
//        val toDelete = ArrayList<Long>()
//        val toInsert = ArrayList<MyPhotoAlbum>()
//
//        //TO DO
//
//        thread {
//            albumDAO.deleteFromList(toDelete)
//            albumDAO.insertAll(*toInsert.toTypedArray())
//        }
//        return mediaStoreAlbums
//    }

    fun testTag() {
        val tagDAO = database.tagDao()
        tagDAO.deleteAll()
        tagDAO.insertAll(Tag(name = "test tag 1"), Tag(name = "test tag 2"), Tag(name = "ははは"))
        val tags = tagDAO.getAll()
        val firstTag = tagDAO.getByName("test tag 1")
        val secondTag = tagDAO.getByName("test tag 2")
        tagDAO.delete(firstTag[0])
        secondTag[0].rowId?.let { tagDAO.deleteOneById(it) }
        val tagsAfterDel = tagDAO.getAll()
        tagDAO.deleteAll()

        Log.i("Database", "first tag: $firstTag")
        Log.i("Database", "second tag: $secondTag")
        Log.i("Database", "all tags")
        tags.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "tags after del")
        tagsAfterDel.forEach { run { Log.i("Database", "$it") } }
    }

    fun testTagToMO() {
        val tagToMediaObjectDAO = database.tagToMediaObjectDAO()
        tagToMediaObjectDAO.deleteAll()
        tagToMediaObjectDAO.insertAll(
            TagToMediaObject(tagId = 1L, mediaObjectId = 1L),
            TagToMediaObject(tagId = 2L, mediaObjectId = 2L),
            TagToMediaObject(tagId = 3L, mediaObjectId = 3L)
        )
        val all = tagToMediaObjectDAO.getAll()
        val firstTagToMO = tagToMediaObjectDAO.getByTagId(1L)
        val secondTagToMO = tagToMediaObjectDAO.getByMediaObjectId(2L)
        tagToMediaObjectDAO.delete(firstTagToMO[0])

        val list = ArrayList<Long>()
        firstTagToMO[0].rowId?.let { list.add(it) }
        secondTagToMO[0].rowId?.let { list.add(it) }
        tagToMediaObjectDAO.deleteFromList(list)
        val tagToMediaObjectsAfterDel = tagToMediaObjectDAO.getAll()
        tagToMediaObjectDAO.deleteAll()

        Log.i("Database", "first TagToMediaObject: $firstTagToMO")
        Log.i("Database", "second TagToMediaObject: $secondTagToMO")
        Log.i("Database", "all TagToMediaObjects")
        all.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "TagToMediaObjects after del")
        tagToMediaObjectsAfterDel.forEach { run { Log.i("Database", "$it") } }
    }

    fun testAlbum() {
        val albumDAO = database.albumDAO()
        albumDAO.deleteAll()
        albumDAO.insertAll(
            MyPhotoAlbum(albumFullPath = "/album1/", albumName = "album1",ignore = false, size=1, position = 1),
            MyPhotoAlbum(albumFullPath = "/album2/", albumName = "album2",ignore = false,size=1, position = 3),
            MyPhotoAlbum(albumFullPath = "/albumHd1/", albumName = "albumHid1",ignore = true, size=1),
            MyPhotoAlbum(albumFullPath = "/albumHId2/", albumName = "albumHid2",ignore = true, size=1),
            MyPhotoAlbum(albumFullPath = "/album3/", albumName = "album3",ignore = false, size=1, position = 2)
        )
        val albums = albumDAO.getAll()
        val visible = albumDAO.getAll(false)
        val hidden = albumDAO.getAll(true)
        albumDAO.delete(visible[0])
        albumDAO.deleteFromList(ArrayList<Long>().apply { hidden[0].rowId?.let { add(it) } })
        val albumsAfterDel = albumDAO.getAll()

        visible[1].albumName = "changed"

        val albumsUpdate = albumDAO.getAll()
        albumDAO.deleteAll()

        Log.i("Database", "all albums")
        albums.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "visible albums")
        visible.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "hidden albums")
        hidden.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "albums after del")
        albumsAfterDel.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "albums after update")
        albumsUpdate.forEach { run { Log.i("Database", "$it") } }
    }
}