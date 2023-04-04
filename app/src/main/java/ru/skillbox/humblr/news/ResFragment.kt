package ru.skillbox.humblr.news

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import dagger.hilt.android.AndroidEntryPoint
import kohii.v1.core.*
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import ru.skillbox.humblr.MainNavGraphDirections
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Comment
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.entities.UserInfo
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.databinding.RecycleFragmentBinding
import ru.skillbox.humblr.favorites.FavoritesFragment
import ru.skillbox.humblr.favorites.FavoritesSavedFragment
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.*
import ru.skillbox.humblr.utils.adapters.*
import kotlin.properties.Delegates

@AndroidEntryPoint
class ResFragment : Fragment(), CommentAdapter.CommentHandler, MListener, CallBack {
    private var _binding: RecycleFragmentBinding? = null
    val binding: RecycleFragmentBinding
        get() = _binding!!
    val viewModel: ResFragViewModel by viewModels()
    private var _kohii: Kohii? = null
    private val kohiiM: Kohii
        get() = _kohii!!
    private var manager: Manager? = null
    private lateinit var layoutManager: LinearLayoutManager
    private var pos = 0
    var loading = false
    private lateinit var subsribers: HashMap<Int, MViewHolder.YoutubeViewHolder>
    private var playback: Playback? = null
    var adapter: NewsAdapter? = null
    var commAdapter: CommentFavoritesAdapter? = null
    private var argType: String? = null
    var type = ""

