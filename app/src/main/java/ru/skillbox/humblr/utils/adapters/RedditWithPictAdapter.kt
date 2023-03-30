package ru.skillbox.humblr.utils.adapters

import android.R.attr.left
import android.R.attr.right
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.databinding.SubredditPictLayoutViewBinding
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.dp


class RedditWithPictAdapter(private val listener: MListener) :
    AbsListItemAdapterDelegate<Link.LinkPict, Link, MViewHolder.WithPictRedditViewHolder>() {
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
        val adapter = WithPictPagerAdapter(pages) {
            listener.onPict(holder.binding!!.pagerView, item.permalink)
        }
        holder.binding?.item = item
        holder.binding?.userName?.setOnClickListener {
            if (item.author != null) {
                listener.navigateToUser(item.author!!)
            }
        }
        holder.binding?.pagerView?.adapter = adapter
        holder.binding!!.pagerView.setOnClickListener {
            listener.onText(it, item.getLink())
        }
        holder.binding!!.commentButton.text = item.numComments
        holder.binding!!.commentButton.setOnClickListener {
            listener.onText(it, item.getLink())
        }
        if (pages.size > 1) {

            holder.binding!!.dotsIndicator.visibility = View.VISIBLE
            holder.binding!!.counter.visibility = View.VISIBLE
            holder.binding?.dotsIndicator?.setViewPager2(holder.binding!!.pagerView)
            val res = holder.binding!!.pagerView.context.resources
            holder.binding!!.counter.text =
                String.format(res.getString(R.string.page_count), 1, adapter.itemCount)
            holder.binding!!.pagerView.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    holder.binding!!.counter.text = String.format(
                        res.getString(R.string.page_count),
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

        /*params.setMargins(left, top, right, bottom)
        yourbutton.setLayoutParams(params)*/


        /*holder.binding!!.panelView.findViewById<TickerView>(R.id.vote_number)
            .setCharacterLists(TickerUtils.provideNumberList())
        holder.binding!!.panelView.findViewById<TickerView>(R.id.comment_number)
            .setCharacterLists(TickerUtils.provideNumberList())*/
    }
}