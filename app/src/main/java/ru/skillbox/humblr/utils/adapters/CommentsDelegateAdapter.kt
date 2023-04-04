package ru.skillbox.humblr.utils.adapters

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager
import kotlinx.coroutines.CoroutineScope
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Comment
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.utils.Com

class CommentsDelegateAdapter(
    val scope: CoroutineScope,
    val commentHandler: CommentAdapter.CommentHandler
) :
    RecyclerView.Adapter<CommentParentViewHolder>() {

    private val differ = AsyncListDiffer(this, CommentDiffUtil())
    private val delegate = AdapterDelegatesManager<List<Created>>()

    init {
        delegate.addDelegate(CommentAdapter(scope, commentHandler))
            .addDelegate(NullCommentAdapter(commentHandler))
    }

    fun initPreview() {
        getPage(1, true)
    }

    fun initFirst() {
        getPage(1, false)
        Com.Companion.NullComment.setCurrentPage(1)
    }

    fun getPage(index: Int, preview: Boolean) {
        commentHandler.getPage(index, preview)
    }

    fun setPage(list: List<Created>, preview: Boolean) {
        if (preview) {
            differ.submitList(list)
        } else {
            differ.submitList(list + Com.Companion.NullComment)
        }

    }

    fun setPagesCount(count: Int) {
        Com.Companion.NullComment.setPagesCount(count)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentParentViewHolder {
        return delegate.onCreateViewHolder(parent, viewType) as CommentParentViewHolder
    }

    override fun onBindViewHolder(holder: CommentParentViewHolder, position: Int) {
        delegate.onBindViewHolder(differ.currentList, position, holder)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return delegate.getItemViewType(differ.currentList, position)
    }

}

fun signView(textView: TextView, sign: Boolean) {
    if (sign) {
        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.primaryColor))
    } else {
        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.primaryTextColor))
    }
}

class CommentDiffUtil : DiffUtil.ItemCallback<Created>() {
    override fun areItemsTheSame(oldItem: Created, newItem: Created): Boolean {
        return oldItem.javaClass == newItem.javaClass
    }

    override fun areContentsTheSame(oldItem: Created, newItem: Created): Boolean {
        return oldItem.equals(newItem)
    }
}