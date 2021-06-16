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
        val fragment: ImageFragment =
            when (parentActivity.myMediaObjectsArray[position].getExtension()) {
                "gif" -> ZoomImagePageFragment()
                else -> SubsamplingImagePageFragment()
            }
        fragment.mediaObject = parentActivity.myMediaObjectsArray[position]
        fragment.parentActivity = parentActivity
        return fragment
    }
}