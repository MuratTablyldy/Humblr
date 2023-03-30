package ru.skillbox.humblr.utils

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs


class ToolbarBehavior : AppBarLayout.Behavior {
    private var scrollableRecyclerView = false
    private var count = 0

    constructor() {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        ev: MotionEvent
    ): Boolean {
        return scrollableRecyclerView && super.onInterceptTouchEvent(parent, child, ev)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {
        updatedScrollable(directTargetChild)
        return scrollableRecyclerView && super.onStartNestedScroll(
            coordinatorLayout,
            child, directTargetChild, target, nestedScrollAxes, type
        )
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return scrollableRecyclerView && super.onNestedFling(
            coordinatorLayout,
            child, target, velocityX, velocityY, consumed
        )
    }

    private fun updatedScrollable(directTargetChild: View) {
        if (directTargetChild is LCEERecyclerView) {
            val recyclerView: RecyclerView = directTargetChild.recyclerView
            val adapter = recyclerView.adapter
            if (adapter != null) {
                if (adapter.itemCount != count) {
                    scrollableRecyclerView = false
                    count = adapter.itemCount
                    val layoutManager = recyclerView.layoutManager
                    val rect=Rect()
                    val rvRect=Rect()
                    if (layoutManager != null) {

                        if (layoutManager is LinearLayoutManager) {
                            layoutManager.findViewByPosition(adapter.itemCount-1)?.getGlobalVisibleRect(rect)
                            recyclerView.getGlobalVisibleRect(rvRect)
                        }
                        scrollableRecyclerView =rect.bottom>rvRect.bottom
                    }
                }
            }
        } else scrollableRecyclerView = true
    }
}