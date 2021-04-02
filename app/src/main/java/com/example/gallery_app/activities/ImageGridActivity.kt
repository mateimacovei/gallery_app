package com.example.gallery_app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gallery_app.*
import com.example.gallery_app.adapter.ImageGridAdapter
import com.example.gallery_app.adapter.clickListenerInterfaces.ImageItemClickListener
import com.example.gallery_app.storageAccess.*
import com.example.gallery_app.storageAccess.StaticMethods.Companion.getNewPhotoArrayForAlbum
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_image_grid.*
import kotlinx.android.synthetic.main.image_grid_menu.*
import kotlin.properties.Delegates


const val VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD: Long = 150

class ImageGridActivity : AppCompatActivity(),
    ImageItemClickListener {
    //I need both, because I can have selection mode on with 0 selected
    var selectionMode: Boolean = false
    private var selected = 0

    val holderImages: ArrayList<ImageGridAdapter.ImageColorViewHolder> = ArrayList()
    lateinit var imageGridAdapter: ImageGridAdapter
    lateinit var album: MyPhotoAlbum

    lateinit var sortBy: SortBy
    lateinit var sortOrder: SortOrder
    lateinit var gridSize: GridSize

    /**
     * updates the sort preferences to the values currently set
     */
    private fun updatePreferencesFile() {
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

    private fun loadPreferences() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_grid)
        setSupportActionBar(image_grid_toolbar)

        loadPreferences()

        this.onConfigurationChanged(this.resources.configuration)
        val onFlingListener = MyOnFlingListener()
        recycleViewerForImages.onFlingListener = onFlingListener

        album = Box.Get(intent, IMAGE_GRID_MESSAGE)
        Box.Remove(intent)

        Log.i("Files", "album being viewed: ${album.albumName}")
//        image_grid_toolbar.navigationIcon //TO DO
        layoutInflater.inflate(R.layout.image_grid_menu, image_grid_toolbar)
        if (album.albumName.length <= 20)
            titleTextView.text = album.albumName
        else
            titleTextView.text = (album.albumName.subSequence(0, 19).toString() + "...")


        loadPicturesFromAlbum()

        Log.i("Activity", "onCreate exit")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.image_grid_layout_menu, menu)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES ->
                for (i in 0 until menu!!.size()) {
                    val item = menu.getItem(i)
                    val spanString = SpannableString(menu.getItem(i).title.toString())
                    spanString.setSpan(ForegroundColorSpan(Color.WHITE), 0, spanString.length, 0) //fix the color to white
                    item.title = spanString
                }
            Configuration.UI_MODE_NIGHT_NO -> {
            }
        }

        return true
    }


    private fun selectAll() {
        for (holder in holderImages)
            holder.setAsSelected()
        for (photo in album.mediaObjects)
            photo.selected = true
        //both are needed, as there could be pictures selected that are in a holder that has been removed due to scrolling

        selected = album.mediaObjects.size
        toolbarCheckBox.text = selected.toString()
    }

    private fun unselectAll() {
        for (holder in holderImages)
            holder.setAsUnselected()
        for (photo in album.mediaObjects)
            photo.selected = false
        selected = 0
        toolbarCheckBox.text = selected.toString()
    }

    private fun enableSelectionMode() {
        selectionMode = true
        image_grid_toolbar.visibility = View.VISIBLE
        titleTextView.visibility = View.GONE
        imageGridNavigationImageButton.visibility = View.GONE
        subtitleTextView.visibility = View.GONE
        toolbarCheckBox.visibility = View.VISIBLE
        if (selected == album.mediaObjects.size)
            toolbarCheckBox.isChecked = true
        toolbarCheckBox.text = selected.toString()

        for (holder in holderImages)
            holder.enableSelectionMode()
    }

    private fun disableSelectionMode() {
        titleTextView.visibility = View.VISIBLE
        subtitleTextView.visibility = View.VISIBLE
        imageGridNavigationImageButton.visibility = View.VISIBLE
//        this.title = album.albumName
//        image_grid_toolbar.subtitle = album.photos.size.toString()
        selectionMode = false
        selected = 0
        toolbarCheckBox.isChecked = false
        toolbarCheckBox.visibility = View.GONE
        for (holder in holderImages)
            holder.disableSelectionMode() //it's different from unselect all
        for (photo in album.mediaObjects)
            photo.selected = false
        selected = 0
        subtitleTextView.text = (album.mediaObjects.size.toString() + " images")
    }

    private fun loadPicturesFromAlbum() {
        Log.i("Activity", "loadPicturesFromAlbum entered")
        val result = getNewPhotoArrayForAlbum(this)
        if (result.second.size == 0) {
            //TO DO elimina albumul din album grid
            finish()
        }
        album.mediaObjects.clear()
        album.mediaObjects.addAll(result.second)

        selected = album.getNrSelected()
        if (selected > 0)
            enableSelectionMode()
        else
            disableSelectionMode()


        imageGridAdapter = ImageGridAdapter(this, album.mediaObjects)
//        I MUST NOT REPLACE album.photos with a new arrayList. Instead, clear and add in the old one
        imageGridAdapter.setClickListener(this)
        recycleViewerForImages.adapter = imageGridAdapter
    }

    private fun reloadPicturesFromAlbum(force: Boolean = false) {
        val result = getNewPhotoArrayForAlbum(this)
        Log.i("Images", "reloadPicturesFromAlbum. update=${result.first} | force=$force")
        if (result.first or force) {
            if (result.second.size == 0) {
                //TO DO elimina albumul din album grid
                finish()
            }
            album.mediaObjects.clear()
            album.mediaObjects.addAll(result.second)
            imageGridAdapter.notifyDataSetChanged()

            selected = album.getNrSelected()
            if (selected > 0)
                enableSelectionMode()
            else
                disableSelectionMode()
        }
    }

    inner class MyOnFlingListener : RecyclerView.OnFlingListener() {
        override fun onFling(velocityX: Int, velocityY: Int): Boolean {
            if (!selectionMode) {
                if (velocityY > VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD) {
                    Log.i("Scroll", "scroll down")
                    image_grid_toolbar.visibility = View.GONE
                }
                if (velocityY < VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD) {
                    Log.i("Scroll", "scroll up")
                    image_grid_toolbar.visibility = View.VISIBLE
                }
            }
            return false //it was not handled. If I set it to true, it will not pass the event on to the inertia method
        }

    }

    override fun onBackPressed() {
        if (!this.selectionMode)
            super.onBackPressed()
        else {
            disableSelectionMode()
        }
    }

    /**
     * sets grid_size by orientation ??TO DO have a member defining the desired column number
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var nrColumns by Delegates.notNull<Int>()
        when (newConfig.orientation) {
            ORIENTATION_PORTRAIT ->
                nrColumns = getPortraitGridColumns(gridSize)
            ORIENTATION_LANDSCAPE ->
                nrColumns = getLandscapeGridColumns(gridSize)
            else -> Log.w(
                    "Orientation",
                    "Orientation in ImageGridKotlinActivity was undefined at configuration change"
            )
        }
        recycleViewerForImages.layoutManager = StaggeredGridLayoutManager(nrColumns, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun startFullscreenActivity(imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder) {
        Log.i("Files", "Image to open: ${imageColorViewHolder.myMediaObject}")
        val intentFullScreenImage = Intent(this, FullscreenImageActivity::class.java)

        Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_ARRAY, this.album.mediaObjects)
        Box.Add(
                intentFullScreenImage,
                FULLSCREEN_IMAGE_POSITION,
                imageColorViewHolder.photoPositionInMyArray
        )

        this.startActivity(intentFullScreenImage)
    }

    /**
     * +- selected
     * update toolbarCheckBox
     * calls imageColorViewHolder.reverseSelection()
     */
    private fun reverseSelection(imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder) {
        if (imageColorViewHolder.myMediaObject.selected)
            selected--
        else selected++
        toolbarCheckBox.text = selected.toString()
        toolbarCheckBox.isChecked = (selected == album.mediaObjects.size)

        imageColorViewHolder.reverseSelection()
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onItemClick(
            view: View,
            position: Int,
            imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder,
    ) {
        if (view is ImageButton) { //important to check ImageButton first, as ImageButton extends ImageView
            startFullscreenActivity(imageColorViewHolder)
        } else {
            if (selectionMode) {
                reverseSelection(imageColorViewHolder)
            } else {
                startFullscreenActivity(imageColorViewHolder)
            }
        }
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onLongItemClick(
            view: View,
            position: Int,
            imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder,
    ) {
//        Toast.makeText(this, "Image LONG clicked $position", Toast.LENGTH_SHORT).show()
        if (!selectionMode) {
            enableSelectionMode()
        }
        reverseSelection(imageColorViewHolder)
    }

    override fun onResume() {
        Log.i("Activity", "onResume entered")
        super.onResume()
        reloadPicturesFromAlbum()
        Log.i("Activity", "onResume exit")
    }

    fun toolbarCheckBoxClicked(v: View) {
        if (selectionMode) {
            if (toolbarCheckBox.isChecked)
                selectAll()
            else
                unselectAll()
        } else Log.w("Error", "checkbox was clicked outside of selectionMode")
    }

    fun selectMenuButtonClicked(item: MenuItem) {
        toolbarCheckBox.text = "0"
        enableSelectionMode()
    }

    fun sortMenuButtonClicked(item: MenuItem) {
        val customAlertDialogView = LayoutInflater.from(this)
                .inflate(R.layout.sort_menu_dialog, null, false)

        val radioGroupSortBy: RadioGroup = customAlertDialogView.findViewById(R.id.radioGroupSortBy)
        val radioGroupSortOrder: RadioGroup = customAlertDialogView.findViewById(R.id.RadioGroupSortOrder)

        val nameRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize4)
        val dateModifiedRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize3)

        val descendingRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.descendingRadioButton)
        val ascendingRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.ascendingRadioButton)

        when (sortBy) {
            SortBy.NAME -> nameRadioButton.isChecked = true
            SortBy.DATE_MODIFIED -> dateModifiedRadioButton.isChecked = true
        }

        when (sortOrder) {
            SortOrder.DESC -> descendingRadioButton.isChecked = true
            SortOrder.ASC -> ascendingRadioButton.isChecked = true
        }

        MaterialAlertDialogBuilder(this)
                .setView(customAlertDialogView)
                .setTitle("Sort by")
                .setNegativeButton("Cancel") { dialog, which ->
                    Log.i("Dialog", "cancel clicked")
                }
                .setPositiveButton("Done") { dialog, which ->
                    Log.i("Dialog", "done clicked")
                    var checkId = radioGroupSortBy.checkedRadioButtonId
                    var radioButton: View = radioGroupSortBy.findViewById(checkId)

                    val oldSortBy = sortBy
                    val oldSortOrder = sortOrder

                    when (radioGroupSortBy.indexOfChild(radioButton)) {
                        0 -> sortBy = SortBy.DATE_MODIFIED
                        1 -> sortBy = SortBy.NAME
                    }

                    checkId = radioGroupSortOrder.checkedRadioButtonId
                    radioButton = radioGroupSortOrder.findViewById(checkId)

                    when (radioGroupSortOrder.indexOfChild(radioButton)) {
                        0 -> sortOrder = SortOrder.ASC
                        1 -> sortOrder = SortOrder.DESC
                    }

                    if (oldSortBy != sortBy || oldSortOrder != sortOrder) {
                        reloadPicturesFromAlbum(force = true)
                        updatePreferencesFile()
                    }
                }
                .show()
    }

    fun gridSizeMenuButtonClicked(item: MenuItem) {
        val customAlertDialogView = LayoutInflater.from(this)
                .inflate(R.layout.grid_size_dialog, null, false)

        val radioGroupGridSize: RadioGroup = customAlertDialogView.findViewById(R.id.radioGroupGridSize)
        val radioButton1: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize1)
        val radioButton2: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize2)
        val radioButton3: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize3)
        val radioButton4: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize4)

        when (gridSize) {
            GridSize.S1 -> radioButton1.isChecked = true
            GridSize.S2 -> radioButton2.isChecked = true
            GridSize.S3 -> radioButton3.isChecked = true
            GridSize.S4 -> radioButton4.isChecked = true
        }

        MaterialAlertDialogBuilder(this)
                .setView(customAlertDialogView)
                .setTitle("Resize grid")
                .setNegativeButton("Cancel") { dialog, which ->
                    Log.i("Dialog", "cancel clicked")
                }
                .setPositiveButton("Done") { dialog, which ->
                    Log.i("Dialog", "done clicked")
                    val checkId = radioGroupGridSize.checkedRadioButtonId
                    val radioButton: View = radioGroupGridSize.findViewById(checkId)

                    val oldGridSize = gridSize

                    when (radioGroupGridSize.indexOfChild(radioButton)) {
                        0 -> gridSize = GridSize.S1
                        1 -> gridSize = GridSize.S2
                        2 -> gridSize = GridSize.S3
                        3 -> gridSize = GridSize.S4
                    }

                    if (oldGridSize != gridSize) {
                        this.onConfigurationChanged(this.resources.configuration)
                        updatePreferencesFile()

                        if (shouldShowFullscreenIcon(oldGridSize) != shouldShowFullscreenIcon(gridSize) && selectionMode) {
                            when (shouldShowFullscreenIcon(gridSize)) {
                                true -> for (holder in holderImages)
                                    holder.imageButtonFullscreen.visibility = View.VISIBLE
                                false -> for (holder in holderImages)
                                    holder.imageButtonFullscreen.visibility = View.GONE
                            }
                        }
                    }
                }
                .show()
    }

    fun goodImageGridSearchClicked(v: View) {
        Log.i("Buttons", "clicked good search")
    }

    fun backNavigationClicked(view: View) {
        onBackPressed()
    }

//    override fun onActivityReenter(resultCode: Int, data: Intent?) {
//        Log.i("Activity","onActivityReenter called")
//        super.onActivityReenter(resultCode, data)
//    }
}