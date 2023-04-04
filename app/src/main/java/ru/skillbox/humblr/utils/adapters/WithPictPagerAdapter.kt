package ru.skillbox.humblr.utils.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ru.skillbox.humblr.R
import ru.skillbox.humblr.databinding.PageViewFragmentBinding

class WithPictPagerAdapter(
    val onClick: (view: View, position: Int) -> Unit,
    val onImageLoaded: (ImageView, position: Int) -> Unit
) :
    RecyclerView.Adapter<WithPictPagerAdapter.PictViewHolder>() {
    private var pictList: List<String> = emptyList()

    constructor(
        pictList: List<String>,
        onClick: (view: View, position: Int) -> Unit,
        onImageLoaded: (ImageView, position: Int) -> Unit
    ) : this(onClick, onImageLoaded) {
        this.pictList = pictList
    }

    fun setList(list: List<String>) {
        pictList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PageViewFragmentBinding.inflate(inflater)
        binding.root.layoutParams = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return PictViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: PictViewHolder, position: Int) {
        val url = pictList[position].replace("amp;", "")
        Glide.with(holder.binding!!.imageView).load(url).placeholder(R.drawable.holder)
            .optionalCenterCrop().diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onImageLoaded.invoke(holder.binding!!.imageView, holder.adapterPosition)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onImageLoaded.invoke(holder.binding!!.imageView, holder.adapterPosition)
                    return false
                }

            })
            .into(holder.binding!!.imageView)

        holder.binding!!.imageView.setOnClickListener {
            onClick(it, holder.adapterPosition)
        }
        holder.binding?.imageView?.transitionName = url
    }

    override fun getItemCount(): Int {
        return pictList.size
    }

    class PictViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: PageViewFragmentBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
        }
    }

    fun setList(list: ArrayList<String>) {
        pictList = list
    }
}