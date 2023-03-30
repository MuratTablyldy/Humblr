package ru.skillbox.humblr.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.text.SpannableString
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import ru.skillbox.humblr.R
import kotlin.math.abs

class MTextView : AppCompatTextView {

    var color: Int?=null
    constructor(context: Context, attr: AttributeSet?) : super(context, attr){
        val array = context.obtainStyledAttributes(attr, R.styleable.MTextView)
        color = array.getColor(R.styleable.MTextView_background_color,R.attr.colorPrimary)
        array.recycle()
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet?, def: Int) : super(context, attr, def){
        val array = context.obtainStyledAttributes(attr, R.styleable.MTextView)
        color = array.getColor(R.styleable.MTextView_background_color,R.attr.colorPrimary)
        array.recycle()
    }

    fun setColor(color:Int){
        this.color=color
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        val startLine = layout.getLineForOffset(0)
        val endLine = layout.getLineForOffset(text.length)
        if (startLine == endLine) {
            //val lineTop = layout.getLineTop(startLine)
            val lineTop=layout.getLineTop(startLine)+(abs(layout.getLineDescent(startLine)))

            val lineBottom = layout.getLineBottom(startLine)

            val startCoor = layout.getPrimaryHorizontal(0).toInt()
            val endCoor=layout.width+startCoor
            val drawable = AppCompatResources.getDrawable(context, R.drawable.background_text)
            if(color!=null){
                val wrappedDrawable = DrawableCompat.wrap(drawable!!)
                DrawableCompat.setTint(wrappedDrawable, color!!)
                wrappedDrawable.setBounds(startCoor, lineTop, endCoor, lineBottom)
                wrappedDrawable.draw(canvas!!)
            }else{
                drawable!!.setBounds(startCoor, lineTop, endCoor, lineBottom)
                drawable.draw(canvas!!)
            }
        } else {
            for (i in startLine..endLine) {
                val start = layout.getLineLeft(i).toInt() + paddingLeft
                val end=layout.width+start
                val top=layout.getLineTop(i)+( abs(layout.getLineDescent(i)))
                val  bottom = layout.getLineBaseline(i)
                var drawable = AppCompatResources.getDrawable(context, R.drawable.background_text)
                if(color!=null){
                    val wrappedDrawable = DrawableCompat.wrap(drawable!!)
                    DrawableCompat.setTint(wrappedDrawable, color!!)
                    wrappedDrawable.setBounds(start,top,end,bottom)
                    wrappedDrawable.draw(canvas!!)
                }else{
                    drawable!!.setBounds(start, top, end, bottom)
                    drawable.draw(canvas!!)
                }

            }

        }

        super.onDraw(canvas)
    }
}