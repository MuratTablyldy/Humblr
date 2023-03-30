package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.databinding.ImageItemViewBinding
import ru.skillbox.humblr.databinding.TextItemViewBinding

class SearchAdapter(val onClick: (Link) -> Unit) :
    RecyclerView.Adapter<SearchAdapter.BaseViewHolder>() {

    val differ = AsyncListDiffer(this, CommentDiffUtil())

    fun setList(list: List<Link>) {
        differ.submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        val item = differ.currentList[position]
        return when (item) {
            is Link.LinkRedditVideo -> {
                REDDIT_VIDEO
            }
            is Link.LinkOut -> {
                REDDIT_LINK
            }
            is Link.LinkText -> {
                REDDIT_TEXT
            }
            is Link.LinkPict -> {
                REDDIT_PICT
            }
            is Link.LinkYouTube -> {
                REDDIT_YOU_TUBE
            }
            else -> {
                -1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            REDDIT_VIDEO -> {
                val binding = ImageItemViewBinding.inflate(inflater, parent, false)
                BaseViewHolder.ImageViewHolder(binding.root)
            }
            REDDIT_LINK -> {
                val binding = TextItemViewBinding.inflate(inflater, parent, false)
                BaseViewHolder.TextViewHolder(binding.root)
            }
            REDDIT_PICT -> {
                val binding = ImageItemViewBinding.inflate(inflater, parent, false)
                BaseViewHolder.ImageViewHolder(binding.root)
            }
            REDDIT_TEXT -> {
                val binding = TextItemViewBinding.inflate(inflater, parent, false)
                BaseViewHolder.TextViewHolder(binding.root)
            }
            REDDIT_YOU_TUBE -> {
                val binding = ImageItemViewBinding.inflate(inflater, parent, false)
                BaseViewHolder.ImageViewHolder(binding.root)
            }
            else -> {
                throw IllegalArgumentException("not valid type")
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (val item = differ.currentList[position]) {
            is Link.LinkPict -> {
                holder.itemView.setOnClickListener {
                    onClick.invoke(item)
                }
                val holderM = holder as BaseViewHolder.ImageViewHolder
                val image = item.getImages().first().replace("amp;", "")
                val binding = holderM.binding
                Glide.with(binding!!.root).load(image).into(binding.imageView)
                binding.redditName.text = item.subreddit
                val instant = item.createdUTC.let {
                    Instant.ofEpochSecond(it)
                }
                val res = binding.userName.context.resources
                val prefix =
                    if (item.edited != null) res.getString(R.string.edited) else {
                        ""
                    }
                val now = Instant.now()
                val duration = Duration.between(instant, now)
                when {
                    duration.toDays() > 0 -> {
                        binding.userName.text =
                            String.format(
                                res.getString(R.string.days_ago2),
                                item.author,
                                prefix,
                                duration.toDays()
                            )

                    }
                    duration.toHours() > 0 -> {
                        String.format(
                            res.getString(R.string.hours_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    duration.toMinutes() > 0 -> {
                        String.format(
                            res.getString(R.string.minutes_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    else -> {
                        String.format(
                            res.getString(R.string.now2),
                            item.author,
                            prefix
                        )
                    }
                }
                binding.text.text = item.title
                binding.bottomText.text = String.format(
                    res.getString(
                        R.string.bottom_view_format,
                        item.ups,
                        item.numComments
                    )
                )
            }
            is Link.LinkYouTube -> {
                val holderM = holder as BaseViewHolder.ImageViewHolder
                val url = "https://img.youtube.com/vi/${item.youtubeId}/0.jpg"
                val binding = holderM.binding
                holder.itemView.setOnClickListener {
                    onClick.invoke(item)
                }
                Glide.with(binding!!.root).load(url).into(binding.imageView)
                binding.redditName.text = item.subreddit
                val instant = item.createdUTC.let {
                    Instant.ofEpochSecond(it)
                }
                val res = binding.userName.context.resources
                val prefix =
                    if (item.edited != null) res.getString(R.string.edited) else {
                        ""
                    }
                val now = Instant.now()
                val duration = Duration.between(instant, now)
                when {
                    duration.toDays() > 0 -> {
                        binding.userName.text =
                            String.format(
                                res.getString(R.string.days_ago2),
                                item.author,
                                prefix,
                                duration.toDays()
                            )

                    }
                    duration.toHours() > 0 -> {
                        String.format(
                            res.getString(R.string.hours_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    duration.toMinutes() > 0 -> {
                        String.format(
                            res.getString(R.string.minutes_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    else -> {
                        String.format(
                            res.getString(R.string.now2),
                            item.author,
                            prefix
                        )
                    }
                }
                binding.text.text = item.title
                binding.bottomText.text = String.format(
                    res.getString(
                        R.string.bottom_view_format,
                        item.ups,
                        item.numComments
                    )
                )
            }
            is Link.LinkOut -> {
                val holderM = holder as BaseViewHolder.TextViewHolder
                val binding = holderM.binding
                holder.itemView.setOnClickListener {
                    onClick.invoke(item)
                }
                binding?.redditName?.text = item.subreddit
                val instant = item.createdUTC.let {
                    Instant.ofEpochSecond(it)
                }
                val res = binding!!.userName.context.resources
                val prefix =
                    if (item.edited != null) res.getString(R.string.edited) else {
                        ""
                    }
                val now = Instant.now()
                val duration = Duration.between(instant, now)
                when {
                    duration.toDays() > 0 -> {
                        binding.userName.text =
                            String.format(
                                res.getString(R.string.days_ago2),
                                item.author,
                                prefix,
                                duration.toDays()
                            )

                    }
                    duration.toHours() > 0 -> {
                        String.format(
                            res.getString(R.string.hours_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    duration.toMinutes() > 0 -> {
                        String.format(
                            res.getString(R.string.minutes_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    else -> {
                        String.format(
                            res.getString(R.string.now2),
                            item.author,
                            prefix
                        )
                    }
                }
                binding.text.text = item.title
                binding.bottomText.text = String.format(
                    res.getString(
                        R.string.bottom_view_format,
                        item.ups,
                        item.numComments
                    )
                )
            }
            is Link.LinkRedditVideo -> {
                val holderM = holder as BaseViewHolder.ImageViewHolder
                holder.itemView.setOnClickListener {
                    onClick.invoke(item)
                }
                val url = item.preview?.images?.last()?.source?.url?.replace("amp;", "")
                val binding = holderM.binding
                Glide.with(binding!!.root).load(url).into(binding.imageView)
                binding.redditName.text = item.subreddit
                val instant = item.createdUTC.let {
                    Instant.ofEpochSecond(it)
                }
                val res = binding.userName.context.resources
                val prefix =
                    if (item.edited != null) res.getString(R.string.edited) else {
                        ""
                    }
                val now = Instant.now()
                val duration = Duration.between(instant, now)
                when {
                    duration.toDays() > 0 -> {
                        binding.userName.text =
                            String.format(
                                res.getString(R.string.days_ago2),
                                item.author,
                                prefix,
                                duration.toDays()
                            )

                    }
                    duration.toHours() > 0 -> {
                        String.format(
                            res.getString(R.string.hours_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    duration.toMinutes() > 0 -> {
                        String.format(
                            res.getString(R.string.minutes_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    else -> {
                        String.format(
                            res.getString(R.string.now2),
                            item.author,
                            prefix
                        )
                    }
                }
                binding.text.text = item.title
                binding.bottomText.text = String.format(
                    res.getString(
                        R.string.bottom_view_format,
                        item.ups,
                        item.numComments
                    )
                )
            }
            is Link.LinkText -> {
                val holderM = holder as BaseViewHolder.TextViewHolder
                val binding = holderM.binding
                holder.itemView.setOnClickListener {
                    onClick.invoke(item)
                }
                binding?.redditName?.text = item.subreddit
                val instant = item.createdUTC.let {
                    Instant.ofEpochSecond(it)
                }
                val res = binding!!.userName.context.resources
                val prefix =
                    if (item.edited != null) res.getString(R.string.edited) else {
                        ""
                    }
                val now = Instant.now()
                val duration = Duration.between(instant, now)
                when {
                    duration.toDays() > 0 -> {
                        binding.userName.text =
                            String.format(
                                res.getString(R.string.days_ago2),
                                item.author,
                                prefix,
                                duration.toDays()
                            )

                    }
                    duration.toHours() > 0 -> {
                        String.format(
                            res.getString(R.string.hours_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    duration.toMinutes() > 0 -> {
                        String.format(
                            res.getString(R.string.minutes_ago2),
                            item.author,
                            prefix,
                            duration.toDays()
                        )
                    }
                    else -> {
                        String.format(
                            res.getString(R.string.now2),
                            item.author,
                            prefix
                        )
                    }
                }
                binding.text.text = item.title
                binding.bottomText.text = String.format(
                    res.getString(
                        R.string.bottom_view_format,
                        item.ups,
                        item.numComments
                    )
                )
            }
            else -> {
                throw IllegalArgumentException("not valid type")
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    sealed class BaseViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView) {
        class TextViewHolder(containerView: View) : BaseViewHolder(containerView) {
            var binding: TextItemViewBinding? = null

            init {
                binding = DataBindingUtil.bind(containerView)
            }
        }

        class ImageViewHolder(containerView: View) : BaseViewHolder(containerView) {
            var binding: ImageItemViewBinding? = null

            init {
                binding = DataBindingUtil.bind(containerView)
            }
        }
    }

    class CommentDiffUtil : DiffUtil.ItemCallback<Link>() {

        override fun areItemsTheSame(oldItem: Link, newItem: Link): Boolean {
            return oldItem.javaClass == newItem.javaClass
        }

        override fun areContentsTheSame(oldItem: Link, newItem: Link): Boolean {
            return oldItem.equals(newItem)
        }
    }

    companion object {
        const val REDDIT_VIDEO = 0
        const val REDDIT_TEXT = 1
        const val REDDIT_LINK = 2
        const val REDDIT_YOU_TUBE = 3
        const val REDDIT_PICT = 4
    }
}