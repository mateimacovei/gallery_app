package com.example.gallery_app.activities

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery_app.*
import com.example.gallery_app.adapter.AbstractMediaObjectHolder
import com.example.gallery_app.adapter.ImageGridAdapter
import com.example.gallery_app.storageAccess.*
import com.example.gallery_app.storageAccess.StaticMethods.Companion.getNewPhotoArrayForAlbum
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_image_grid.*
import kotlinx.android.synthetic.main.image_grid_menu.*


class ImageGridActivity : AbstractGridActivity() {
    val holderImages: ArrayList<AbstractMediaObjectHolder> = ArrayList()
    private lateinit var imageGridAdapter: ImageGridAdapter
    lateinit var album: MyPhotoAlbum

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_grid)
        setSupportActionBar(image_grid_toolbar)

        this.onConfigurationChanged(this.resources.configuration)
        recycleViewerForImages.onFlingListener = MyOnFlingListener()

        album = Box.Get(intent, IMAGE_GRID_MESSAGE)
        Box.Remove(intent)

        Log.i("Files", "album being viewed: ${album.albumName}")
//        image_grid_toolbar.navigationIcon //TO DO
        layoutInflater.inflate(R.layout.image_grid_menu, image_grid_toolbar)
        if (album.albumName.length <= 20)
            titleTextView.text = album.albumName
        else
            titleTextView.text = (album.albumName.subSequence(0, 19).toString() + "...")

        loadPicturesFromAlbum(onCreate = true)

        Log.i("Activity", "onCreate exit")
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

    override fun enableSelectionMode() {
        selectionMode = true
        image_grid_toolbar.visibility = View.VISIBLE
        titleTextView.visibility = View.GONE
        imageGridNavigationImageButton.visibility = View.GONE
        subtitleTextView.visibility = View.GONE
        toolbarCheckBox.visibility = View.VISIBLE
        toolbarCheckBox.isChecked = (selected == album.mediaObjects.size)
        toolbarCheckBox.text = selected.toString()

        for (holder in holderImages)
            holder.enableSelectionMode()
    }

    override fun disableSelectionMode() {
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
    }

    private fun updateSubTitle() {
        var newSubtitle = ""
        if (album.nrPhotos != 0)
            newSubtitle += if (album.nrPhotos == 1)
                " 1 image "
            else
                album.nrPhotos.toString() + " images "
        if (album.nrVideos != 0)
            newSubtitle += if (album.nrVideos == 1)
                "1 video"
            else
                album.nrVideos.toString() + " videos"
        subtitleTextView.text = newSubtitle
    }

    private fun loadPicturesFromAlbum(force: Boolean = false, onCreate: Boolean = false) {
        Log.i("Activity", "loadPicturesFromAlbum entered: nrLoaded: $nrLoaded, force = $force, onCreate = $onCreate")
        if (nrLoaded < 3) {
            nrLoaded++
            if (nrLoaded == 2)
                return
        }
        val result = getNewPhotoArrayForAlbum(this)
        Log.i("Activity", "loadPicturesFromAlbum got result; shouldUpdate = ${result.first}")
        if (result.second.isEmpty()) {
            //TO DO elimina albumul din album grid
            finish()
        }
        if (onCreate or force or result.first) {
            album.mediaObjects.clear()
            album.mediaObjects.addAll(result.second)

            if (!onCreate)
                imageGridAdapter.notifyDataSetChanged()

            selected = album.getNrSelected()
            if (selected > 0)
                enableSelectionMode()
            else
                disableSelectionMode()
            updateSubTitle()

            if (onCreate) {
                imageGridAdapter = ImageGridAdapter(this, album.mediaObjects)
//        I MUST NOT REPLACE album.photos with a new arrayList. Instead, clear and add in the old one
                imageGridAdapter.setClickListener(this)
                recycleViewerForImages.adapter = imageGridAdapter
            }
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

    /**
     * sets grid_size by orientation ??TO DO have a member defining the desired column number
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recycleViewerForImages.layoutManager = getNewLayoutManager(newConfig)
    }

    private fun startFullscreenActivity(imageColorViewHolder: AbstractMediaObjectHolder) {
        imageColorViewHolder as ImageGridAdapter.ImageColorViewHolder

        Log.i("Files", "Image to open: ${imageColorViewHolder.myMediaObject}")
        val intentFullScreenImage = Intent(this, FullscreenImageActivity::class.java)

        Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_ARRAY, this.album.mediaObjects.clone())
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
    private fun reverseSelection(imageColorViewHolder: AbstractMediaObjectHolder) {
        if (imageColorViewHolder.isSelected())
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
            colorViewHolder: AbstractMediaObjectHolder,
    ) {
        if (view is ImageButton) { //important to check ImageButton first, as ImageButton extends ImageView
            startFullscreenActivity(colorViewHolder)
        } else {
            if (selectionMode) {
                reverseSelection(colorViewHolder)
            } else {
                startFullscreenActivity(colorViewHolder)
            }
        }
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onLongItemClick(
            view: View,
            position: Int,
            colorViewHolder: AbstractMediaObjectHolder,
    ) {
//        Toast.makeText(this, "Image LONG clicked $position", Toast.LENGTH_SHORT).show()
        if (!selectionMode) {
            enableSelectionMode()
        }
        reverseSelection(colorViewHolder)
    }

    override fun onResume() {
        Log.i("Activity", "onResume entered")
        super.onResume()
        loadPicturesFromAlbum()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.image_grid_layout_menu, menu)
        return true
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

        var sortBy = PreferencesFileHandler.getSortBy(this)
        var sortOrder = PreferencesFileHandler.getSortOrder(this)
        when (sortBy) {
            SortBy.NAME -> nameRadioButton.isChecked = true
            SortBy.DATE_MODIFIED -> dateModifiedRadioButton.isChecked = true
        }

        when (sortOrder) {
            SortOrder.DESC -> descendingRadioButton.isChecked = true
            SortOrder.ASC -> ascendingRadioButton.isChecked = true
        }

        val dialog = MaterialAlertDialogBuilder(this)
                .setView(customAlertDialogView)
                .setTitle("Sort by")
                .setNegativeButton("Cancel") { _, _ ->
                    Log.i("Dialog", "cancel clicked")
                }
                .setPositiveButton("Done") { _, _ ->
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
                        PreferencesFileHandler.updateSort(this, sortBy, sortOrder)
                        loadPicturesFromAlbum(force = true)
                    }
                }
                .create()

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                }
            }
        }
        dialog.show()
    }

    fun gridSizeMenuButtonClicked(item: MenuItem) {
        val customAlertDialogView = LayoutInflater.from(this)
                .inflate(R.layout.grid_size_dialog, null, false)

        val radioGroupGridSize: RadioGroup = customAlertDialogView.findViewById(R.id.radioGroupGridSize)
        val radioButton1: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize1)
        val radioButton2: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize2)
        val radioButton3: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize3)
        val radioButton4: RadioButton = customAlertDialogView.findViewById(R.id.radioButtonGridSize4)

        var gridSize = PreferencesFileHandler.getGridSize(this)
        when (gridSize) {
            GridSize.S1 -> radioButton1.isChecked = true
            GridSize.S2 -> radioButton2.isChecked = true
            GridSize.S3 -> radioButton3.isChecked = true
            GridSize.S4 -> radioButton4.isChecked = true
        }

        val dialog = MaterialAlertDialogBuilder(this)
                .setView(customAlertDialogView)
                .setTitle("Resize grid")
                .setNegativeButton("Cancel") { _, _ ->
                    Log.i("Dialog", "cancel clicked")
                }
                .setPositiveButton("Done") { _, _ ->
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
                        PreferencesFileHandler.updateGridSize(this, gridSize)
                        this.onConfigurationChanged(this.resources.configuration)

                        if (shouldShowFullscreenIcon(oldGridSize) != shouldShowFullscreenIcon(gridSize) && selectionMode && holderImages[0] is ImageGridAdapter.ImageColorViewHolder) {
                            when (shouldShowFullscreenIcon(gridSize)) {
                                true -> for (holder in holderImages) {
                                    holder as ImageGridAdapter.ImageColorViewHolder
                                    holder.imageButtonFullscreen.visibility = View.VISIBLE
                                }
                                false -> for (holder in holderImages) {
                                    holder as ImageGridAdapter.ImageColorViewHolder
                                    holder.imageButtonFullscreen.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
                .create()

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                }
            }
        }
        dialog.show()
    }

    fun goodImageGridSearchClicked(v: View) {
        Log.i("Buttons", "clicked good search")
    }

    fun backNavigationClicked(v: View) {
        onBackPressed()
    }

//    override fun onActivityReenter(resultCode: Int, data: Intent?) {
//        Log.i("Activity","onActivityReenter called")
//        super.onActivityReenter(resultCode, data)
//    }
}