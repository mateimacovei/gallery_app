package com.example.gallery_app.uiClasses

import android.content.ContentUris
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.gallery_app.R
import com.example.gallery_app.activities.AlbumGridActivity
import com.example.gallery_app.storageAccess.domain.MyPhotoAlbum
import kotlinx.android.synthetic.main.item_album_in_grid.view.*

class AlbumGridAdapter(private val context: AlbumGridActivity, val albums: ArrayList<MyPhotoAlbum>) :
    RecyclerView.Adapter<AlbumGridAdapter.ColorViewHolder>() {
    var mClickListener: MyClickListener? = null

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): AlbumGridAdapter.ColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_album_in_grid, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumGridAdapter.ColorViewHolder, position: Int) {
        holder.album = albums[position]

        val rm = Glide.with(context)
        val loader: RequestBuilder<Drawable> = if (holder.album.mediaObjects.size > 0)
            rm.load(holder.album.mediaObjects[0].uri)
        else rm.load(holder.album.uriLong?.let {
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
        })

        //due to glide being smart, it will work for albums taken from room even if the thumbnail picture has been deleted
        loader.transform(MultiTransformation(CenterCrop(), RoundedCorners(20)))
            .listener(object : RequestListener<Drawable> {
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
                    if (holder.album.selected) {
                        resource?.setTint(Color.GRAY)
                        resource?.setTintBlendMode(BlendMode.MODULATE)
                    }
//                    holder.updatePictureBySelection()
//                    holder.imageView.drawable.setTint(Color.GREEN)
                    return false
                }

            })
            .into(holder.imageView)

        if (context.selectionMode) {
            holder.checkBox.isChecked = holder.album.selected
        } else {
            holder.checkBox.visibility = View.GONE
        }

        holder.albumNameTextView.text = holder.album.albumName
        holder.albumCountTextView.text = holder.album.size.toString()
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    inner class ColorViewHolder(view: View) : AbstractMediaObjectHolder(view) {
        override val imageView: ImageView = view.imageViewAlbum
        override val checkBox: CheckBox = view.checkBoxAlbum
        val albumNameTextView: TextView = view.albumNameTextView
        val albumCountTextView: TextView = view.albumItemsCount
        lateinit var album: MyPhotoAlbum

        init {
            imageView.setOnClickListener(this)
            imageView.setOnLongClickListener(this)
            checkBox.setOnClickListener(this)
            checkBox.setOnLongClickListener(this)

            context.holders.add(this)
        }

        override fun isSelected(): Boolean {
            return album.selected
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
        }

        /**
         * Makes the checkbox and the imageButton visible.
         * At this point, all pictures should be unselected; setAsSelected() will be called from now on
         */
        override fun enableSelectionMode() {
            checkBox.visibility = View.VISIBLE
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
            album.selected = true
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
            album.selected = false
        }
    }

    // allows clicks events to be caught
    fun setClickListener(albumItemClickListener: MyClickListener) {
        this.mClickListener = albumItemClickListener
    }
}