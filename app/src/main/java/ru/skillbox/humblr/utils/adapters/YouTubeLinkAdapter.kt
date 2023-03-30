package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.databinding.SubredditLayoutYoutubeBinding
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.utils.MControllerView

class YouTubeLinkAdapter(val listener: MListener) :
    AbsListItemAdapterDelegate<Link.LinkYouTube, Link, MViewHolder.YoutubeViewHolder>() {


    override fun isForViewType(
        item: Link,
        items: MutableList<Link>,
        position: Int
    ): Boolean {
        return item is Link.LinkYouTube
    }

    override fun onCreateViewHolder(parent: ViewGroup): MViewHolder.YoutubeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SubredditLayoutYoutubeBinding.inflate(inflater, parent, false)
        binding.listener = listener
        return MViewHolder.YoutubeViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        item: Link.LinkYouTube,
        holder: MViewHolder.YoutubeViewHolder,
        payloads: MutableList<Any>
    ) {
        holder.binding?.item = item
        holder.binding?.userName?.setOnClickListener {
            if (item.author != null) {
                listener.navigateToUser(item.author!!)
            }
        }
        holder.binding?.join?.onClickListener {
            item.subInfo?.name?.let { it1 ->
                listener.onJoin(
                    holder.binding!!.join,
                    it1,
                    holder.binding!!.textView
                )
            }
        }
        if (item.subInfo != null) {
            if (item.subInfo!!.userIsSubscriber == true) {
                holder.binding!!.textView.setColor(
                    holder.itemView.context.resources.getColor(
                        R.color.selected,
                        null
                    )
                )
                holder.binding?.join?.changeState(MControllerView.State.SELECTED)
            }
        }
        val view = holder.binding!!.youtubePlayer
        val url = "https://img.youtube.com/vi/${item.youtubeId}/0.jpg"
        holder.binding!!.tumbtail.visibility = View.VISIBLE
        Glide.with(view).load(url).into(holder.binding!!.tumbtail)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is MViewHolder.YoutubeViewHolder) {
            listener.removeSubscription(holder.absoluteAdapterPosition)
            holder.onDetached()
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is MViewHolder.YoutubeViewHolder) {
            listener.removeSubscription(holder.absoluteAdapterPosition)
            holder.onDetached()
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is MViewHolder.YoutubeViewHolder) {
            listener.subscribe(holder)
        }

    }

}