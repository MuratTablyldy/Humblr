package ru.skillbox.humblr.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.databinding.EmptyViewBinding
import ru.skillbox.humblr.utils.Com
import ru.skillbox.humblr.utils.dp

class NullCommentAdapter(val commentHandler: CommentAdapter.CommentHandler) :
    AbsListItemAdapterDelegate<Com.Companion.NullComment, Created, CommentParentViewHolder.EmptyEndView>() {
    var binding: EmptyViewBinding? = null
    private var lastChosenPage = -1
    private var leftChosenPage = -1
    private var pagesCountS = 0
    override fun isForViewType(item: Created, items: MutableList<Created>, position: Int): Boolean {
        return item is Com.Companion.NullComment
    }

    override fun onCreateViewHolder(parent: ViewGroup): CommentParentViewHolder.EmptyEndView {
        val inflater = LayoutInflater.from(parent.context)
        binding = EmptyViewBinding.inflate(inflater!!, parent, false)
        return CommentParentViewHolder.EmptyEndView(binding!!.root)
    }

    override fun onBindViewHolder(
        item: Com.Companion.NullComment,
        holder: CommentParentViewHolder.EmptyEndView,
        payloads: MutableList<Any>
    ) {
        val count = item.getPageCount()
        pagesCountS = item.getPageCount()
        if (holder.binding?.root?.children?.count() == 0 && item.pages.isEmpty()) {
            if (count > 3) {
                for (i in 1..3) {
                    if (i == 1) {
                        consume(
                            i.toString(),
                            null,
                            holder.itemView.context,
                            item.getPageCount(),
                            holder.binding!!.root,
                            true
                        )
                    } else {
                        consume(
                            i.toString(),
                            null,
                            holder.itemView.context,
                            item.getPageCount(),
                            holder.binding!!.root,
                            false
                        )
                    }
                }
                consume(
                    ">",
                    null,
                    holder.itemView.context,
                    item.getPageCount(),
                    holder.binding!!.root,
                    false
                )
            } else {
                for (i in 1..count) {
                    consume(
                        i.toString(),
                        null,
                        holder.itemView.context,
                        item.getPageCount(),
                        holder.binding!!.root,
                        false
                    )
                }
            }
            item.previousPage = 1
        } else if (holder.binding?.root?.children?.count() == 0) {
            //holder.binding?.root?.removeAllViews()
            for (i in 0..item.pages.lastIndex) {
                val pg = item.pages[i]
                if (!pg.contains("[<>]".toRegex())) {
                    val index = pg.toInt()
                    if (index == item.getCurrent()) {
                        consume(
                            item.pages[i],
                            null,
                            holder.itemView.context,
                            item.getPageCount(),
                            holder.binding!!.root,
                            true
                        )
                    } else {
                        consume(
                            item.pages[i],
                            null,
                            holder.itemView.context,
                            item.getPageCount(),
                            holder.binding!!.root,
                            false
                        )
                    }
                } else {
                    consume(
                        item.pages[i],
                        null,
                        holder.itemView.context,
                        item.getPageCount(),
                        holder.binding!!.root,
                        false
                    )
                }

                item.pages[i]
            }
        }
        val pages = mutableListOf<String>()
        val iterator = holder.binding!!.root.children.iterator()
        while (iterator.hasNext()) {
            val textV = iterator.next() as TextView
            pages.add(textV.text.toString())

        }
        item.pages = pages
    }


    private fun consume(
        page: String,
        index: Int?,
        context: Context,
        pagesCount: Int,
        linearLayout: LinearLayoutCompat,
        sign: Boolean
    ) {
        val textview = TextView(context)
        if (pagesCount == 1) {
            signView(textview, true)
        }
        textview.setOnClickListener {
            val text = (it as TextView).text
            onClick(text, linearLayout)
        }
        textview.text = page
        if (index == null) {
            linearLayout.addView(textview)
        } else {
            linearLayout.addView(textview, index)
        }
        (textview.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
            10.dp,
            10.dp,
            10.dp,
            10.dp
        )
        if (sign)
            signView(textview, true)

    }

    fun onClick(text: CharSequence, linearLayout: LinearLayoutCompat) {
        when (text) {
            ">" -> {
                val lastIndex = linearLayout.children.count() - 1
                var indexS = 0
                val iterator = linearLayout.children.iterator()
                var penult = 0
                var addFirst = false
                val pages= mutableListOf<String>()
                while (indexS <= lastIndex) {
                    val view = iterator.next() as TextView
                    when (indexS) {
                        0 -> {
                            val value = view.text
                            if (value != "<") {
                                addFirst = true
                                val num = value.toString().toInt() + 1
                                view.text = num.toString()
                            }
                        }
                        lastIndex - 1 -> {
                            penult = view.text.toString().toInt()
                            penult++
                            view.text = (penult).toString()
                            lastChosenPage = penult
                        }
                        lastIndex -> {
                            if (penult == pagesCountS) {
                                linearLayout.removeView(view)

                            }
                            if (addFirst) {
                                consume(
                                    "<",
                                    0,
                                    linearLayout.context,
                                    pagesCountS,
                                    linearLayout,
                                    false
                                )
                            }
                        }
                        else -> {
                            val value = view.text.toString().toInt() + 1
                            view.text = value.toString()
                        }
                    }
                    indexS++
                }
                val iter=linearLayout.children.iterator()
                while (iter.hasNext()){
                    val valy=iter.next() as TextView
                    pages.add(valy.text.toString())
                }
                Com.Companion.NullComment.pages=pages
                Com.Companion.NullComment.previousPage = lastChosenPage
                onClick(lastChosenPage.toString(), linearLayout)
            }
            "<" -> {
                val lastIndex = linearLayout.children.count() - 1
                var index = 0
                val iterator = linearLayout.children.iterator()
                var firstView: TextView? = null
                var removeFirst = false
                val pages= mutableListOf<String>()
                while (index <= lastIndex) {
                    val view = iterator.next() as TextView
                    when (index) {
                        0 -> {
                            firstView = view
                        }
                        1 -> {
                            val value = view.text.toString().toInt() - 1
                            leftChosenPage = value
                            view.text = value.toString()
                            if (value == 1) {
                                removeFirst = true
                            }
                        }
                        lastIndex -> {
                            val value = view.text.toString()
                            if (value != ">") {
                                val num = value.toInt() - 1
                                view.text = num.toString()
                                consume(
                                    ">",
                                    lastIndex + 1,
                                    linearLayout.context,
                                    pagesCountS,
                                    linearLayout,
                                    false
                                )
                            }
                            if (removeFirst) {
                                linearLayout.removeView(firstView)
                            }
                        }
                        else -> {
                            val value = view.text.toString().toInt() - 1
                            view.text = value.toString()
                        }
                    }
                    index++
                }
                val iter=linearLayout.children.iterator()
                while (iter.hasNext()){
                    val valy=iter.next() as TextView
                    pages.add(valy.text.toString())
                }
                Com.Companion.NullComment.pages=pages
                Com.Companion.NullComment.previousPage = leftChosenPage
                onClick(leftChosenPage.toString(), linearLayout)
            }
            else -> {
                val iterator = linearLayout.children.iterator()
                while (iterator.hasNext()) {
                    val view = iterator.next() as TextView
                    if (view.text == text) {
                        signView(view, true)
                    } else {
                        signView(view, false)
                    }
                }
                Com.Companion.NullComment.previousPage = text.toString().toInt()
                commentHandler.getPage(text.toString().toInt(), false)
            }
        }
    }

}