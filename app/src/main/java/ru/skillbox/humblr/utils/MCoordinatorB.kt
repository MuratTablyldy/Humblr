package ru.skillbox.humblr.utils

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView

class MCoordinatorB:CoordinatorLayout {
    constructor(context: Context):super(context,null)
    constructor(context: Context,attributeSet: AttributeSet):super(context,attributeSet)
    var point=Point()
    var point2=Point()
    private var recyclerView:RecyclerView?=null
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return when(ev?.action){
            MotionEvent.ACTION_MOVE->{
                point2.y=ev.y.toInt()
                val dist=point.y-point2.y
                if(dist>0){
                    val adapter=recyclerView?.adapter
                    if(adapter!=null){
                        val layoutManager = recyclerView?.layoutManager
                        val rect= Rect()
                        val rvRect= Rect()
                        if(layoutManager!=null){
                            layoutManager.findViewByPosition(adapter.itemCount-1)?.getGlobalVisibleRect(rect)
                            recyclerView?.getGlobalVisibleRect(rvRect)
                            Log.d("${ev.action}",(rect.bottom<rvRect.bottom).toString())
                            return rect.bottom<rvRect.bottom
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }else{
                    false
                }
            }
            MotionEvent.ACTION_DOWN->{
                point.y=ev.y.toInt()
                false
            }

            else -> {
                Log.d("${ev?.action}","${MotionEvent.ACTION_SCROLL}")
                false
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {

        return super.onTouchEvent(ev)
    }


    fun setRecycleView(recyclerView: RecyclerView){
        this.recyclerView=recyclerView
    }
}