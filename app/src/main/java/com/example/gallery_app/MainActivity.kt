package com.example.gallery_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.gallery_app.activities.AlbumGridActivity
import com.example.gallery_app.database.AppDatabase
import com.example.gallery_app.database.domain.Tag
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


//        thread {
//            val db = Room.databaseBuilder(
//                applicationContext,
//                AppDatabase::class.java, "my-gallery-database"
//            ).build()
//
//            val tagDAO = db.tagDao()
////            tagDAO.insertAll(Tag(name = "test tag 1"), Tag(name = "test tag 2"), Tag(name = "ははは"))
//            val tags = tagDAO.getAll()
//            db.close()
//            Log.i("Tags", "Found: ${tags.size}")
//            tags.forEach { run{Log.i("Tags", "${it.name}")} }
//            runOnUiThread {
//                Runnable {
//                    Toast.makeText(
//                        this,
//                        "last tag: ${tags[tags.lastIndex].name}",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//
//                }
//            }
//
//        }
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
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMS_RETURN)
        else
            openAlbumActivity()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
    ) {
        if (requestCode == PERMS_RETURN) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "File permissions denied!", Toast.LENGTH_LONG).show()
                finish()
            } else {
//                Toast.makeText(this, "File permissions granted!", Toast.LENGTH_SHORT).show()
                openAlbumActivity()
            }
        }

    }
}