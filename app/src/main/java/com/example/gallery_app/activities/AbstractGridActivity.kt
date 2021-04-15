package com.example.gallery_app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gallery_app.R
import com.example.gallery_app.adapter.MyClickListener
import com.example.gallery_app.storageAccess.*
import kotlinx.android.synthetic.main.image_grid_menu.*
import kotlin.properties.Delegates

const val VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD: Long = 150

abstract class AbstractGridActivity : AppCompatActivity(), MyClickListener {
    //I need both, because I can have selection mode on with 0 selected
    var selectionMode: Boolean = false
    protected var selected = 0

    lateinit var sortBy: SortBy
    lateinit var sortOrder: SortOrder
    lateinit var gridSize: GridSize

    /**
     * updates the sort preferences to the values currently set
     */
    protected fun updatePreferencesFile() {
        val pref = applicationContext.getSharedPreferences("MyPref", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        when (sortBy) {
            SortBy.DATE_MODIFIED -> editor.putString("sortBy", "DATE_MODIFIED")
            SortBy.NAME -> editor.putString("sortBy", "NAME")
        }
        when (sortOrder) {
            SortOrder.DESC -> editor.putString("sortOrder", "DESC")
            SortOrder.ASC -> editor.putString("sortOrder", "ASC")
        }
        when (gridSize) {
            GridSize.S1 -> editor.putString("gridSize", "S1")
            GridSize.S2 -> editor.putString("gridSize", "S2")
            GridSize.S3 -> editor.putString("gridSize", "S3")
            GridSize.S4 -> editor.putString("gridSize", "S4")
        }
        editor.apply()
    }

    protected fun loadPreferences() {
        val pref = applicationContext.getSharedPreferences("MyPref", MODE_PRIVATE)
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

    protected fun getNewLayoutManager(newConfig: Configuration): StaggeredGridLayoutManager {
        var nrColumns by Delegates.notNull<Int>()
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT ->
                nrColumns = getPortraitGridColumns(gridSize)
            Configuration.ORIENTATION_LANDSCAPE ->
                nrColumns = getLandscapeGridColumns(gridSize)
            else -> Log.w(
                "Orientation",
                "Orientation was undefined at configuration change"
            )
        }
        Log.i("Orientation", "new layout columns: $nrColumns")
        return StaggeredGridLayoutManager(nrColumns, StaggeredGridLayoutManager.VERTICAL)
    }

    abstract fun enableSelectionMode()
    abstract fun disableSelectionMode()

    override fun onBackPressed() {
        if (!this.selectionMode) {
//            super.onBackPressed()
            val data = Intent()
//            data.putExtra("myData1", "Data 1 value")
            setResult(RESULT_OK, data)
            finish()
        } else {
            disableSelectionMode()
        }
    }




}