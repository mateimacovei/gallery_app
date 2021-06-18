package com.example.gallery_app.storageAccess.repo

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.gallery_app.storageAccess.database.AlbumDAO
import com.example.gallery_app.storageAccess.database.AppDatabase
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum
import com.example.gallery_app.storageAccess.domain.Tag
import com.example.gallery_app.storageAccess.domain.TagToMediaObject

class Repo(private val application: Application) {
    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "my-gallery-database"
    )
        .fallbackToDestructiveMigration()
        .build()

    val tagDAO = database.tagDao()
    val tagToMediaObjectDAO = database.tagToMediaObjectDAO()
    private val albumDAO: AlbumDAO = database.albumDAO()


    fun testTag(db: AppDatabase) {
        val tagDAO = db.tagDao()
        tagDAO.deleteAll()
        tagDAO.insertAll(Tag(name = "test tag 1"), Tag(name = "test tag 2"), Tag(name = "ははは"))
        val tags = tagDAO.getAll()
        val firstTag = tagDAO.getByName("test tag 1")
        val secondTag = tagDAO.getByName("test tag 2")
        tagDAO.delete(firstTag[0])
        secondTag[0].rowId?.let { tagDAO.deleteOneById(it) }
        val tagsAfterDel = tagDAO.getAll()
        db.close()

        Log.i("Database", "first tag: $firstTag")
        Log.i("Database", "second tag: $secondTag")
        Log.i("Database", "all tags")
        tags.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "tags after del")
        tagsAfterDel.forEach { run { Log.i("Database", "$it") } }
    }

    fun testTagToMO(db: AppDatabase) {
        val tagToMediaObjectDAO = db.tagToMediaObjectDAO()
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
        db.close()

        Log.i("Database", "first TagToMediaObject: $firstTagToMO")
        Log.i("Database", "second TagToMediaObject: $secondTagToMO")
        Log.i("Database", "all TagToMediaObjects")
        all.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "TagToMediaObjects after del")
        tagToMediaObjectsAfterDel.forEach { run { Log.i("Database", "$it") } }
    }

    fun testAlbum(db: AppDatabase) {
        val albumDAO = db.albumDAO()
        albumDAO.deleteAll()
        albumDAO.insertAll(
            MyPhotoAlbum(albumFullPath = "/album1/", albumName = "album1",ignore = false, thumbnailPath = "th1"),
            MyPhotoAlbum(albumFullPath = "/album2/", albumName = "album2",ignore = false, thumbnailPath = "th1"),
            MyPhotoAlbum(albumFullPath = "/albumhid1/", albumName = "albumhid1",ignore = true, thumbnailPath = ""),
            MyPhotoAlbum(albumFullPath = "/albumhid2/", albumName = "albumhid2",ignore = true, thumbnailPath = ""),
            MyPhotoAlbum(albumFullPath = "/album3/", albumName = "album3",ignore = false, thumbnailPath = "th3")
        )
        val albums = albumDAO.getAll()
        val visible = albumDAO.getAll(false)
        val hidden = albumDAO.getAll(true)
        albumDAO.delete(visible[0])
        albumDAO.deleteFromList(ArrayList<Long>().apply { hidden[0].rowId?.let { add(it) } })
        val albumsAfterDel = albumDAO.getAll()
        db.close()

        Log.i("Database", "all albums")
        albums.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "visible albums")
        visible.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "hidden albums")
        hidden.forEach { run { Log.i("Database", "$it") } }

        Log.i("Database", "albums after del")
        albumsAfterDel.forEach { run { Log.i("Database", "$it") } }
    }
}