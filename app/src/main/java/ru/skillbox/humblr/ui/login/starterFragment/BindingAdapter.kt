package ru.skillbox.humblr.ui.login.starterFragment

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.BindingAdapter

class BindingAdapter {

    companion object {
        @JvmStatic
        @BindingAdapter("app:getImageFromRes")
        fun getImageFromRes(view: ImageView, @DrawableRes res: Int) {
            val image = AppCompatResources.getDrawable(view.context, res)
            view.setImageDrawable(image)
        }

        @JvmStatic
        @BindingAdapter("app:getStringFromRes")
        fun getStringFromRes(view: TextView, @StringRes res: Int) {
            view.setText(res)
        }
    }
}