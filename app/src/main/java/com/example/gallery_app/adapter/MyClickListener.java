package com.example.gallery_app.adapter;

import android.view.View;

import com.example.gallery_app.adapter.AbstractMediaObjectHolder;
import com.example.gallery_app.adapter.AlbumGridAdapter;

public interface MyClickListener {
    void onItemClick(View view, int position, AbstractMediaObjectHolder colorViewHolder);
    void onLongItemClick(View view, int position, AbstractMediaObjectHolder colorViewHolder);
}