    @OptIn(InternalCoroutinesApi::class)
    private var selection by Delegates.observable<Pair<Int, Rebinder?>>(
        initialValue = -1 to null,
        onChange = { _, from, to ->
            if (from == to) return@observable
            val (oldPos, oldRebinder) = from
            val (newPos, newRebinder) = to
            if (newRebinder != null) {
                pos = newPos
                val link = (binding.rec.recyclerView.adapter as NewsAdapter).getItemLink(newPos)
                (activity as MainActivity).navigateToRedditVideoFragment(newRebinder, newPos, link)
                binding.rec.recyclerView.adapter?.notifyItemChanged(newPos)
            } else {
                if (oldRebinder != null) {
                    playback?.also {
                        val vh = binding.rec.recyclerView.findViewHolderForAdapterPosition(oldPos)
                        if (vh == null) it.unbind()
                        binding.rec.recyclerView.adapter?.notifyItemChanged(oldPos)
                    }
                    playback = null
                }
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecycleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rec.visibility = View.INVISIBLE
        binding.progress.root.visibility = View.VISIBLE
        binding.progress.progressL.visibility = View.VISIBLE
        _kohii = KohiiProvider.get(requireContext())
        val args = arguments?.getString(FavoritesSavedAdapter.ARG_TYPE)
        argType = args
        if (savedInstanceState == null) {
            viewModel.getMe()
            if (args == "COMMENTS_ALL" || args == "COMMENTS_SAVED") {
                type = "comment"
                prepareCommentAdapter()
            } else {
                type = "link"
                prepareLinkAdapter()
            }
        }
        bind()
    }

    fun prepareCommentAdapter() {
        val args = arguments?.getString(FavoritesSavedAdapter.ARG_TYPE)
        binding.rec.setOnRetryClickListener {
            viewModel.exceptions.postValue(null)
            val name = viewModel.me.value?.name
            if (args == "COMMENTS_SAVED") {
                if (name != null) {
                    viewModel.getSavedComments(
                        name,
                        null,
                        null,
                        null,
                        null,
                        null,
                        RedditApi.Time.all,
                        2,
                        RedditApi.Sort.top
                    )
                }
            } else if (args == "COMMENTS_ALL") {
                if (name != null) {
                    viewModel.getMineComments(
                        name,
                        null,
                        null,
                        null,
                        null,
                        null,
                        RedditApi.Time.all,
                        2,
                        RedditApi.Sort.top
                    )
                }
            }
        }
        commAdapter = CommentFavoritesAdapter(lifecycleScope, this)
        binding.rec.recyclerView.adapter = commAdapter
        layoutManager = binding.rec.recyclerView.layoutManager as LinearLayoutManager
        binding.rec.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastIndex = layoutManager.findLastVisibleItemPosition()
                if (commAdapter!!.getItem(lastIndex) is Comment.LoadingComment && commAdapter!!.itemCount > 0) {
                    startLoadComm()
                }
            }

        })
    }

    @OptIn(InternalCoroutinesApi::class)
    fun prepareLinkAdapter() {
        val args = arguments?.getString(FavoritesSavedAdapter.ARG_TYPE)
        subsribers = HashMap()
        adapter = NewsAdapter(this, this)
        binding.rec.recyclerView.adapter = adapter
        binding.rec.setOnRetryClickListener {
            viewModel.exceptions.postValue(null)
            val name = viewModel.me.value?.name
            if (args == "LINKS_ALL") {
                if (name != null) {
                    viewModel.getSubredditsMine(name, null, null, null, null, true, {
                        if (adapter!!.itemCount > 0) {
                            adapter?.removeLoad()
                        } else {
                            binding.rec.showEmptyView()
                        }
                    }, binding.progress.progressL)
                }
            } else if (args == "LINKS_SAVED") {
                if (name != null) {
                    viewModel.getSavedSubreddits(
                        name,
                        null,
                        null,
                        null,
                        null,
                        null,
                        RedditApi.Time.all,
                        2,
                        RedditApi.Sort.top,
                        binding.progress.progressL
                    ) {
                        if (adapter!!.itemCount > 0) {
                            adapter?.removeLoad()
                        } else {
                            binding.rec.showEmptyView()
                        }
                    }
                }
            }
        }
        manager =
            kohiiM.register(this, memoryMode = MemoryMode.HIGH)
                .addBucket(binding.rec.recyclerView)
        layoutManager = binding.rec.recyclerView.layoutManager as LinearLayoutManager
        binding.rec.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            val PERCENT_SHOW = 80
            val PERCENT_HIDE = 50
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastIndex = layoutManager.findLastVisibleItemPosition()
                if (adapter!!.getLink(lastIndex) is Link.LoadingLink && adapter!!.itemCount > 0) {
                    startLoad()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        val firstIndex = layoutManager.findFirstVisibleItemPosition()
                        val lastIndex = layoutManager.findLastVisibleItemPosition()
                        for (index in firstIndex..lastIndex) {
                            if (subsribers.containsKey(index)) {
                                val rvRect = Rect()
                                binding.rec.recyclerView.getLocalVisibleRect(rvRect)
                                val rowRect = Rect()
                                layoutManager.findViewByPosition(index)
                                    ?.getGlobalVisibleRect(rowRect)
                                val percent: Int = if (rowRect.top >= rvRect.bottom) {
                                    val visibleHeight = rowRect.top - rvRect.bottom
                                    (visibleHeight * 100) / layoutManager.findViewByPosition(
                                        index
                                    )!!.height

                                } else {
                                    val visibleHeight = rowRect.bottom - rvRect.top
                                    (visibleHeight * 100) / layoutManager.findViewByPosition(
                                        index
                                    )!!.height
                                }

                                if (percent > PERCENT_SHOW) {
                                    val holder = subsribers[index]
                                    holder?.initYoutube(getYoutubePlayer()) {
                                        (activity as MainActivity).navigateToYoutubeFragment(
                                            direction = it
                                        )
                                    }
                                } else if (percent < PERCENT_HIDE) {
                                    val holder = subsribers[index]
                                    holder?.onDetached()
                                }
                            }

                        }
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {}
                    RecyclerView.SCROLL_STATE_SETTLING -> {}
                }
            }
        })
    }

    fun onCommentsRetry() {
        val args = arguments?.getString(FavoritesSavedAdapter.ARG_TYPE)
        viewModel.exceptions.postValue(null)
        val name = viewModel.me.value?.name
        if (args == "COMMENTS_SAVED") {
            if (name != null) {
                viewModel.getSavedComments(
                    name,
                    null,
                    null,
                    null,
                    null,
                    null,
                    RedditApi.Time.all,
                    2,
                    RedditApi.Sort.top
                )
            }
        } else if (args == "COMMENTS_ALL") {
            if (name != null) {
                viewModel.getMineComments(
                    name,
                    null,
                    null,
                    null,
                    null,
                    null,
                    RedditApi.Time.all,
                    2,
                    RedditApi.Sort.top
                )
            }
        }
    }

    fun onSubredditRetry() {
        val args = arguments?.getString(FavoritesSavedAdapter.ARG_TYPE)
        viewModel.exceptions.postValue(null)
        val name = viewModel.me.value?.name
        if (args == "LINKS_ALL") {
            if (name != null) {
                viewModel.getSubredditsMine(name, null, null, null, null, true, {
                    if (adapter!!.itemCount > 0) {
                        adapter?.removeLoad()
                    } else {
                        binding.rec.showEmptyView()
                    }
                }, binding.progress.progressL)
            }
        } else if (args == "LINKS_SAVED") {
            if (name != null) {
                viewModel.getSavedSubreddits(
                    name,
                    null,
                    null,
                    null,
                    null,
                    null,
                    RedditApi.Time.all,
                    2,
                    RedditApi.Sort.top,
                    binding.progress.progressL
                ) {
                    if (adapter!!.itemCount > 0) {
                        adapter?.removeLoad()
                    } else {
                        binding.rec.showEmptyView()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @OptIn(InternalCoroutinesApi::class)
    fun bind() {
        viewModel.me.observe(viewLifecycleOwner) {
            if (it != null) {
                when (argType) {
                    "LINKS_ALL" -> {
                        binding.progress.root.visibility = View.VISIBLE
                        viewModel.getSubredditsMine(it.name!!, null, null, null, null, true, {
                            if (adapter!!.itemCount > 0) {
                                adapter?.removeLoad()
                            } else {
                                binding.rec.showEmptyView()
                            }
                        }, binding.progress.progressL)
                    }
                    "LINKS_SAVED" -> {
                        binding.progress.root.visibility = View.VISIBLE
                        viewModel.getSavedSubreddits(
                            it.name!!,
                            null,
                            null,
                            null,
                            null,
                            true,
                            RedditApi.Time.all,
                            2,
                            RedditApi.Sort.top,
                            binding.progress.progressL
                        ) {
                            if (adapter!!.itemCount > 0) {
                                adapter?.removeLoad()
                            } else {
                                binding.rec.showEmptyView()
                            }
                        }
                    }
                    "COMMENTS_SAVED" -> {
                        viewModel.getMineComments(
                            it.name!!,
                            null,
                            null,
                            null,
                            null,
                            true,
                            RedditApi.Time.all,
                            2,
                            RedditApi.Sort.top
                        )
                    }
                    "COMMENTS_ALL" -> {
                        viewModel.getMineComments(
                            it.name!!,
                            null,
                            null,
                            null,
                            null,
                            true,
                            RedditApi.Time.all,
                            2,
                            RedditApi.Sort.top
                        )
                    }
                }
            }
        }
        viewModel.apply {
            exceptions.observe(viewLifecycleOwner) {
                if (it is MainRepository.TokenISInvalidException) {
                    (activity as MainActivity).onTokenExpired(this@ResFragment)
                    return@observe
                }
                binding.rec.showErrorView(it.message)
            }
            recyclerViewVolume.observe(viewLifecycleOwner) {
                manager?.applyVolumeInfo(it, binding.rec.recyclerView, Scope.BUCKET)
            }
            rebinder.observe(viewLifecycleOwner) {
                selection = it
            }
            links.observe(viewLifecycleOwner) {
                if (it != null) {
                    val adapterw = binding.rec.recyclerView.adapter
                    if (adapterw == null) {
                        /*subsribers = HashMap()
                        adapter= NewsAdapter(this@ResFragment, this@ResFragment)
                        binding.rec.recyclerView.adapter=adapter*/
                        prepareLinkAdapter()
                    }
                    if (binding.rec.visibility == View.INVISIBLE) {
                        binding.progress.root.visibility = View.INVISIBLE
                        binding.rec.visibility = View.VISIBLE
                        binding.rec.apply {
                            alpha = 0f
                            animate().alpha(1f).setDuration(200).start()
                        }
                    }
                    loading = false
                    if (it.isNotEmpty()) {
                        val last = it.last()
                        val list = adapter?.list
                        if (list?.contains(last) == false) {
                            adapter?.addLinks(it)
                        } else {
                            adapter?.removeLoad()
                        }
                    }
                }
            }
            comments.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (adapter == null) {
                        /*commAdapter = CommentFavoritesAdapter(lifecycleScope, this@ResFragment)
                        binding.rec.recyclerView.adapter = commAdapter*/
                        prepareCommentAdapter()
                    }
                    if (binding.rec.visibility == View.INVISIBLE) {
                        binding.progress.root.visibility = View.INVISIBLE
                        binding.rec.visibility = View.VISIBLE
                        binding.rec.apply {
                            alpha = 0f
                            animate().alpha(1f).setDuration(200).start()
                        }
                    }
                    loading = false
                    if (it.isNotEmpty()) {
                        val last = it.last()
                        val list = commAdapter?.list
                        if (list != null) {
                            if (!list.contains(last)) {
                                commAdapter?.addComments(it)
                            } else {
                                commAdapter?.removeLoad()
                            }
                        } else {
                            commAdapter?.addComments(it)
                        }

                    }
                }
            }
        }
    }

    override fun getPage(index: Int, preview: Boolean) {
    }

    override suspend fun vote(
        num: Int,
        name: String,
        view: MControllerView,
        tickerView: TickerView
    ) {
        if (view.state == MControllerView.State.SELECTED) {
            lifecycleScope.launch {

                when (viewModel.vote(0, name, null)) {
                    is Result.Error -> {
                        view.changeState(MControllerView.State.SELECTED)
                    }
                    is Result.Success -> {
                        val numberText = tickerView.text
                        if (!numberText.contains("[km]".toRegex())) {
                            val number = numberText.toString().toInt()
                            tickerView.text = "${number - num}"
                        }
                    }
                }

            }
        } else {
            lifecycleScope.launch {
                when (viewModel.vote(num, name, null)) {
                    is Result.Success -> {
                        val numberText = tickerView.text
                        if (!numberText.contains("[km]".toRegex())) {
                            val number = numberText.toString().toInt()
                            tickerView.text = "${number + num}"
                        }
                    }
                    is Result.Error -> {
                        view.changeState(MControllerView.State.RELEASED)
                    }
                }
            }
        }
    }

    override fun ready() {

    }

    override fun writeComment(
        view: View,
        parent: ViewGroup,
        comment: Comment,
        root: Boolean,
        depth: Int
    ) {
        binding.rec.visibility = View.GONE
        binding.writeCommentLayout.root.visibility = View.VISIBLE
        val parent1 = parentFragment as FavoritesSavedFragment
        val parent2 = parent1.parentFragment as FavoritesFragment
        parent1.binding.tub.visibility = View.GONE
        parent2.binding.tub.visibility = View.GONE
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.writeCommentLayout.editor.focusEditor()
        imm.showSoftInput(binding.writeCommentLayout.editor, InputMethodManager.SHOW_IMPLICIT)
        binding.writeCommentLayout.exit.setOnClickListener {
            binding.rec.visibility = View.VISIBLE
            binding.writeCommentLayout.root.visibility = View.INVISIBLE
            parent1.binding.tub.visibility = View.VISIBLE
            parent2.binding.tub.visibility = View.VISIBLE
            binding.writeCommentLayout.editor.clearFocus()
            imm.hideSoftInputFromWindow(
                binding.writeCommentLayout.editor.windowToken,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
        binding.writeCommentLayout.title.text =
            comment.body?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
        val instant = comment.createdUTC?.let { Instant.ofEpochSecond(it) }
        val res = resources
        val prefix =
            if (comment.edited != null) res.getString(R.string.edited) else {
                ""
            }
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        val pref = "$${comment.author} $prefix"
        when {
            duration.toDays() > 0 -> {
                binding.writeCommentLayout.userName.text =
                    String.format(
                        res.getString(R.string.days_ago),
                        pref,
                        duration.toDays()
                    )

            }
            duration.toHours() > 0 -> {
                binding.writeCommentLayout.userName.text =
                    String.format(
                        res.getString(R.string.hours_ago),
                        pref,
                        duration.toDays()
                    )

            }
            duration.toMinutes() > 0 -> {
                binding.writeCommentLayout.userName.text =
                    String.format(
                        res.getString(R.string.minutes_ago),
                        pref,
                        duration.toDays()
                    )
            }
            else -> {
                binding.writeCommentLayout.userName.text =
                    String.format(
                        res.getString(R.string.now),
                        pref,
                        duration.toDays()
                    )
            }
        }
        binding.writeCommentLayout.post.setOnClickListener {
            if (binding.writeCommentLayout.editor.html == null || binding.writeCommentLayout.editor.html!!.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Nothing to post because message is empty",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val commentR =
                    viewModel.postComment(comment.name!!, binding.writeCommentLayout.editor.html!!)

                if (commentR != null) {
                    val me = viewModel.me.value
                    commentR.account = UserInfo(me?.name!!, me.icon!!)
                    if (root) {
                        val group = parent as LinearLayoutCompat
                        val layout = LayoutInflater.from(group.context)
                            .inflate(R.layout.comment_view_reply, group, false)
                        bind(layout, commentR)
                        group.addView(layout, 0)
                        binding.writeCommentLayout.editor.html = ""
                        binding.rec.visibility = View.VISIBLE
                        binding.writeCommentLayout.root.visibility = View.INVISIBLE
                        binding.writeCommentLayout.editor.clearFocus()
                        imm.hideSoftInputFromWindow(
                            binding.writeCommentLayout.editor.windowToken,
                            InputMethodManager.HIDE_IMPLICIT_ONLY
                        )
                    } else {
                        val group = parent as LinearLayoutCompat
                        val index = group.indexOfChild(view)
                        val layout = LayoutInflater.from(group.context)
                            .inflate(R.layout.comment_view_reply, group, false)
                        bind(layout, commentR)
                        val columnHolder =
                            layout.findViewById<LinearLayoutCompat>(R.id.column_holder)
                        for (i in 1 until depth + 1) {
                            val mView = View(layout.context)
                            mView.setBackgroundColor(layout.context.getColor(R.color.grey2))
                            columnHolder.addView(mView)
                            val params = mView.layoutParams as LinearLayoutCompat.LayoutParams
                            params.leftMargin = 26.dp
                            mView.layoutParams.width = 3.dp
                        }
                        group.addView(layout, index + 1)
                        binding.writeCommentLayout.editor.html = ""
                        binding.rec.visibility = View.VISIBLE
                        binding.writeCommentLayout.root.visibility = View.INVISIBLE
                        parent1.binding.tub.visibility = View.VISIBLE
                        parent2.binding.tub.visibility = View.VISIBLE
                        binding.writeCommentLayout.editor.clearFocus()
                        imm.hideSoftInputFromWindow(
                            binding.writeCommentLayout.editor.windowToken,
                            0
                        )
                    }
                }
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    fun bind(root: View, comment: Comment) {
        lifecycleScope.launch {
            val icon = viewModel.me.value?.icon
            val iconp = icon?.replace("amp;", "")
            Glide.with(root).load(iconp)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).override(150, 150)
                .into(root.findViewById(R.id.avatar))
            root.findViewById<TextView>(R.id.nickname).text = comment.author
            val messageView = root.findViewById<TextView>(R.id.text)
            messageView.text =
                comment.body?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
            messageView.movementMethod = LinkMovementMethod.getInstance()
            val instant = if (comment.edited != null) comment.edited.let {
                Instant.ofEpochSecond(it)
            } else {
                comment.createdUTC?.let { Instant.ofEpochSecond(it) }
            }
            val res = messageView.context.resources
            val prefix =
                if (comment.edited != null) res.getString(R.string.edited) else {
                    ""
                }
            val now = Instant.now()
            val duration = Duration.between(instant, now)
            val timeView = root.findViewById<TextView>(R.id.time)
            when {
                duration.toDays() > 0 -> {
                    timeView.text =
                        String.format(
                            res.getString(R.string.days_ago),
                            prefix,
                            duration.toDays()
                        )
                }
                duration.toHours() > 0 -> {
                    timeView.text =
                        String.format(
                            res.getString(R.string.hours_ago),
                            prefix,
                            duration.toHours()
                        )
                }
                duration.toMinutes() > 0 -> {
                    timeView.text =
                        String.format(
                            res.getString(R.string.minutes_ago),
                            prefix,
                            duration.toMinutes()
                        )
                }
                else -> {
                    timeView.text = String.format(res.getString(R.string.now), prefix)
                }
            }
            val voteNumber = root.findViewById<TickerView>(R.id.vote_number)
            voteNumber.setCharacterLists(TickerUtils.provideNumberList())
            val score = comment.score
            when {
                score == null -> {
                    voteNumber.text = "0"
                }
                score > 1000000 -> {
                    voteNumber.text =
                        "${(comment.score!!.toFloat() / 1000).toString().substring(0..3)}m"
                }
                score > 1000 -> {
                    val texts = (comment.score!!.toFloat() / 1000).toString()
                    if (texts.length > 4) {
                        voteNumber.text = "${texts.substring(0..4)}k"
                    } else {
                        voteNumber.text = "${texts}k"
                    }
                }
                else -> {
                    voteNumber.text = comment.score.toString()
                }
            }
            val upvoteB = root.findViewById<MControllerView>(R.id.up_vote)
            val downVoteB = root.findViewById<MControllerView>(R.id.down_vote)

            upvoteB.onClickListener {
                lifecycleScope.launch {
                    vote(1, "t1_${comment.id}", upvoteB, voteNumber)
                }
            }
            downVoteB.onClickListener {
                lifecycleScope.launch {
                    vote(-1, "t1_${comment.id}", downVoteB, voteNumber)
                }
            }
        }
    }

    override fun save(view: MControllerView, commentId: String) {
        if (view.state == MControllerView.State.SELECTED) {
            lifecycleScope.launch {

                when (viewModel.unsave(commentId)) {
                    true -> {
                        //nothing to do, everything ok!
                    }
                    false -> {
                        view.changeState(MControllerView.State.SELECTED)
                    }
                }
            }
        } else {
            lifecycleScope.launch {
                when (viewModel.save(commentId, "comment")) {
                    true -> {
                        //nothing to do, everything ok!
                    }
                    false -> {
                        view.changeState(MControllerView.State.RELEASED)
                    }
                }
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onClick(view: View, item: Link) {
        val link = item as Link.LinkOut
        (activity as MainActivity).navigateToPictFragment(link.permalink)

    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onPict(view: View, link: String) {
        (activity as MainActivity).navigateToPictFragment(link)

    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onText(view: View, link: String) {
        (activity as MainActivity).navigateToTextFragment(link)
    }

    override fun getKohii(): Kohii {
        return kohiiM
    }

    override fun shouldRebindVideo(rebinder: Rebinder?): Boolean {
        return rebinder != selection.second
    }

    override fun onVideoClick(position: Int, rebinder: Rebinder, view: View) {
        selectRebinder(position, rebinder)
        activity?.let {
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                it,
                androidx.core.util.Pair.create(view, "on_recycle")
            )
        }
    }

    override fun onMute(imageView: MImageView) {
        if (imageView.isOff()) {
            imageView.switch()
        } else {
            imageView.switch()
        }
        val current = viewModel.recyclerViewVolume.value!!
        viewModel.recyclerViewVolume.value =
            VolumeInfo(!current.mute, current.volume)
    }

    override fun isMuted(): Boolean {
        val value = viewModel.recyclerViewVolume.value
        return value!!.mute
    }

    override fun subscribe(holder: MViewHolder.YoutubeViewHolder) {
        val postion = holder.absoluteAdapterPosition
        subsribers[postion] = holder
    }

    override fun removeSubscription(index: Int) {
        subsribers.remove(index)
    }

    override fun getScope(): CoroutineScope {
        return lifecycleScope
    }

    internal fun selectRebinder(
        position: Int,
        rebinder: Rebinder
    ) {
        viewModel.setRebinder(position to rebinder)
    }

    override fun onJoin(view: MControllerView, subredditName: String, textView: MTextView) {
        if (view.state == MControllerView.State.SELECTED) {
            lifecycleScope.launch {
                when (viewModel.subscribe(RedditApi.SubscibeType.unsub, null, subredditName)) {
                    is Result.Success -> {
                        Log.d("result", "success")
                        textView.setColor(resources.getColor(R.color.unselected, null))
                    }
                    is Result.Error -> {
                        Log.d("result", "not success")
                        view.changeState(MControllerView.State.SELECTED)
                    }
                }
            }

        } else if (view.state == MControllerView.State.RELEASED) {

            lifecycleScope.launch {
                when (viewModel.subscribe(RedditApi.SubscibeType.sub, true, subredditName)) {
                    is Result.Success -> {
                        textView.setColor(resources.getColor(R.color.selected, null))
                    }
                    is Result.Error -> {
                        view.changeState(MControllerView.State.RELEASED)
                    }
                }
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun navigateToUser(user: String) {
        val direction = MainNavGraphDirections.actionGlobalProfileGrapth(user)
        findNavController().navigate(direction)
    }

    override fun onPict2(extras: FragmentNavigator.Extras, link: String) {
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onText2(view: View, link: Link.LinkText) {
        (activity as MainActivity).navigateToTextFragment(link.permalink)
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onYoutube(link: String, id: String, second: Long) {
        (activity as MainActivity).navigateToYoutubeFragment(link, id, second)
    }

    fun startLoad(): Boolean {
        if (!loading) {
            loading = true
            val last = viewModel.links.value?.get(viewModel.links.value!!.lastIndex) as Created
            if (argType == "LINKS_ALL") {
                viewModel.getSubredditsMine(
                    viewModel.me.value?.name!!,
                    null,
                    last.getIds(),
                    null,
                    null,
                    true,
                    {
                        if (adapter!!.itemCount > 0) {
                            adapter?.removeLoad()
                        } else {
                            binding.rec.showEmptyView()
                        }
                    },
                    binding.progress.progressL
                )
            } else if (argType == "LINKS_SAVED") {
                viewModel.getSavedSubreddits(
                    viewModel.me.value?.name!!,
                    null,
                    last.getIds(),
                    null,
                    null,
                    true,
                    RedditApi.Time.all,
                    2,
                    RedditApi.Sort.top,
                    binding.progress.progressL
                ) {
                    if (adapter!!.itemCount > 0) {
                        adapter?.removeLoad()
                    } else {
                        binding.rec.showEmptyView()
                    }
                }
            }
        }
        return false
    }

    fun startLoadComm() {
        if (!loading) {
            loading = true
            val last = viewModel.links.value?.get(viewModel.links.value!!.lastIndex) as Created

            if (argType == "COMMENTS_ALL") {
                viewModel.getMineComments(
                    viewModel.me.value?.name!!,
                    null,
                    last.getIds(),
                    null,
                    null,
                    true,
                    RedditApi.Time.all,
                    2,
                    RedditApi.Sort.top
                )
            } else if (argType == "COMMENTS_SAVED") {
                viewModel.getSavedComments(
                    viewModel.me.value?.name!!,
                    null,
                    last.getIds(),
                    null,
                    null,
                    true,
                    RedditApi.Time.all,
                    2,
                    RedditApi.Sort.top
                )
            }
        }
    }

    fun getYoutubePlayer(): YouTubePlayerView {
        val youTubePlayerView = YouTubePlayerView(requireContext())
        youTubePlayerView.enableAutomaticInitialization = false
        return youTubePlayerView
    }

    override fun invoke() {
        viewModel.exceptions.postValue(null)
        when (type) {
            "comment" -> {
                onCommentsRetry()
            }
            "link" -> {
                onSubredditRetry()
            }
        }
    }
}