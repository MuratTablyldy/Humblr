package ru.skillbox.humblr.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.appcompat.content.res.AppCompatResources
import ru.skillbox.humblr.R

class MImageView : androidx.appcompat.widget.AppCompatImageView {
    lateinit var volumeOff: Drawable
    lateinit var volumeOn: Drawable

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    fun init(context: Context) {

        volumeOff = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_volume_off_24)!!
        volumeOn = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_volume_up_24)!!
        setImageDrawable(volumeOn)
    }

    fun isOff(): Boolean {
        return when (drawable) {
            volumeOff -> true
            else -> false
        }
    }

    fun switch() {
        when (drawable) {
            volumeOff -> setImageDrawable(volumeOn)
            volumeOn -> setImageDrawable(volumeOff)
        }
    }
}