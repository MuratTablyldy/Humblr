package ru.skillbox.humblr.utils

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import ru.skillbox.humblr.R
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView

class BindingAdapter {

    companion object {

        @JvmStatic
        @BindingAdapter("app:set_reddit_from_url")
        fun setDrawableFromUrl(view: ImageView, uri: String) {
                    Glide.with(view).load(uri).into(view)
                        .onLoadFailed(AppCompatResources.getDrawable(view.context, R.drawable.oops))
        }
        @JvmStatic
        @BindingAdapter("android:configure_automatically")
        fun configureTicker(view:TickerView, auto:String){
            if(auto=="true")
            view.setCharacterLists(TickerUtils.provideNumberList())
        }
    }

}
