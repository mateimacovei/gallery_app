package com.example.gallery_app.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gallery_app.MT_FULLSCREEN_IMAGE_MESSAGE
import com.example.gallery_app.R
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto
import kotlinx.android.synthetic.main.activity_my_fullscreen_picture.*

class MyFullscreenPictureActivity : AppCompatActivity() {
    private var isFullscreen: Boolean = false
    lateinit var picture: MyPhoto
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_fullscreen_picture)
        imageView=imageViewFullscreen

        picture = Box.Get(intent, MT_FULLSCREEN_IMAGE_MESSAGE)
        Box.Remove(intent)

        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .error(R.mipmap.ic_launcher_round)

        Glide.with(this)
            .load(picture.uri)
            .apply(options)
            .fitCenter()
            .into(imageView)
//        supportActionBar?.hide()
//        toggleFullscreen()
    }

    fun enterFullscreen(){
        this.isFullscreen=true
        supportActionBar?.hide()
        imageView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    fun exitFullscreen(){
        this.isFullscreen=false
        supportActionBar?.show()
        imageView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    fun toggleFullscreen(){
        if(isFullscreen)
            exitFullscreen()
        else enterFullscreen()
    }

    fun fullScreenImageClicked(view: View) {
        toggleFullscreen()
    }

}