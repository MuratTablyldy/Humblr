package ru.skillbox.humblr.ui.login.starterFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import ru.skillbox.humblr.databinding.PageViewBinding
import android.widget.LinearLayout




class PagerAdapter:RecyclerView.Adapter<PagerAdapter.PagerViewHolder>() {
   private val list= mutableListOf<Page>()
    fun addList(list: List<Page>){
        this.list.addAll(list)
    }
    fun addElement(page: Page){
        list.add(page)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=PageViewBinding.inflate(inflater)
        binding.root.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return PagerViewHolder((binding.root))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.binding?.page=list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }
    class PagerViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var binding:PageViewBinding?=null
        init {
            binding= DataBindingUtil.bind(itemView)
        }
    }
}