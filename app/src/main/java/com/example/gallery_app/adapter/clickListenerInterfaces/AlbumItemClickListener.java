package com.example.gallery_app.adapter.clickListenerInterfaces;

import android.view.View;

import com.example.gallery_app.adapter.AlbumGridAdapter;

public interface AlbumItemClickListener {
    void onItemClick(View view, int position, AlbumGridAdapter.ColorViewHolder colorViewHolder);
    void onLongItemClick(View view, int position, AlbumGridAdapter.ColorViewHolder colorViewHolder);
}
