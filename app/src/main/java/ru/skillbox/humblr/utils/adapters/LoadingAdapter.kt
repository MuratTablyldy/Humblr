package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link

class LoadingAdapter:AbsListItemAdapterDelegate<Link.LoadingLink,Link,MViewHolder.LoadingViewHolder>() {
    override fun isForViewType(item: Link, items: MutableList<Link>, position: Int): Boolean = item is Link.LoadingLink

    override fun onCreateViewHolder(parent: ViewGroup): MViewHolder.LoadingViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val view=inflater.inflate(R.layout.loading_view,parent,false)
        return MViewHolder.LoadingViewHolder(view)
    }

    override fun onBindViewHolder(
        item: Link.LoadingLink,
        holder: MViewHolder.LoadingViewHolder,
        payloads: MutableList<Any>
    ) {

    }

}