package ru.skillbox.humblr.utils

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ru.skillbox.humblr.R
import ru.skillbox.humblr.databinding.LceeRecycleViewBinding
import ru.skillbox.humblr.databinding.RecyclerEmptyLayoutBinding
import ru.skillbox.humblr.databinding.RecyclerErrorLayoutBinding
import ru.skillbox.humblr.databinding.RecyclerLoadingLayoutBinding

class LCEERecyclerView constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private  var binding: LceeRecycleViewBinding
    private  var errorBinding: RecyclerErrorLayoutBinding
    private  var emptyBinding: RecyclerEmptyLayoutBinding
    private  var loadingBinding: RecyclerLoadingLayoutBinding
    init {
    val flag=context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)
        binding = LceeRecycleViewBinding.inflate(LayoutInflater.from(context), this)
        errorBinding = binding.customErrorView
        emptyBinding = binding.customEmptyView
        loadingBinding = binding.customOverlayView
        when(flag){
            Configuration.UI_MODE_NIGHT_YES->{
                binding.customEmptyView.emptyImage.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.empty_night))
                binding.customErrorView.errorImage.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.wrong_night))
            }
            Configuration.UI_MODE_NIGHT_NO->{
                binding.customEmptyView.emptyImage.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.empty))
                binding.customErrorView.errorImage.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.wrong))
            }
        }
    }




    val recyclerView: RecyclerView
        get() = binding.customRecyclerView

    var errorText: String = ""
        set(value) {
            field = value
            errorBinding.errorMsgText.text = value
        }

    var emptyText: String = ""
        set(value) {
            field = value
            emptyBinding.emptyMessage.text = value
        }

    @DrawableRes
    var errorIcon = 0
        set(value) {
            field = value
            errorBinding.errorImage.setImageResource(value)
        }

    @DrawableRes
    var emptyIcon = 0
        set(value) {
            field = value
            emptyBinding.emptyImage.setImageResource(value)
        }

    init {

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LCEERecyclerView,
            0,
            0
        ).apply {
            try {
                errorText =
                    getString(R.styleable.LCEERecyclerView_errorText) ?: "Something went wrong"
                emptyText =
                    getString(R.styleable.LCEERecyclerView_emptyText) ?: "Nothing to show"
                errorIcon = getResourceId(
                    R.styleable.LCEERecyclerView_errorIcon,
                    R.drawable.wrong
                )
                emptyIcon =
                    getResourceId(R.styleable.LCEERecyclerView_emptyIcon, R.drawable.empty)
            } finally {
                recycle()
            }
        }
    }

    fun showEmptyView(msg: String? = null) {
        emptyText = msg ?: emptyText
        recyclerView.visibility = View.INVISIBLE
        loadingBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
        emptyBinding.root.visibility = View.VISIBLE
    }

    fun showErrorView(msg: String? = null) {
        errorText = msg ?: errorText
        recyclerView.visibility = View.INVISIBLE
        loadingBinding.root.visibility = View.GONE
        emptyBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.VISIBLE
    }

    fun showLoadingView() {
        recyclerView.visibility = View.INVISIBLE
        emptyBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
        loadingBinding.root.visibility = View.VISIBLE
    }

    fun hideAllViews() {
        recyclerView.visibility = View.VISIBLE
        loadingBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
        emptyBinding.root.visibility = View.GONE
    }

    fun setOnRetryClickListener(callback: () -> Unit) {
        errorBinding.retryButton.setOnClickListener {
            callback()
        }
    }
}