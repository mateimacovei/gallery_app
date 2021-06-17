package com.example.gallery_app.storageAccess.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class TagToMediaObject(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="rowid")
    val rowId: Long? = null,
    val tagId: Long?,
    val mediaObjectId: Long?
) {

    override fun toString(): String {
        return "TagToMediaObject(rowId=$rowId, tagId=$tagId, mediaObjectId=$mediaObjectId)"
    }
}