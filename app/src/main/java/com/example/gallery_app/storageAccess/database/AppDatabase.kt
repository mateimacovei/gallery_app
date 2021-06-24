package com.example.gallery_app.storageAccess.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gallery_app.storageAccess.domain.MyMediaObject
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum
import com.example.gallery_app.storageAccess.domain.Tag
import com.example.gallery_app.storageAccess.domain.TagToMediaObject

@Database(entities = [MyPhotoAlbum::class, MyMediaObject::class,Tag::class, TagToMediaObject::class], version = 6)
//@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDAO
    abstract fun tagToMediaObjectDAO(): TagToMediaObjectDAO
    abstract fun albumDAO(): AlbumDAO
    abstract fun mediaObjectsDAO(): MediaObjectDAO
}