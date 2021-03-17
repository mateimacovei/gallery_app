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
import com.example.gallery_app.adapter.clickListenerInterfaces.ImageItemClickListener
import com.example.gallery_app.storageAccess.MyPhoto
import kotlinx.android.synthetic.main.item_image_in_grid.view.*
import java.util.*

class ImageGridAdapter(private val context: ImageGridActivity, private val images: ArrayList<MyPhoto>) :
        RecyclerView.Adapter<ImageGridAdapter.ImageColorViewHolder>() {
    var mClickListener: ImageItemClickListener? = null

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
        holderImage.myPhoto = images[position]
        holderImage.photoPositionInMyArray = position

        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .error(R.mipmap.ic_launcher_round)

        Glide.with(context)
            .load(holderImage.myPhoto.uri)
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
                    if (holderImage.myPhoto.selected) {
                        resource?.setTint(Color.GRAY)
                        resource?.setTintBlendMode(BlendMode.MODULATE)
                    }
//                    holder.updatePictureBySelection()
//                    holder.imageView.drawable.setTint(Color.GREEN)
                    return false
                }

            })
            .into(holderImage.imageView)

        if (context.selectionMode) {
            holderImage.checkBox.isChecked = holderImage.myPhoto.selected
        } else {
            holderImage.checkBox.visibility = View.GONE
            holderImage.imageButtonFullscreen.visibility = View.GONE
        }

//        Picasso.get()
//            .load(holder.photo.path)
//            .resize(250, 250)
//            .centerCrop()
//            .into(holder.imageView)
//        holder.imageView.setOnClickListener {
//            //handle click event on image
//        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ImageColorViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener,
        View.OnLongClickListener {
        val imageView: ImageView = view.imageViewPicture
        val checkBox: CheckBox = view.checkBoxImage
        val imageButtonFullscreen: ImageButton = view.imageButtonFullscreen
        lateinit var myPhoto: MyPhoto
        var photoPositionInMyArray: Int = 0

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
            imageView.setOnClickListener(this)
            imageView.setOnLongClickListener(this)
            checkBox.setOnClickListener(this)
            checkBox.setOnLongClickListener(this)
            imageButtonFullscreen.setOnClickListener(this)
            imageButtonFullscreen.setOnLongClickListener(this)

            context.holderImages.add(this)
//            imageView.post { this.updatePictureBySelection() }
//            imageView.drawable.
//            imageView.setOnLongClickListener(
//
//            })

        }


        override fun onClick(view: View?) {
            //important to check ImageButton first, as ImageButton extends ImageView
            when (view) {
                is ImageButton -> Log.i("Files", "short clicked ImageButton")
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
            //important to check ImageButton first, as ImageButton extends ImageView
            when (view) {
                is ImageButton -> Log.i("Files", "long clicked ImageButton")
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
            if (myPhoto.selected)
                setAsUnselected()
            else setAsSelected()
        }

        /**
         * Unselects the photo and removes the checkbox and the imageButton
         */
        fun disableSelectionMode() {
            setAsUnselected()
            checkBox.visibility = View.GONE
            imageButtonFullscreen.visibility = View.GONE
        }

        /**
         * Makes the checkbox and the imageButton visible.
         * At this point, all pictures should be unselected; setAsSelected() will be called from now on
         */
        fun enableSelectionMode() {
            checkBox.visibility = View.VISIBLE
            imageButtonFullscreen.visibility = View.VISIBLE
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
            myPhoto.selected = true
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
            myPhoto.selected = false
        }
    }

    // convenience method for getting data at click position
//    fun getItem(id: Int): Photo {
//        return images[id]
//    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ImageItemClickListener) {
        this.mClickListener = itemClickListener
    }
}