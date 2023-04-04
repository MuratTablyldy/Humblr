package ru.skillbox.humblr.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import ru.skillbox.humblr.R

class MButton : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrSet,
        defStyleAttr
    )

    constructor(context: Context, attrSet: AttributeSet?) : super(context, attrSet)

    private var off: Drawable? = null
    private var on: Drawable? = null
    var isOff: Boolean = true
        set(value) {
            field = value
            if (field) {
                setImageDrawable(off)
            } else {
                setImageDrawable(on)
            }
            field = value
        }

    init {
        off = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_volume_off_24)
        on = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_volume_up_24)
        setImageDrawable(on)
        setBackgroundColor(context.getColor(R.color.opaque))
    }
}