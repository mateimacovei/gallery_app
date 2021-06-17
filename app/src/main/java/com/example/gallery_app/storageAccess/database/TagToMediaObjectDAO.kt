package com.example.gallery_app.storageAccess.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.gallery_app.storageAccess.domain.TagToMediaObject


@Dao
interface TagToMediaObjectDAO {
    @Query("SELECT * FROM TagToMediaObject")
    fun getAll(): List<TagToMediaObject>

    @Query("SELECT * FROM TagToMediaObject WHERE tagId=:tagId")
    fun getByTagId(tagId: Long): List<TagToMediaObject>

    @Query("SELECT * FROM TagToMediaObject WHERE tagId=:mediaObjectId")
    fun getByMediaObjectId(mediaObjectId: Long): List<TagToMediaObject>

    @Insert
    fun insertAll(vararg tagToMediaObjects: TagToMediaObject)

    @Delete
    fun delete(tagToMediaObject: TagToMediaObject)

    @Query("DELETE FROM TagToMediaObject")
    fun deleteAll()

    @Query("DELETE FROM TagToMediaObject WHERE rowid in (:idList)")
    fun deleteFromList(idList: List<Long>)
}