package ru.skillbox.humblr.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.skillbox.humblr.R

class MBottomSheetBehavior<V : View> : BottomSheetBehavior<V> {
    constructor(context: Context, atrSet: AttributeSet) : super(context, atrSet)
    constructor(context: Context) : super(context, null)

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (target.id == R.id.m_bar || target.id == R.id.to_json)
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }
}