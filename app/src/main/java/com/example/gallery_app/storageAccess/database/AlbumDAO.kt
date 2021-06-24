package com.example.gallery_app.storageAccess.database

import androidx.room.*
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum
import com.example.gallery_app.storageAccess.domain.TagToMediaObject


@Dao
interface AlbumDAO {
    @Query("SELECT * FROM MyPhotoAlbum ORDER BY position")
    fun getAll(): List<MyPhotoAlbum>

    @Query("SELECT * FROM MyPhotoAlbum WHERE `ignore`=:getIgnored ORDER BY position")
    fun getAll(getIgnored: Boolean): List<MyPhotoAlbum>

    @Insert
    fun insertAll(vararg myPhotoAlbum: MyPhotoAlbum)

    @Delete
    fun delete(myPhotoAlbum: MyPhotoAlbum)

    @Query("DELETE FROM MyPhotoAlbum")
    fun deleteAll()

    @Query("DELETE FROM MyPhotoAlbum WHERE rowid in (:idList)")
    fun deleteFromList(idList: List<Long>)

    @Update
    fun updateMyPhotoAlbum(myPhotoAlbum: MyPhotoAlbum)
}