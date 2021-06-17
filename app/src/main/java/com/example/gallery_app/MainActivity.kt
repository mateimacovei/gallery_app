package com.example.gallery_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.gallery_app.activities.AlbumGridActivity
import com.example.gallery_app.storageAccess.database.AppDatabase
import com.example.gallery_app.storageAccess.domain.TagToMediaObject
import kotlin.concurrent.thread

const val IMAGE_GRID_MESSAGE = "com.example.gallery_app.IMAGEGRID"
const val FULLSCREEN_IMAGE_ARRAY = "com.example.gallery_app.FULLSCREENIMAGEARRAY"
const val FULLSCREEN_IMAGE_POSITION = "com.example.gallery_app.FULLSCREENIMAGEPOSITION"
const val IMAGE_DETAILS = "com.example.gallery_app.IMAGEDETAILS"

class MainActivity : AppCompatActivity() {
    private val PERMS_RETURN: Int = 12345

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = ""

        requestPermissions()
        // TO DO : request StorageMedia refresh

        thread{
//            val db = Room.databaseBuilder(
//                applicationContext,
//                AppDatabase::class.java, "my-gallery-database"
//            )
//                .fallbackToDestructiveMigration()
//                .build()
//
////            val tagDAO = db.tagDao()
////            tagDAO.deleteAll()
////            tagDAO.insertAll(Tag(name = "test tag 1"), Tag(name = "test tag 2"), Tag(name = "ははは"))
////            val tags = tagDAO.getAll()
////            val firstTag = tagDAO.getByName("test tag 1")
////            val secondTag = tagDAO.getByName("test tag 2")
////            tagDAO.delete(firstTag[0])
////            secondTag[0].rowId?.let { tagDAO.deleteOneById(it) }
////            val tagsAfterDel = tagDAO.getAll()
////            db.close()
////
////            Log.i("Database","first tag: $firstTag")
////            Log.i("Database","second tag: $secondTag")
////            Log.i("Database","all tags")
////            tags.forEach { run { Log.i("Database", "$it") } }
////
////            Log.i("Database","tags after del")
////            tagsAfterDel.forEach { run { Log.i("Database", "$it") } }
//
////            val tagToMediaObjectDAO = db.tagToMediaObjectDAO()
////            tagToMediaObjectDAO.deleteAll()
////            tagToMediaObjectDAO.insertAll(TagToMediaObject(tagId = 1L, mediaObjectId = 1L), TagToMediaObject(tagId = 2L, mediaObjectId = 2L), TagToMediaObject(tagId = 3L, mediaObjectId = 3L))
////            val all = tagToMediaObjectDAO.getAll()
////            val firstTagToMO = tagToMediaObjectDAO.getByTagId(1L)
////            val secondTagToMO = tagToMediaObjectDAO.getByMediaObjectId(2L)
////            tagToMediaObjectDAO.delete(firstTagToMO[0])
////
////            val list = ArrayList<Long>()
////            firstTagToMO[0].rowId?.let { list.add(it) }
////            secondTagToMO[0].rowId?.let { list.add(it) }
////            tagToMediaObjectDAO.deleteFromList(list)
////            val tagToMediaObjectsAfterDel = tagToMediaObjectDAO.getAll()
////            db.close()
////
////            Log.i("Database","first TagToMediaObject: $firstTagToMO")
////            Log.i("Database","second TagToMediaObject: $secondTagToMO")
////            Log.i("Database","all TagToMediaObjects")
////            all.forEach { run { Log.i("Database", "$it") } }
////
////            Log.i("Database","TagToMediaObjects after del")
////            tagToMediaObjectsAfterDel.forEach { run { Log.i("Database", "$it") } }
//
//
////            runOnUiThread {
////                Runnable {
////                    Toast.makeText(
////                        this,
////                        "last tag: ${tags[tags.lastIndex].name}",
////                        Toast.LENGTH_SHORT
////                    )
////                        .show()
////
////                }
////            }
        }

    }

    private fun openAlbumActivity() {
        val intentAlbumGrid = Intent(this, AlbumGridActivity::class.java)
        this.startActivityForResult(intentAlbumGrid, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("Activity", "returned from grid activity")
        this.onBackPressed();
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
//                    I need write permission for delete
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMS_RETURN)
        else
            openAlbumActivity()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //TO DO: ^ any effect?
        if (requestCode == PERMS_RETURN) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Storage permissions denied!", Toast.LENGTH_LONG).show()
                finish()
            } else {
//                Toast.makeText(this, "File permissions granted!", Toast.LENGTH_SHORT).show()
                openAlbumActivity()
            }
        }

    }
}