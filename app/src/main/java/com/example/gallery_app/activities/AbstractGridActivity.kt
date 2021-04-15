package com.example.gallery_app.activities

import androidx.appcompat.app.AppCompatActivity
import com.example.gallery_app.adapter.clickListenerInterfaces.ImageItemClickListener

const val VELOCITY_HIDE_SHOW_TOOLBAR_THRESHOLD: Long = 150

abstract class AbstractGridActivity : AppCompatActivity(),
    ImageItemClickListener {
}