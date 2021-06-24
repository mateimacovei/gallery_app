package com.example.gallery_app.storageAccess.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MediaObjectDAO {

//    @Query()
//    fun getOne(fullPath: String): Single<MyMediaObject>

    @Query("DELETE FROM MyMediaObject")
    fun deleteAll()
}