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

    protected var nrLoaded = 0

    protected fun getNewLayoutManager(newConfig: Configuration): StaggeredGridLayoutManager {
        var nrColumns by Delegates.notNull<Int>()
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT ->
                nrColumns = getPortraitGridColumns(PreferencesFileHandler.getGridSize(this))
            Configuration.ORIENTATION_LANDSCAPE ->
                nrColumns = getLandscapeGridColumns(PreferencesFileHandler.getGridSize(this))
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