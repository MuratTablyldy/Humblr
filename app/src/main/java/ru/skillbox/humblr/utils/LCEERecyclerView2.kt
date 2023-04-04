package ru.skillbox.humblr.utils


import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.databinding.LceeRecycleView2Binding
import ru.skillbox.humblr.databinding.LceeRecycleViewBinding
import ru.skillbox.humblr.databinding.RecyclerEmptyLayoutBinding
import ru.skillbox.humblr.databinding.RecyclerErrorLayoutBinding
import ru.skillbox.humblr.databinding.RecyclerLoadingLayoutBinding

class LCEERecyclerView2 constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)


    private val binding: LceeRecycleView2Binding =
        LceeRecycleView2Binding.inflate(LayoutInflater.from(context), this)
    private val errorBinding: RecyclerErrorLayoutBinding = binding.customErrorView
    private val emptyBinding: RecyclerEmptyLayoutBinding = binding.customEmptyView
    private val loadingBinding: RecyclerLoadingLayoutBinding = binding.customOverlayView
    private var onLoad: OnLoad? = null


    init {
        binding.idNestedSV.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            v as NestedScrollView
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                binding.idPBLoading.visibility = View.VISIBLE;
                onLoad?.startLoad()
            }
        }
    }

    fun setOnLoad(onLoad: OnLoad) {
        this.onLoad = onLoad
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
        loadingBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
        emptyBinding.root.visibility = View.VISIBLE
    }

    fun showErrorView(msg: String? = null) {
        errorText = msg ?: errorText
        loadingBinding.root.visibility = View.GONE
        emptyBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.VISIBLE
    }

    fun showLoadingView() {
        emptyBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
        loadingBinding.root.visibility = View.VISIBLE
    }

    fun hideAllViews() {
        loadingBinding.root.visibility = View.GONE
        errorBinding.root.visibility = View.GONE
        emptyBinding.root.visibility = View.GONE
    }

    fun setOnRetryClickListener(callback: () -> Unit) {
        errorBinding.retryButton.setOnClickListener {
            callback()
        }
    }

    interface OnLoad {
        fun startLoad(): Boolean
    }
}