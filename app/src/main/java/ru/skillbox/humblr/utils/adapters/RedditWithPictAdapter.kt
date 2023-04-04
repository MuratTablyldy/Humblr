package ru.skillbox.humblr.utils.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionSet
import androidx.viewpager2.widget.ViewPager2
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import kotlinx.coroutines.InternalCoroutinesApi
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.databinding.SubredditPictLayoutViewBinding
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.ZoomTransformer
import ru.skillbox.humblr.utils.dp
import java.util.concurrent.atomic.AtomicBoolean
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.news.RecycleFragment

class RedditWithPictAdapter(private val listener: MListener, fragment: Fragment) :
    AbsListItemAdapterDelegate<Link.LinkPict, Link, MViewHolder.WithPictRedditViewHolder>() {

    private var viewHolderListener: ViewHolderListener? = ViewHolderListenerImpl(fragment)
    private val isRecycleFrag = fragment is RecycleFragment
    var positionM = 0


    override fun isForViewType(item: Link, items: MutableList<Link>, position: Int): Boolean {
        return item is Link.LinkPict
    }

    override fun onCreateViewHolder(parent: ViewGroup): MViewHolder.WithPictRedditViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SubredditPictLayoutViewBinding.inflate(inflater)
        binding.root.setPadding(40.dp, 20.dp, 40.dp, 20.dp)
        return MViewHolder.WithPictRedditViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        item: Link.LinkPict,
        holder: MViewHolder.WithPictRedditViewHolder,
        payloads: MutableList<Any>
    ) {
        val pages = item.getImages()
        val adapter = WithPictPagerAdapter(pages, { view, position ->
            if (isRecycleFrag) {
                viewHolderListener?.onItemClicked(
                    view,
                    adapterPosition = position,
                    item.permalink,
                    pages
                )
            } else {
                listener.onPict(view, item.permalink)
            }

        }) { image, position ->
            positionM = position
            viewHolderListener?.onLoadCompleted(image, position)
        }
        holder.binding?.item = item
        holder.binding?.userName?.setOnClickListener {
            if (item.author != null) {
                listener.navigateToUser(item.author!!)
            }
        }
        holder.binding?.pagerView?.adapter = adapter
        holder.binding!!.commentButton.setOnClickListener {
            listener.onPict(it, item.permalink)
        }
        holder.binding!!.commentButton.text = item.numComments
        if (pages.size > 1) {
            holder.binding!!.dotsIndicator.visibility = View.VISIBLE
            holder.binding!!.counter.visibility = View.VISIBLE
            holder.binding?.dotsIndicator?.setViewPager2(holder.binding!!.pagerView)
            val res = holder.binding!!.pagerView.context.resources
            holder.binding!!.pagerView.setPageTransformer(ZoomTransformer())
            holder.binding!!.counter.text =
                String.format(res.getString(R.string.page_count), 1, adapter.itemCount)
            holder.binding!!.pagerView.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    holder.binding!!.counter.text = String.format(
                        res.getString(ru.skillbox.humblr.R.string.page_count),
                        position + 1,
                        adapter.itemCount
                    )
                }
            })
        } else {
            holder.binding!!.dotsIndicator.visibility = View.GONE
            holder.binding!!.counter.visibility = View.GONE
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
        val params = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(20.dp, 10.dp, 20.dp, 20.dp)
        holder.itemView.layoutParams = params
    }

    private interface ViewHolderListener {
        fun onLoadCompleted(view: ImageView, adapterPosition: Int)
        fun onItemClicked(view: View, adapterPosition: Int, link: String, pictures: List<String>)
    }

    private class ViewHolderListenerImpl constructor(private val fragment: Fragment) :
        ViewHolderListener {
        private val enterTransitionStarted: AtomicBoolean = AtomicBoolean()

        @OptIn(InternalCoroutinesApi::class)
        override fun onLoadCompleted(view: ImageView, adapterPosition: Int) {
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
            pictures: List<String>
        ) {
            MainActivity.currentPosition = adapterPosition
            (fragment.exitTransition as TransitionSet?)?.excludeTarget(view, true)
            val transitioningView = view.findViewById<ImageView>(R.id.imageView)
            val bundle = Bundle()
            bundle.putString("link", link)
            bundle.putStringArray("images", pictures.toTypedArray())
            val extras =
                FragmentNavigatorExtras(transitioningView to transitioningView.transitionName)
            fragment.findNavController().navigate(
                R.id.action_newsFragment_to_detainFragment,
                args = bundle,
                null,
                extras
            )
        }
    }
}