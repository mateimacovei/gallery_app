package com.example.gallery_app.adapter

//import com.example.gallery_app.storageAccess.Photo
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.gallery_app.R
import com.example.gallery_app.activities.ImageGridActivity
import com.example.gallery_app.storageAccess.MyMediaObject
import com.example.gallery_app.storageAccess.PreferencesFileHandler
import com.example.gallery_app.storageAccess.shouldShowFullscreenIcon
import kotlinx.android.synthetic.main.item_image_in_grid.view.*
import java.util.*

class ImageGridAdapter(private val context: ImageGridActivity, private val images: ArrayList<MyMediaObject>) :
        RecyclerView.Adapter<ImageGridAdapter.ImageColorViewHolder>() {
    var mClickListener: MyClickListener? = null

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): ImageGridAdapter.ImageColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_in_grid, parent, false)
        return this.ImageColorViewHolder(view)
    }

    override fun onBindViewHolder(holderImage: ImageGridAdapter.ImageColorViewHolder, position: Int) {
//        Log.i("Files", "onbindViewHolder called")

        //THIS IS THE ONLY PLACE WHERE I HAVE BOTH THE ITEM VIEW AND THE POSITION OF THE ITEM IN THE ORIGINAL DATA
        holderImage.myMediaObject = images[position]
        holderImage.photoPositionInMyArray = position

        val options: RequestOptions = RequestOptions()
                .centerCrop()
                .error(R.mipmap.ic_launcher_round)

        val listener = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                    p0: GlideException?,
                    p1: Any?,
                    p2: Target<Drawable>?,
                    p3: Boolean
            ): Boolean {
                Log.i("Files", "load failed")
                return false
            }

            override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
            ): Boolean {
                if (holderImage.myMediaObject.selected) {
                    //TO DO: Tint not working on gifs
                    resource?.setTint(Color.GRAY)
                    resource?.setTintBlendMode(BlendMode.MODULATE)
                }
//                    holder.updatePictureBySelection()
//                    holder.imageView.drawable.setTint(Color.GREEN)
                return false
            }

        }

        Glide.with(context)
                    .load(holderImage.myMediaObject.uri)
                    .apply(options)
                    .listener(listener)
                    .into(holderImage.imageView)

        if (context.selectionMode) {
            holderImage.checkBox.isChecked = holderImage.myMediaObject.selected
            if(!shouldShowFullscreenIcon(PreferencesFileHandler.getGridSize(context)))
                holderImage.imageButtonFullscreen.visibility = View.GONE
        } else {
            holderImage.checkBox.visibility = View.GONE
            holderImage.imageButtonFullscreen.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ImageColorViewHolder(view: View) : AbstractMediaObjectHolder(view) {
        override val imageView: ImageView = view.imageViewPicture
        override val checkBox: CheckBox = view.checkBoxImage
        val imageButtonFullscreen: ImageButton = view.imageButtonFullscreen
        lateinit var myMediaObject: MyMediaObject
        var photoPositionInMyArray: Int = 0

        init {
            imageView.setOnClickListener(this)
            imageView.setOnLongClickListener(this)
            checkBox.setOnClickListener(this)
            checkBox.setOnLongClickListener(this)
            imageButtonFullscreen.setOnClickListener(this)
            imageButtonFullscreen.setOnLongClickListener(this)

            context.holderImages.add(this)
        }

        override fun isSelected(): Boolean{
            return myMediaObject.selected
        }

        override fun onClick(v: View?) {
            super.logClickedView(v)
            mClickListener?.onItemClick(
                    v,
                    adapterPosition,
                    this
            )
        }

        override fun onLongClick(v: View?): Boolean {
            super.logClickedView(v)
            mClickListener?.onLongItemClick(
                    v,
                    adapterPosition,
                    this
            )
            return true
        }

        /**
         * Unselects the photo and removes the checkbox and the imageButton
         */
        override fun disableSelectionMode() {
            setAsUnselected()
            checkBox.visibility = View.GONE
            imageButtonFullscreen.visibility = View.GONE
        }

        /**
         * Makes the checkbox and the imageButton visible.
         * At this point, all pictures should be unselected; setAsSelected() will be called from now on
         */
        override fun enableSelectionMode() {
            checkBox.visibility = View.VISIBLE
            if (shouldShowFullscreenIcon(PreferencesFileHandler.getGridSize(context)))
                imageButtonFullscreen.visibility = View.VISIBLE
        }

        /**
         * enableSelectionMode() should have been previously called
         * Checks the checkbox
         * adds tint
         * sets phote.selected = true
         */
        override fun setAsSelected() {
            checkBox.isChecked = true
            imageView.drawable.setTint(Color.GRAY)
            imageView.drawable.setTintBlendMode(BlendMode.MODULATE)
            myMediaObject.selected = true
        }

        /**
         * enableSelectionMode() should have been previously called
         * Unchecks the checkbox
         * removes the tint
         * sets phote.selected = false
         */
        override fun setAsUnselected() {
            checkBox.isChecked = false
            imageView.drawable?.setTintList(null)
            myMediaObject.selected = false
        }
    }

    // convenience method for getting data at click position
//    fun getItem(id: Int): Photo {
//        return images[id]
//    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: MyClickListener) {
        this.mClickListener = itemClickListener
    }
}