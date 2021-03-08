package com.example.gallery_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gallery_app.activities.AlbumGridActivity
import com.example.gallery_app.activities.ImageGridActivity
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto
import com.example.gallery_app.storageAccess.StaticMethods

const val IMAGE_GRID_MESSAGE = "com.example.gallery_app.IMAGEGRID"
const val FULLSCREEN_IMAGE_ARRAY = "com.example.gallery_app.FULLSCREENIMAGEARRAY"
const val FULLSCREEN_IMAGE_POSITION = "com.example.gallery_app.FULLSCREENIMAGEPOSITION"

class MainActivity : AppCompatActivity() {
    private val PERMS_RETURN: Int = 12345

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()

        val intentAlbumGrid = Intent(this,AlbumGridActivity::class.java)
        this.startActivity(intentAlbumGrid)
    }


    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMS_RETURN
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMS_RETURN) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "File permissions denied!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "File permissions granted!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun gridViewButtonClicked(view: View) {
        val pictures: ArrayList<MyPhoto> = StaticMethods.getAllPictures(this)

        val intentImageGrid = Intent(this, ImageGridActivity::class.java)
        //these don't work because URI (member in MyPicture class) is abstract, can't be serialized
//            .apply {
//                putExtra(IMAGE_GRID_MESSAGE, pictures)
//            }

        Box.Add(intentImageGrid, IMAGE_GRID_MESSAGE,pictures)


//        Log.i("Files","before putting pictures in intent to ImageGridActivity")
//        intentImageGrid.putExtra(IMAGE_GRID_MESSAGE,pictures[0])
//
//        Log.i("Files","after putting pictures in intent to ImageGridActivity")

        this.startActivity(intentImageGrid)
    }

    fun textMainClicked(view: View) {
        Log.i("Files", "text view in main clicked")
    }

    fun albumGridButtonClicked(view: View) {
        val intentAlbumGrid = Intent(this,AlbumGridActivity::class.java)
        this.startActivity(intentAlbumGrid)
    }

}