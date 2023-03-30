package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.skillbox.humblr.R
import ru.skillbox.humblr.databinding.PageViewFragmentBinding

class WithPictPagerAdapter(val onClick: () -> Unit) :
    RecyclerView.Adapter<WithPictPagerAdapter.PictViewHolder>() {
    private var pictList: List<String> = emptyList()

    constructor(pictList: List<String>, onClick: () -> Unit) : this(onClick) {
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
        )
            .into(holder.binding!!.imageView)

        holder.binding!!.imageView.setOnClickListener {
            onClick()
        }

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