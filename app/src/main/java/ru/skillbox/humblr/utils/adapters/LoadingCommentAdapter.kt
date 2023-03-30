package ru.skillbox.humblr.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Comment
import ru.skillbox.humblr.data.interfaces.Created

class LoadingCommentAdapter :
    AbsListItemAdapterDelegate<Comment.LoadingComment, Created, MViewHolder.LoadingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup): MViewHolder.LoadingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.loading_view, parent, false)
        return MViewHolder.LoadingViewHolder(view)
    }


    override fun isForViewType(item: Created, items: MutableList<Created>, position: Int): Boolean =
        item is Comment.LoadingComment

    override fun onBindViewHolder(
        item: Comment.LoadingComment,
        holder: MViewHolder.LoadingViewHolder,
        payloads: MutableList<Any>
    ) {
    }
}