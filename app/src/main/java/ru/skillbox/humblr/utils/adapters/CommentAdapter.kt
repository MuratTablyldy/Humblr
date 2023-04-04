package ru.skillbox.humblr.utils.adapters

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.text.HtmlCompat
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Comment
import ru.skillbox.humblr.data.entities.Thing2
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.databinding.CommentViewBinding
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.dp

class CommentAdapter(
    private val scope: CoroutineScope,
    private val commentHandler: CommentHandler
) :
    AbsListItemAdapterDelegate<Comment, Created, CommentParentViewHolder.CommentViewHolderViewHolder>() {

    private fun addComment(
        scope: CoroutineScope,
        linearLayoutCompat: LinearLayoutCompat,
        comment: Comment,
        depth: Int,
        parentView: View?,
    ) {
        AsyncLayoutInflater(linearLayoutCompat.context).inflate(
            R.layout.comment_view_reply,
            linearLayoutCompat
        ) { root, _, _ ->
            bind(root, comment)
            val columnHolder = root.findViewById<LinearLayoutCompat>(R.id.column_holder)
            if (depth > 1) {
                for (i in 1 until depth) {
                    val view = View(root.context)
                    view.setBackgroundColor(root.context.getColor(R.color.grey2))
                    columnHolder.addView(view)
                    val params = view.layoutParams as LinearLayoutCompat.LayoutParams
                    params.leftMargin = 17.dp
                    view.layoutParams.width = 3.dp
                }
            }
            val buttone = root.findViewById<FloatingActionButton>(R.id.comment_button)
            buttone.setOnClickListener {
                commentHandler.writeComment(root, linearLayoutCompat, comment, false, depth)
            }
            val index = linearLayoutCompat.children.indexOf(parentView)
            linearLayoutCompat.addView(root, index + 1)
            if (!comment.replies.data.children.isNullOrEmpty()) {
                val comments = comment.replies.data.children
                val loc = depth + 1
                comments?.forEach { it ->
                    addComment(
                        scope,
                        linearLayoutCompat,
                        it.data as Comment,
                        loc,
                        root
                    )
                }
            }
        }
    }

    fun bind(root: View, comment: Comment) {
        scope.launch {
            var icon = comment.account?.profileImg?.replace("amp;", "")
            if (icon == null) {
                icon = "https://www.redditstatic.com/avatars/defaults/v2/avatar_default_3.png"
            }
            Glide.with(root).load(icon)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).override(150, 150)
                .into(root.findViewById(R.id.avatar))
            root.findViewById<TextView>(R.id.nickname).text = comment.author
            val saveButton = root.findViewById<MControllerView>(R.id.save)
            saveButton.onClickListener {
                commentHandler.save(saveButton, comment.name!!)
            }
            val messageView = root.findViewById<TextView>(R.id.text)
            messageView.text =
                comment.body?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
            messageView.movementMethod = LinkMovementMethod.getInstance()
            val instant = if (comment.edited != null) comment.edited.let {
                Instant.ofEpochSecond(it)
            } else {
                comment.createdUTC?.let { Instant.ofEpochSecond(it) }
            }
            val res = messageView.context.resources
            val prefix =
                if (comment.edited != null) res.getString(R.string.edited) else {
                    ""
                }
            val now = Instant.now()
            val duration = Duration.between(instant, now)
            val timeView = root.findViewById<TextView>(R.id.time)
            when {
                duration.toDays() > 0 -> {
                    timeView.text =
                        String.format(
                            res.getString(R.string.days_ago),
                            prefix,
                            duration.toDays()
                        )
                }
                duration.toHours() > 0 -> {
                    timeView.text =
                        String.format(
                            res.getString(R.string.hours_ago),
                            prefix,
                            duration.toHours()
                        )
                }
                duration.toMinutes() > 0 -> {
                    timeView.text =
                        String.format(
                            res.getString(R.string.minutes_ago),
                            prefix,
                            duration.toMinutes()
                        )
                }
                else -> {
                    timeView.text = String.format(res.getString(R.string.now), prefix)
                }
            }
            val voteNumber = root.findViewById<TickerView>(R.id.vote_number)
            voteNumber.setCharacterLists(TickerUtils.provideNumberList())
            val score = comment.score
            when {
                score == null -> {
                    voteNumber.text = "0"
                }
                score > 1000000 -> {
                    voteNumber.text =
                        "${(comment.score!!.toFloat() / 1000).toString().substring(0..3)}m"
                }
                score > 1000 -> {
                    val texts = (comment.score!!.toFloat() / 1000).toString()
                    if (texts.length > 4) {
                        voteNumber.text = "${texts.substring(0..4)}k"
                    } else {
                        voteNumber.text = "${texts}k"
                    }
                }
                else -> {
                    voteNumber.text = comment.score.toString()
                }
            }
            val upvoteB = root.findViewById<MControllerView>(R.id.up_vote)
            val downVoteB = root.findViewById<MControllerView>(R.id.down_vote)

            upvoteB.onClickListener {
                scope.launch {
                    commentHandler.vote(1, "t1_${comment.id}", upvoteB, voteNumber)
                }
            }
            downVoteB.onClickListener {
                scope.launch {
                    commentHandler.vote(-1, "t1_${comment.id}", downVoteB, voteNumber)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): CommentParentViewHolder.CommentViewHolderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CommentViewBinding.inflate(inflater, parent, false)
        binding.voteNumber.setCharacterLists(TickerUtils.provideNumberList())
        return CommentParentViewHolder.CommentViewHolderViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        comment: Comment,
        holder: CommentParentViewHolder.CommentViewHolderViewHolder,
        payloads: MutableList<Any>
    ) {
        holder.binding?.replyView?.removeAllViews()
        bind(holder.binding!!.root, comment)
        val button = holder.binding!!.commentButton
        button.setOnClickListener {
            commentHandler.writeComment(
                holder.binding!!.root,
                holder.binding!!.replyView,
                comment,
                true,
                0
            )
        }
        if (comment.replies.data.children?.isNotEmpty() == true) {
            val comments = comment.replies.data.children
            val firstView = LayoutInflater.from(holder.itemView.context).inflate(
                R.layout.comment_view_reply,
                holder.binding!!.replyView,
                true
            )
            val buttone = firstView.findViewById<FloatingActionButton>(R.id.comment_button)
            buttone.setOnClickListener {
                commentHandler.writeComment(
                    firstView,
                    holder.binding!!.replyView,
                    comment,
                    false,
                    1
                )
            }
            bind(firstView, comments!!.first().data as Comment)
            val buttonComment = holder.binding!!.commentButton
            buttonComment.setOnClickListener {
                commentHandler.writeComment(
                    holder.binding!!.root,
                    holder.binding!!.replyView,
                    comment,
                    true,
                    0
                )
            }
            if (comments.size > 1) {
                val buttonMore = (LayoutInflater.from(holder.itemView.context).inflate(
                    R.layout.more_button,
                    holder.binding!!.replyView,
                    true
                ) as LinearLayoutCompat).findViewById<Button>(R.id.show_more)

                val num = comments.let { getNumComments(it) - 1 }
                buttonMore.text = String.format(
                    holder.itemView.context.resources.getString(R.string.comments_count),
                    num
                )
                buttonMore.setOnClickListener {
                    holder.binding?.replyView?.removeAllViews()
                    comments.forEach {
                        val com = it.data as Comment
                        addComment(scope, holder.binding!!.replyView, com, 1, null)
                    }
                }
            }

        }
    }

    private fun getNumComments(list: List<Thing2>): Int {
        var count = 0
        for (index in list.indices) {
            count++
            val com = list[index]
            val comm = com.data as Comment
            val children = comm.replies.data.children
            if (!children.isNullOrEmpty()) {
                count += getNumComments(children)
            }

        }
        return count
    }

    override fun isForViewType(item: Created, items: MutableList<Created>, position: Int): Boolean =
        item is Comment

    interface CommentHandler {
        fun getPage(index: Int, preview: Boolean)
        suspend fun vote(num: Int, name: String, view: MControllerView, tickerView: TickerView)
        fun ready()
        fun writeComment(view: View, parent: ViewGroup, comment: Comment, root: Boolean, depth: Int)
        fun save(view: MControllerView, commentId: String)
    }

}
