package ru.skillbox.humblr.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import ru.skillbox.humblr.utils.adapters.MViewHolder


class MRecycleView : RecyclerView {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        val dividerItemDecoration = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        addItemDecoration(dividerItemDecoration)
    }
    override fun findViewHolderForAdapterPosition(position: Int): ViewHolder? {
        return super.findViewHolderForAdapterPosition(position)
    }
}