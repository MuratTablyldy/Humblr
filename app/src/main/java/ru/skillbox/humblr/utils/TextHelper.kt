package ru.skillbox.humblr.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import ru.skillbox.humblr.R

class TextHelper:androidx.appcompat.widget.AppCompatImageButton, View.OnClickListener {
    constructor(context: Context,attributeSet: AttributeSet):super(context,attributeSet)
    constructor(context: Context):super(context)
    lateinit var mlistener:(MControllerView.State)->Unit
    private var state:MControllerView.State=MControllerView.State.RELEASED
    fun setonClick(click:(MControllerView.State)->Unit){
        mlistener=click
        setOnClickListener(this)
    }

    init{
        DrawableCompat.setTint(this.getDrawable(), ContextCompat.getColor(context, R.color.grey))
    }

    override fun onClick(p0: View?) {
        state = if(state==MControllerView.State.RELEASED){
            DrawableCompat.setTint(this.getDrawable(), ContextCompat.getColor(context, R.color.primaryColor))
            MControllerView.State.SELECTED
        } else {
            DrawableCompat.setTint(this.getDrawable(), ContextCompat.getColor(context,
                R.color.grey2
            ))
            MControllerView.State.RELEASED
        }
        mlistener.invoke(state)
    }


}