package com.example.gallery_app.adapter.clickListenerInterfaces;

import android.view.View;

import com.example.gallery_app.adapter.ImageGridAdapter;

public interface ImageItemClickListener {
    void onItemClick(View view, int position, ImageGridAdapter.ImageColorViewHolder imageColorViewHolder);
    void onLongItemClick(View view, int position, ImageGridAdapter.ImageColorViewHolder imageColorViewHolder);
}
