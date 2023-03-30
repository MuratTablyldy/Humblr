package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.databinding.WithOutLinkItemBinding
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.dp

class WithLinkAdapter(private val listener: MListener) :
    AbsListItemAdapterDelegate<Link.LinkOut, Link, MViewHolder.WithLinkRedditViewHolder>() {
    override fun isForViewType(item: Link, items: MutableList<Link>, position: Int): Boolean {
        return item is Link.LinkOut
    }

    override fun onCreateViewHolder(parent: ViewGroup): MViewHolder.WithLinkRedditViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = WithOutLinkItemBinding.inflate(inflater)
        return MViewHolder.WithLinkRedditViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        item: Link.LinkOut,
        holder: MViewHolder.WithLinkRedditViewHolder,
        payloads: MutableList<Any>
    ) {
        holder.binding?.join?.onClickListener {
            item.subInfo?.name?.let { it1 ->
                listener.onJoin(
                    holder.binding!!.join,
                    it1,
                    holder.binding!!.title
                )
            }
        }
        if (item.subInfo != null) {
            if (item.subInfo!!.userIsSubscriber == true) {
                holder.binding!!.title.setColor(
                    holder.itemView.context.resources.getColor(
                        R.color.selected,
                        null
                    )
                )
                holder.binding?.join?.changeState(MControllerView.State.SELECTED)
            }
        }

        holder.binding?.redditName?.text = item.subreddit
        holder.binding?.userName?.text = item.author
        holder.binding?.userName?.setOnClickListener {
            if (item.author != null) {
                listener.navigateToUser(item.author!!)
            }
        }
        holder.binding!!.pagerView.setLink(item.url, listener.getScope())
        holder.binding?.item = item
        holder.binding!!.listener = listener

        val params = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(20.dp, 10.dp, 20.dp, 20.dp)
        holder.itemView.layoutParams = params
    }
}