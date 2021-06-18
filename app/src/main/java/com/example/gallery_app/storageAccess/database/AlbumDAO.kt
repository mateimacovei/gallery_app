package com.example.gallery_app.storageAccess.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum
import com.example.gallery_app.storageAccess.domain.TagToMediaObject


@Dao
interface AlbumDAO {
    @Query("SELECT * FROM MyPhotoAlbum")
    fun getAll(): List<MyPhotoAlbum>

    @Query("SELECT * FROM MyPhotoAlbum WHERE `ignore`=:getIgnored")
    fun getAll(getIgnored: Boolean): List<MyPhotoAlbum>

    @Insert
    fun insertAll(vararg myPhotoAlbum: MyPhotoAlbum)

    @Delete
    fun delete(myPhotoAlbum: MyPhotoAlbum)

    @Query("DELETE FROM MyPhotoAlbum")
    fun deleteAll()

    @Query("DELETE FROM MyPhotoAlbum WHERE rowid in (:idList)")
    fun deleteFromList(idList: List<Long>)
}