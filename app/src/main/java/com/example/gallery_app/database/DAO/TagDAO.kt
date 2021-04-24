package com.example.gallery_app.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.gallery_app.database.domain.Tag

@Dao
interface TagDAO {
    @Query("SELECT * FROM Tag")
    fun getAll(): List<Tag>

    @Query("SELECT * FROM Tag WHERE rowid IN (:tagsIds)")
    fun loadAllByIds(tagsIds: IntArray): List<Tag>

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): Tag

    @Insert
    fun insertAll(vararg tags: Tag)

    @Delete
    fun delete(user: Tag)

    @Query("DELETE FROM Tag WHERE rowid = (:id)")
    fun deleteOneById(id: Long)
}