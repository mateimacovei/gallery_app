package com.example.gallery_app.storageAccess

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class PreferencesFileHandler {
    companion object {
        private var sortBy: SortBy? = null
        private var sortOrder: SortOrder? = null
        private var gridSize: GridSize? = null

        fun updateSort(context: Context, sortBy: SortBy, sortOrder: SortOrder) {
            this.sortBy = sortBy
            this.sortOrder = sortOrder
            val pref = context.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            when (sortBy) {
                SortBy.DATE_MODIFIED -> editor.putString("sortBy", "DATE_MODIFIED")
                SortBy.NAME -> editor.putString("sortBy", "NAME")
            }
            when (sortOrder) {
                SortOrder.DESC -> editor.putString("sortOrder", "DESC")
                SortOrder.ASC -> editor.putString("sortOrder", "ASC")
            }
            editor.apply()
        }

        fun updateGridSize(context: Context, gridSize: GridSize) {
            this.gridSize = gridSize
            val pref = context.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            when (gridSize) {
                GridSize.S1 -> editor.putString("gridSize", "S1")
                GridSize.S2 -> editor.putString("gridSize", "S2")
                GridSize.S3 -> editor.putString("gridSize", "S3")
                GridSize.S4 -> editor.putString("gridSize", "S4")
            }
            editor.apply()
        }

        fun getSortBy(context: Context): SortBy {
            if (sortBy == null) {
                val pref = context.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
                when (pref.getString("sortBy", "null")) {
                    "NAME" -> sortBy = SortBy.NAME
                    "DATE_MODIFIED" -> sortBy = SortBy.DATE_MODIFIED
                    else -> {
                        sortBy = SortBy.DATE_MODIFIED
                        val editor: SharedPreferences.Editor = pref.edit()
                        editor.putString("sortBy", "DATE_MODIFIED")
                        editor.apply()
                    }
                }
            }
            return sortBy as SortBy
        }

        fun getSortOrder(context: Context): SortOrder {
            if (sortOrder == null) {
                val pref = context.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
                when (pref.getString("sortOrder", "null")) {
                    "DESC" -> sortOrder = SortOrder.DESC
                    "ASC" -> sortOrder = SortOrder.ASC
                    else -> {
                        sortOrder = SortOrder.DESC
                        val editor: SharedPreferences.Editor = pref.edit()
                        editor.putString("sortOrder", "DESC")
                        editor.apply()
                    }
                }
            }
            return sortOrder as SortOrder
        }

        fun getGridSize(context: Context): GridSize {
            if (gridSize == null) {
                val pref = context.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
                when (pref.getString("gridSize", "null")) {
                    "S1" -> gridSize = GridSize.S1
                    "S2" -> gridSize = GridSize.S2
                    "S3" -> gridSize = GridSize.S3
                    "S4" -> gridSize = GridSize.S4
                    else -> {
                        gridSize = GridSize.S1
                        val editor: SharedPreferences.Editor = pref.edit()
                        editor.putString("gridSize", "S1")
                        editor.apply()
                    }
                }
            }
            return gridSize as GridSize
        }
    }
}