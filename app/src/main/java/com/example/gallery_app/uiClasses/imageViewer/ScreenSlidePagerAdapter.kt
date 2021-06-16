package com.example.gallery_app.uiClasses.imageViewer

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gallery_app.activities.FullscreenImageActivity

class ScreenSlidePagerAdapter(private val parentActivity: FullscreenImageActivity) :
    FragmentStateAdapter(parentActivity) {
    override fun getItemCount(): Int {
        return parentActivity.myMediaObjectsArray.size
    }

    override fun createFragment(position: Int): Fragment {
        if (parentActivity.myMediaObjectsArray[position].getExtension().contentEquals("gif")) {
            val fragment = ZoomImagePageFragment()
            fragment.mediaObject = parentActivity.myMediaObjectsArray[position]
            fragment.parentActivity = parentActivity
            return fragment
        } else {
            val fragment = SubsamplingImagePageFragment()
            fragment.mediaObject = parentActivity.myMediaObjectsArray[position]
            fragment.parentActivity = parentActivity
            return fragment
        }
    }
}