package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionSet
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import kotlinx.coroutines.InternalCoroutinesApi
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.databinding.WithOutLinkItemBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.news.NewsFragmentDirections
import ru.skillbox.humblr.news.RecycleFragment
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.dp
import ru.skillbox.humblr.utils.richLink.RichLinkView
import java.util.concurrent.atomic.AtomicBoolean

class WithLinkAdapter(private val listener: MListener, fragment: Fragment) :
    AbsListItemAdapterDelegate<Link.LinkOut, Link, MViewHolder.WithLinkRedditViewHolder>() {

    private var viewHolderListener: ViewHolderListener? =
        ViewHolderListenerImpl(fragment)
    private val isRecycleFrag = fragment is RecycleFragment


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
        holder.binding?.card?.setOnClickListener {
            if (isRecycleFrag) {
                viewHolderListener?.onItemClicked(
                    it,
                    holder.adapterPosition,
                    item.permalink,
                    item.url
                )
            } else {
                listener.onClick(it, item)
            }
        }
        holder.binding!!.pagerView.setLink(item.url, listener.getScope()) {
            viewHolderListener?.onLoadCompleted(holder.adapterPosition)
        }
        holder.binding?.item = item
        holder.binding!!.listener = listener
        holder.binding!!.pagerView.transitionName = item.url
        val params = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(20.dp, 10.dp, 20.dp, 20.dp)
        holder.itemView.layoutParams = params
    }

    private interface ViewHolderListener {
        fun onLoadCompleted(adapterPosition: Int)
        fun onItemClicked(view: View, adapterPosition: Int, link: String, url: String)
    }

    private class ViewHolderListenerImpl constructor(private val fragment: Fragment) :
        ViewHolderListener {
        private val enterTransitionStarted: AtomicBoolean = AtomicBoolean()

        @OptIn(InternalCoroutinesApi::class)
        override fun onLoadCompleted(adapterPosition: Int) {
            if (MainActivity.currentPosition != adapterPosition) {
                return
            }
            if (enterTransitionStarted.getAndSet(true)) {
                return
            }
            fragment.startPostponedEnterTransition()
        }

        @OptIn(InternalCoroutinesApi::class)
        override fun onItemClicked(
            view: View,
            adapterPosition: Int,
            link: String,
            url: String
        ) {
            MainActivity.currentPosition = adapterPosition
            (fragment.exitTransition as TransitionSet?)?.excludeTarget(view, true)
            val transitioningView = view.findViewById<RichLinkView>(R.id.pager_view)
            /*transitioningView.imageView?.transitionName = "transitionImage"
            transitioningView.textViewTitle?.transitionName =
                transitioningView.textViewTitle?.text.toString()
            transitioningView.textViewDesp?.transitionName =
                transitioningView.textViewDesp?.text.toString()
            transitioningView.textViewUrl?.transitionName =
                transitioningView.textViewUrl?.text.toString()*/

            if (transitioningView.textViewTitle == null) {
                val direction = NewsFragmentDirections.actionNewsFragmentToDetailLinkFragment(
                    url = url,
                    link = link
                )
                fragment.findNavController().navigate(direction)
            } else {
                val extras =
                    FragmentNavigatorExtras(
                        transitioningView to "rich_link",
                        transitioningView.textViewTitle!! to "rich_link_title",
                        transitioningView.textViewDesp!! to "rich_link_desp",
                        transitioningView.textViewUrl!! to "rich_link_url",
                        transitioningView.imageView!! to "transitionImage"
                    )
                val direction = NewsFragmentDirections.actionNewsFragmentToDetailLinkFragment(
                    url = url,
                    link = link
                )
                fragment.findNavController().navigate(direction, extras)
            }

        }
    }

}