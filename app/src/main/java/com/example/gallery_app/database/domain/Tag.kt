package com.example.gallery_app.database.domain

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4
@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) val rowid: Int? = null,
    val name: String?
){

}