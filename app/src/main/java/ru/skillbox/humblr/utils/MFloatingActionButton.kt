package ru.skillbox.humblr.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.skillbox.humblr.R

class MFloatingActionButton : FloatingActionButton, OnClickListener {
    var onRelease: Int? = null
    var onSelected: Int? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.MFloatingActionButton)
        onRelease = array.getColor(
            R.styleable.MFloatingActionButton_tint_on_release,
            resources.getColor(R.color.grey2, null)
        )
        onSelected = array.getColor(
            R.styleable.MFloatingActionButton_tint_on_selected,
            resources.getColor(R.color.grey2, null)
        )
        array.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, def: Int) : super(context, attrs, def) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.MFloatingActionButton)
        onRelease = array.getColor(
            R.styleable.MFloatingActionButton_tint_on_release,
            resources.getColor(R.color.grey2, null)
        )
        onSelected = array.getColor(
            R.styleable.MFloatingActionButton_tint_on_selected,
            resources.getColor(R.color.grey2, null)
        )
        array.recycle()
    }

    var state: State = State.RELEASED

    constructor(context: Context) : super(context)

    private var onClick: OnClick? = null

    init {
        this.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        if (onSelected != null && onRelease != null) {
            state = if (state == State.RELEASED) State.CLICKED else State.RELEASED
            val draw = drawable
            val wrappedDrawable = DrawableCompat.wrap(draw)
            if (state == State.RELEASED) {
                DrawableCompat.setTint(wrappedDrawable, onRelease!!)
                setImageDrawable(wrappedDrawable)
            } else {
                DrawableCompat.setTint(wrappedDrawable, onSelected!!)
                setImageDrawable(wrappedDrawable)
            }
        }
        onClick?.onClick(state)
    }

    fun setOnClick(onClickListener: OnClick) {
        onClick = onClickListener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onClick = null
    }

    interface OnClick {
        fun onClick(state: State)
    }

    enum class State { RELEASED, CLICKED }

}