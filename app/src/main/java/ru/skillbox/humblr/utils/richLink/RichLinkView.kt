package ru.skillbox.humblr.utils.richLink


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result


open class RichLinkView : RelativeLayout {
    private var view: View? = null
    private var meta: MetaData? = null
    var linearLayout: LinearLayout? = null
    var imageView: ImageView? = null
    var textViewTitle: TextView? = null
    var textViewDesp: TextView? = null
    var textViewUrl: TextView? = null
    private var main_url: String? = null



    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun initView() {
        if (findLinearLayoutChild() != null) {
            view = findLinearLayoutChild()
        } else {
            view = this
            inflate(context, R.layout.link_layout, this)
        }
        linearLayout = findViewById(R.id.rich_link_card)
        imageView = findViewById(R.id.rich_link_image)
        textViewTitle = findViewById(R.id.rich_link_title)
        textViewDesp = findViewById(R.id.rich_link_desp)
        textViewUrl = findViewById(R.id.rich_link_url)
        if (meta?.imageurl.isNullOrBlank()) {
            imageView!!.visibility = GONE
        } else {
            imageView!!.visibility = VISIBLE
            val url=meta?.imageurl?.replace("amp;","")
            Glide.with(context).load(url).into(imageView!!)
        }
        if (meta?.title.isNullOrBlank()) {
            textViewTitle!!.visibility = GONE
        } else {
            textViewTitle!!.visibility = VISIBLE
            textViewTitle!!.text = meta?.title
        }
        if (meta?.url.isNullOrBlank()) {
            textViewUrl?.visibility = GONE
        } else {
            textViewUrl?.visibility = VISIBLE
            main_url = meta?.url
            textViewUrl?.text = meta?.url
        }
        if (meta?.description.isNullOrBlank()) {
            textViewDesp?.visibility = GONE
        } else {
            textViewDesp?.visibility = VISIBLE
            textViewDesp?.text = meta?.description
        }
        linearLayout?.setOnClickListener { view ->
                richLinkClicked()
        }
    }

    private fun richLinkClicked() {
        val url=if(main_url?.contains("http")==false){
            "https://$main_url"
        } else main_url
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }


    private fun findLinearLayoutChild(): LinearLayout? {
        return if (childCount > 0 && getChildAt(0) is LinearLayout) {
            getChildAt(0) as LinearLayout
        } else null
    }

    fun setLinkFromMeta(metaData: MetaData?) {
        meta = metaData
        initView()
    }

    val metaData: MetaData?
        get() = meta

    fun setLink(url: String, scope: CoroutineScope) {

        scope.launch {
            when(val data=LinkHandler.getLink(url)){
                is Result.Success->{
                    meta=data.data
                    initView()
                }
                is Result.Error->{

                }
            }
        }
    }
}