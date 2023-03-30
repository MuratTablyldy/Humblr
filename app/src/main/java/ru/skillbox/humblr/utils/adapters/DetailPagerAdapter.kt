package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.skillbox.humblr.data.entities.ImageIn
import ru.skillbox.humblr.databinding.PageViewFragmentBinding

class DetailPagerAdapter(private val listener:(String, ImageView)->Unit):RecyclerView.Adapter<DetailPagerAdapter.PictViewHolder>() {
    private var pictList:List<String> = emptyList()

    constructor(pictList:List<String>,listener:(String,ImageView)->Unit) : this(listener) {
        this.pictList =pictList
    }
    fun setList(list:List<String>){
        pictList=list
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictViewHolder {
        val inflater =LayoutInflater.from(parent.context)
        val binding=PageViewFragmentBinding.inflate(inflater)
        binding.root.layoutParams = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return PictViewHolder(binding.root)
    }
    override fun getItemCount(): Int {
        return pictList.size
    }
    override fun onBindViewHolder(holder:PictViewHolder, position: Int) {
        listener.invoke(pictList[position],holder.binding!!.imageView)
    }

    class PictViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var binding:PageViewFragmentBinding?=null
        init{
            binding=DataBindingUtil.bind(itemView)
        }
    }
}