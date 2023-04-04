package ru.skillbox.humblr.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout


class FlingBehavior : AppBarLayout.Behavior {
    private var isPositive = false

    constructor() {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout, target: View, velocityX: Float,
        velocityY: Float, consumed: Boolean
    ): Boolean {
        var velocityYM = velocityY
        var consumedM = consumed
        if (velocityYM > 0 && !isPositive || velocityYM < 0 && isPositive) {
            velocityYM *= -1
        }
        if (target is RecyclerView && velocityYM < 0) {
            val recyclerView = target
            val firstChild: View = recyclerView.getChildAt(0)
            val childAdapterPosition = recyclerView.getChildAdapterPosition(firstChild)
            consumedM = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD
        }
        return super
            .onNestedFling(coordinatorLayout, child, target, velocityX, velocityYM, consumedM)
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        isPositive = dy > 0
    }

    companion object {
        private const val TOP_CHILD_FLING_THRESHOLD = 3
    }
}