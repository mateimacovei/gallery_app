package com.example.gallery_app.adapter.clickListenerInterfaces;

import android.view.View;

import com.example.gallery_app.adapter.ImageGridAdapter;

public interface ImageItemClickListener {
    void onItemClick(View view, int position, ImageGridAdapter.ColorViewHolder colorViewHolder);
    void onLongItemClick(View view, int position, ImageGridAdapter.ColorViewHolder colorViewHolder);
}
