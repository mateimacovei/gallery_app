package com.example.gallery_app.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gallery_app.database.DAO.TagDAO
import com.example.gallery_app.database.domain.Tag

@Database(entities = [Tag::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDAO
}