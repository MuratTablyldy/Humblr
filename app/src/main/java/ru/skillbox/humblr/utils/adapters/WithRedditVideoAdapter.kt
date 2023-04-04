package ru.skillbox.humblr.utils.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import kohii.v1.core.Playback
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.databinding.SubredditLayoutRedditVideoBinding
import ru.skillbox.humblr.utils.MControllerView

class WithRedditVideoAdapter(val listener: MListener) :
    AbsListItemAdapterDelegate<Link.LinkRedditVideo, Link, MViewHolder.NewsWithRedditVideoHolder>() {

    override fun isForViewType(
        item: Link,
        items: MutableList<Link>,
        position: Int
    ): Boolean {
        return item is Link.LinkRedditVideo
    }

    override fun onCreateViewHolder(parent: ViewGroup): MViewHolder.NewsWithRedditVideoHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SubredditLayoutRedditVideoBinding.inflate(inflater, parent, false)
        binding.listener = listener
        return MViewHolder.NewsWithRedditVideoHolder(binding.root)
    }

    override fun onBindViewHolder(
        item: Link.LinkRedditVideo,
        holder: MViewHolder.NewsWithRedditVideoHolder,
        payloads: MutableList<Any>
    ) {
        holder.videoData = item
        if (listener.isMuted() != holder.binding?.volumeOff!!.isOff()) {
            holder.binding?.volumeOff!!.switch()
        }
        holder.binding?.volumeOff?.setOnClickListener {
            listener.onMute(holder.binding?.volumeOff!!)
        }
        holder.binding?.userName?.setOnClickListener {
            if (item.author != null) {
                listener.navigateToUser(item.author!!)
            }
        }
        holder.binding?.redditName?.text = item.subreddit
        holder.binding?.userName?.text = item.author
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

        val url = item.preview?.images?.last()?.source?.url?.replace("amp;", "")
        Glide.with(holder.binding!!.thumbnail).load(url)
            .into(holder.binding!!.thumbnail)
        if (listener.shouldRebindVideo(holder.rebinder)) {
            listener.getKohii().setUp(Uri.parse(item.mediaEmbed!!.reddit_video!!.dash_url)) {
                tag = holder.videoTag!!
                repeatMode = Player.REPEAT_MODE_ALL
                artworkHintListener = holder
                controller = object : Playback.Controller {
                    override fun kohiiCanPause() = true
                    override fun kohiiCanStart() = true
                    override fun setupRenderer(playback: Playback, renderer: Any?) {
                        super.setupRenderer(playback, renderer)
                        (renderer as View).setOnClickListener(holder)
                    }

                }
            }.bind(holder.binding!!.playerView)
        }
    }

    fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return if (holder is MViewHolder.NewsWithRedditVideoHolder) {
            holder.clearTransientStates()
            return true
        } else {
            super.onFailedToRecycleView(holder)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is MViewHolder.NewsWithRedditVideoHolder)
            holder.onAttached()
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is MViewHolder.NewsWithRedditVideoHolder)
            holder.onDetached()
        super.onViewDetachedFromWindow(holder)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is MViewHolder.NewsWithRedditVideoHolder) {
            holder.binding?.let { listener.getKohii().cancel(it.playerView) }
            holder.onRecycled()
        }
    }

}