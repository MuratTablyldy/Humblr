package ru.skillbox.humblr.utils.adapters

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.MListener

class NewsAdapter(listener: MListener, fragment: Fragment) : RecyclerView.Adapter<MViewHolder>() {

    private val differ = AsyncListDiffer(this, DiffUtilCallBack())
    private val delegate = AdapterDelegatesManager<List<Link>>()
    var redditVidAdapter: WithRedditVideoAdapter
    var youtubeAdap: YouTubeLinkAdapter
    val list: List<Link>
        get() = differ.currentList

    init {
        redditVidAdapter = WithRedditVideoAdapter(listener)
        youtubeAdap = YouTubeLinkAdapter(listener)
        delegate.addDelegate(redditVidAdapter)
            .addDelegate(youtubeAdap)
            .addDelegate(WithLinkAdapter(listener, fragment))
            .addDelegate(WithoutPictAdapter(listener))
            .addDelegate(RedditWithPictAdapter(listener, fragment = fragment))
            .addDelegate(LoadingAdapter())
    }


    fun removeLoad() {
        val mList = ArrayList(differ.currentList)
        mList.remove(Link.LoadingLink)
        differ.submitList(mList)

    }

    fun getItemLink(position: Int): String {
        return differ.currentList[position].getLink()
    }

    fun getLink(position: Int): Link {
        return differ.currentList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        return delegate.onCreateViewHolder(parent, viewType) as MViewHolder
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        delegate.onBindViewHolder(differ.currentList, position, holder)
    }

    override fun getItemViewType(position: Int): Int {
        return delegate.getItemViewType(differ.currentList, position)
    }

    override fun getItemCount(): Int = differ.currentList.size

    class DiffUtilCallBack : DiffUtil.ItemCallback<Link>() {
        override fun areItemsTheSame(oldItem: Link, newItem: Link): Boolean =
            oldItem.javaClass == newItem.javaClass


        override fun areContentsTheSame(oldItem: Link, newItem: Link): Boolean =
            oldItem == newItem
    }

    fun setList(list: List<Link>) {
        differ.submitList(list)
    }

    fun addLinks(list: List<Link>) {
        if (list.isEmpty()) {
            val mList = ArrayList(differ.currentList)
            mList.remove(Link.LoadingLink)
            differ.submitList(mList)
            return
        }
        val mList = ArrayList(differ.currentList)
        mList.remove(Link.LoadingLink)
        if (mList.isNotEmpty() && mList.last() == list.last()) {
            differ.submitList(mList)
            return
        }
        mList.addAll(list)
        mList.add(Link.LoadingLink)
        differ.submitList(mList)
    }

    fun addLink(link: Link) {
        val list = differ.currentList + link
        differ.submitList(list)
    }

    fun remove() {
        if (differ.currentList.size > 0) {
            val list = ArrayList(differ.currentList)
            list.remove(Link.LoadingLink)
            differ.submitList(list)
        }
    }

    override fun onViewRecycled(holder: MViewHolder) {
        super.onViewRecycled(holder)
        delegate.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: MViewHolder) {
        super.onViewDetachedFromWindow(holder)
        delegate.onViewDetachedFromWindow(holder)
    }

    override fun onViewAttachedToWindow(holder: MViewHolder) {
        super.onViewAttachedToWindow(holder)
        delegate.onViewAttachedToWindow(holder)
    }
}