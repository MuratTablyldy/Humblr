package ru.skillbox.humblr.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.Nullable
import ru.skillbox.humblr.R

class MControllerView: androidx.appcompat.widget.AppCompatImageView,OnClickListener {
    var onRelease: Drawable?=null
    var onSelected: Drawable?=null
    lateinit var anim:Animation
    lateinit var mlistener:(State)->Unit
    enum class State{
        RELEASED,SELECTED
    }
    var state = State.RELEASED


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        val array=context.obtainStyledAttributes(attrs,R.styleable.MControllerView)
        onRelease=array.getDrawable(R.styleable.MControllerView_on_released)
        onSelected=array.getDrawable(R.styleable.MControllerView_on_selected)
        init(context)
        array.recycle()
        setOnClickListener(this)
        anim= AnimationUtils.loadAnimation(context,R.anim.bounce)
    }
    fun init(context: Context){
        setImageDrawable(onRelease)
    }
    fun onClickListener(onClick:(State)->Unit){
        mlistener=onClick
    }
    fun changeState(state: State){
        this.state=state
        if(state==State.RELEASED){
            setImageDrawable(onRelease)
        } else{
            setImageDrawable(onSelected)
        }
    }

    override fun onClick(p0: View?) {
        mlistener.invoke(state)
        state=if(state==State.RELEASED) State.SELECTED else State.RELEASED

        startAnimation(anim)
    }

    override fun onAnimationEnd() {
        super.onAnimationEnd()
        if(state==State.RELEASED){
            setImageDrawable(onRelease)
        } else{
            setImageDrawable(onSelected)
        }
    }

}