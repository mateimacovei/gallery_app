package com.example.test_01.adapter.clickListenerInterfaces;

import android.view.View;

import com.example.test_01.adapter.ImageGridAdapter;

public interface ImageItemClickListener {
    void onItemClick(View view, int position, ImageGridAdapter.ColorViewHolder colorViewHolder);
    void onLongItemClick(View view, int position, ImageGridAdapter.ColorViewHolder colorViewHolder);
}
