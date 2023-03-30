package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.databinding.SubredditLayoutBinding
import ru.skillbox.humblr.utils.MControllerView

class WithoutPictAdapter(val listener: MListener) :
    AbsListItemAdapterDelegate<Link.LinkText, Link, MViewHolder.NewsWithoutPictViewHolder>() {
    override fun isForViewType(
        item: Link,
        items: MutableList<Link>,
        position: Int
    ): Boolean = item is Link.LinkText

    override fun onCreateViewHolder(parent: ViewGroup): MViewHolder.NewsWithoutPictViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SubredditLayoutBinding.inflate(inflater, parent, false)
        binding.listener = listener
        return MViewHolder.NewsWithoutPictViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        item: Link.LinkText,
        holder: MViewHolder.NewsWithoutPictViewHolder,
        payloads: MutableList<Any>
    ) {

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

        holder.binding?.item = item
        holder.binding?.userName?.setOnClickListener {
            if (item.author != null) {
                listener.navigateToUser(item.author!!)
            }
        }
        holder.binding!!.root.setOnClickListener {
            listener.onText(it, item.permalink)
        }
        holder.binding!!.commentButton.setOnClickListener {
            listener.onText(it, item.getLink())
        }

    }
}