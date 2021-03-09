package com.example.gallery_app.activities

import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import androidx.appcompat.app.AppCompatActivity
import com.example.gallery_app.IMAGE_DETAILS
import com.example.gallery_app.R
import com.example.gallery_app.storageAccess.Box
import com.example.gallery_app.storageAccess.MyPhoto
import kotlinx.android.synthetic.main.activity_image_detail.*


class ImageDetailActivity : AppCompatActivity() {
    lateinit var photo: MyPhoto
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        this.title="Details"

        photo= Box.Get(intent, IMAGE_DETAILS)
        Box.Remove(intent)

        textViewDate.text= photo.DATE_MODIFIED

        textViewTitle.text=photo.name

        textViewPath.text=photo.albumFullPath

        (photo.SIZE+" Kb").also { textViewSize.text = it }
        //TO DO : make switch case for kb,mb; also use B, not b

        (photo.WIDTH+"x"+photo.HEIGHT).also { textViewResolution.text = it }
    }
}