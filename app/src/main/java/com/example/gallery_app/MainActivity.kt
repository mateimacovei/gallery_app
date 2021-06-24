package com.example.gallery_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gallery_app.activities.AlbumGridActivity


const val IMAGE_GRID_MESSAGE = "com.example.gallery_app.IMAGEGRID"
const val FULLSCREEN_IMAGE_ARRAY = "com.example.gallery_app.FULLSCREENIMAGEARRAY"
const val FULLSCREEN_IMAGE_POSITION = "com.example.gallery_app.FULLSCREENIMAGEPOSITION"
const val IMAGE_DETAILS = "com.example.gallery_app.IMAGEDETAILS"

class MainActivity : AppCompatActivity() {
    private val PERMS_RETURN: Int = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = ""

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    openAlbumActivity()
                } else {
                    Toast.makeText(this,"Permissions not granted",Toast.LENGTH_LONG).show()
                    finish()
                }
            }



        requestPermissions29()
        // TO DO : request StorageMedia refresh
//        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
//
//        startActivity(
//            Intent(
//                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
//                uri
//            )
//        )
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


    private fun requestPermissions29() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        )
//                    I need write permission for delete
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMS_RETURN
            )
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