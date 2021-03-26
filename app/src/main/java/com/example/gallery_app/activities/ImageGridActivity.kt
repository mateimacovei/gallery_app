package com.example.gallery_app.activities

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
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
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhotoAlbum
import com.example.gallery_app.storageAccess.SortBy
import com.example.gallery_app.storageAccess.SortOrder
import com.example.gallery_app.storageAccess.StaticMethods.Companion.getNewPhotoArrayForAlbum
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_image_grid.*
import kotlinx.android.synthetic.main.image_grid_menu.*

const val VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD: Long = 150

class ImageGridActivity : AppCompatActivity(),
    ImageItemClickListener {
    //I need both, because I can have selection mode on with 0 selected
    var selectionMode: Boolean = false
    private var selected = 0

    val holderImages: ArrayList<ImageGridAdapter.ImageColorViewHolder> = ArrayList()
    lateinit var imageGridAdapter: ImageGridAdapter
    lateinit var album: MyPhotoAlbum

    var sortBy: SortBy = SortBy.DATE_MODIFIED
    var sortOrder: SortOrder = SortOrder.DESC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_grid)
        setSupportActionBar(image_grid_toolbar)

        val sglm = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recycleViewerForImages.layoutManager = sglm
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

        this.onConfigurationChanged(this.resources.configuration)
        loadPicturesFromAlbum()

        Log.i("Activity", "onCreate exit")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.image_grid_layout_menu, menu)
        return true
    }


    private fun selectAll() {
        for (holder in holderImages)
            holder.setAsSelected()
        for (photo in album.photos)
            photo.selected = true
        //both are needed, as there could be pictures selected that are in a holder that has been removed due to scrolling

        selected = album.photos.size
        toolbarCheckBox.text = selected.toString()
    }

    private fun unselectAll() {
        for (holder in holderImages)
            holder.setAsUnselected()
        for (photo in album.photos)
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
        if (selected == album.photos.size)
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
        for (photo in album.photos)
            photo.selected = false
        selected = 0
        subtitleTextView.text = (album.photos.size.toString() + " images")
    }

    private fun loadPicturesFromAlbum() {
        Log.i("Activity", "loadPicturesFromAlbum entered")
        selected = 0
        for (picture in album.photos)
            if (picture.selected)
                selected++
        if (selected > 0)
            enableSelectionMode()
        else {
            disableSelectionMode()
        }

        imageGridAdapter = ImageGridAdapter(this, album.photos)
        //I MUST NOT REPLACE album.photos with a new arrayList. Instead, clear and add in the old one
        imageGridAdapter.setClickListener(this)
        recycleViewerForImages.adapter = imageGridAdapter
    }

    private fun reloadPicturesFromAlbum() {
        val result = getNewPhotoArrayForAlbum(this)
        if (result.first) {
            album.photos.clear()
            album.photos.addAll(result.second)
            imageGridAdapter.notifyDataSetChanged()

            selected = 0
            for (picture in album.photos)
                if (picture.selected)
                    selected++
            if (selected > 0)
                enableSelectionMode()
            else {
                disableSelectionMode()
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

//        val grid: StaggeredGridLayoutManager = recycleViewerForImages.layoutManager as StaggeredGridLayoutManager
//
//        val firstVisibleItemPositions = IntArray(100)
//        val aux: IntArray = grid.findFirstVisibleItemPositions(firstVisibleItemPositions)
//
//        Log.i("GRID", "offset: $aux, | into: $firstVisibleItemPositions")

//        val v: View? = recycleViewerForImages.layoutManager?.getChildAt(1)
//        var offset=0
//        if(v!=null)
//            offset= v.top

//        Log.i("GRID","offset: $offset")

        when (newConfig.orientation) {
            ORIENTATION_PORTRAIT -> recycleViewerForImages.layoutManager =
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            ORIENTATION_LANDSCAPE -> recycleViewerForImages.layoutManager =
                StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
            else -> { // Note the block
                Log.w(
                    "Orientation",
                    "Orientation in ImageGridKotlinActivity was undefined at configuration change"
                )
            }
        }
    }

    private fun startFullscreenActivity(imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder) {
        Log.i("Files", "Image to open: ${imageColorViewHolder.myPhoto}")
        val intentFullScreenImage = Intent(this, FullscreenImageActivity::class.java)

        Box.Add(intentFullScreenImage, FULLSCREEN_IMAGE_ARRAY, this.album.photos)
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
        if (imageColorViewHolder.myPhoto.selected)
            selected--
        else selected++
        toolbarCheckBox.text = selected.toString()
        toolbarCheckBox.isChecked = (selected == album.photos.size)

        imageColorViewHolder.reverseSelection()
    }

    /**
     * "view" helps determine which element within the "colorViewHolder" was clicked
     */
    override fun onItemClick(
        view: View,
        position: Int,
        imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder
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
        imageColorViewHolder: ImageGridAdapter.ImageColorViewHolder
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

        val nameRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.nameRadioButton)
        val dateCreatedRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.dateCreatedRadioButton)
        val dateModifiedRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.dateModifiedRadioButton)
        val descendingRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.descendingRadioButton)
        val ascendingRadioButton: RadioButton = customAlertDialogView.findViewById(R.id.ascendingRadioButton)

        when (sortBy) {
            SortBy.NAME -> nameRadioButton.isChecked = true
            SortBy.DATE_CREATED -> dateCreatedRadioButton.isChecked = true
            SortBy.DATE_MODIFIED -> dateModifiedRadioButton.isChecked = true
        }

        when (sortOrder)
        {
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

                    when (radioGroupSortBy.indexOfChild(radioButton)){
                        0->sortBy=SortBy.DATE_CREATED
                        1->sortBy=SortBy.DATE_MODIFIED
                        2->sortBy=SortBy.NAME
                    }

                    checkId = radioGroupSortOrder.checkedRadioButtonId
                    radioButton = radioGroupSortOrder.findViewById(checkId)

                    when (radioGroupSortOrder.indexOfChild(radioButton)){
                        0->sortOrder=SortOrder.ASC
                        1->sortOrder=SortOrder.DESC
                    }
                }
                .show()
    }

    fun gridMenuButtonClicked(item: MenuItem) {
        Log.i("Buttons", "clicked gridMenuButtonClicked")
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