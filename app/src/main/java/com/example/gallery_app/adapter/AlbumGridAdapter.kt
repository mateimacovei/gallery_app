package com.example.gallery_app.adapter

import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.gallery_app.R
import com.example.gallery_app.activities.AlbumGridActivity
import com.example.gallery_app.adapter.clickListenerInterfaces.AlbumItemClickListener
import com.example.gallery_app.storageAccess.MyPhotoAlbum
import kotlinx.android.synthetic.main.item_album_in_grid.view.*

class AlbumGridAdapter(private val context: AlbumGridActivity, private val albums: ArrayList<MyPhotoAlbum>) :
    RecyclerView.Adapter<AlbumGridAdapter.ColorViewHolder>() {
    var mClickListener: AlbumItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlbumGridAdapter.ColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_album_in_grid, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumGridAdapter.ColorViewHolder, position: Int) {
        holder.album = albums[position]
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .error(R.mipmap.ic_launcher_round)

        if (holder.album.mediaObjects.size > 0)
            Glide.with(context)
                .load(holder.album.mediaObjects[0].uri)
                .apply(options)
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
        holder.albumCountTextView.text = holder.album.mediaObjects.size.toString()

//        holder.imageView.setOnClickListener {
//            //handle click event on image
//        }

    }

    override fun getItemCount(): Int {
        return albums.size
    }

    inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener,
        View.OnLongClickListener {
        val imageView: ImageView = view.imageViewAlbum
        val checkBox: CheckBox = view.checkBoxAlbum
        val albumNameTextView: TextView = view.albumNameTextView
        val albumCountTextView: TextView = view.albumItemsCount
        lateinit var album: MyPhotoAlbum

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
            imageView.setOnClickListener(this)
            imageView.setOnLongClickListener(this)
            checkBox.setOnClickListener(this)
            checkBox.setOnLongClickListener(this)

            context.holders.add(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                is ImageView -> Log.i("Files", "short clicked ImageView")
                is CheckBox -> Log.i("Files", "short clicked CheckBox")
                else -> Log.i("Files", "short clicked unidentified")
            }
            mClickListener?.onItemClick(
                view,
                adapterPosition,
                this
            )
        }

        override fun onLongClick(view: View?): Boolean {
            when (view) {
                is ImageView -> Log.i("Files", "long clicked ImageView")
                is CheckBox -> Log.i("Files", "long clicked CheckBox")
                else -> Log.i("Files", "long clicked unidentified")
            }
            mClickListener?.onLongItemClick(
                view,
                adapterPosition,
                this
            )
            return true
        }

        /**
         * enableSelectionMode() should have been previously called.
         * calls setAsSelected() is the picture is unselected, setAsUnselected() otherwise
         */
        fun reverseSelection() {
            if (album.selected)
                setAsUnselected()
            else setAsSelected()
        }

        /**
         * Unselects the photo and removes the checkbox and the imageButton
         */
        fun disableSelectionMode() {
            setAsUnselected()
            checkBox.visibility = View.GONE
        }

        /**
         * Makes the checkbox and the imageButton visible.
         * At this point, all pictures should be unselected; setAsSelected() will be called from now on
         */
        fun enableSelectionMode() {
            checkBox.visibility = View.VISIBLE
        }

        /**
         * enableSelectionMode() should have been previously called
         * Checks the checkbox
         * adds tint
         * sets phote.selected = true
         */
        fun setAsSelected() {
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
        fun setAsUnselected() {
            checkBox.isChecked = false
            imageView.drawable?.setTintList(null)
            album.selected = false
        }
    }

    // allows clicks events to be caught
    fun setClickListener(albumItemClickListener: AlbumItemClickListener) {
        this.mClickListener = albumItemClickListener
    }
}