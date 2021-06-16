package com.example.gallery_app.uiClasses;

import android.view.View;

public interface MyClickListener {
    void onItemClick(View view, int position, AbstractMediaObjectHolder colorViewHolder);
    void onLongItemClick(View view, int position, AbstractMediaObjectHolder colorViewHolder);
}
