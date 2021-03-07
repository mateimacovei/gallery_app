package com.example.test_01.adapter.clickListenerInterfaces;

import android.view.View;

import com.example.test_01.adapter.AlbumGridAdapter;

public interface AlbumItemClickListener {
    void onItemClick(View view, int position, AlbumGridAdapter.ColorViewHolder colorViewHolder);
    void onLongItemClick(View view, int position, AlbumGridAdapter.ColorViewHolder colorViewHolder);
}
