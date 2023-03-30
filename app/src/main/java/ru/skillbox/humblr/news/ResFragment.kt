package ru.skillbox.humblr.news

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.robinhood.ticker.TickerView
import dagger.hilt.android.AndroidEntryPoint
import kohii.v1.core.*
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Comment
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.databinding.RecycleFragmentBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.KohiiProvider
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.MImageView
import ru.skillbox.humblr.utils.MTextView
import ru.skillbox.humblr.utils.adapters.*
import kotlin.properties.Delegates

@AndroidEntryPoint
class ResFragment : Fragment(), CommentAdapter.CommentHandler, MListener {
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

    @OptIn(InternalCoroutinesApi::class)
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
                binding.rec.recyclerView.adapter = CommentsDelegateAdapter(lifecycleScope, this)
                binding.rec.setOnRetryClickListener {
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
            } else {
                subsribers = HashMap()
                adapter = NewsAdapter(this)
                binding.rec.recyclerView.adapter = adapter
                binding.rec.setOnRetryClickListener {
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
        }
        bind()


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

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
            recyclerViewVolume.observe(viewLifecycleOwner) {
                manager?.applyVolumeInfo(it, binding.rec.recyclerView, Scope.BUCKET)
            }
            rebinder.observe(viewLifecycleOwner) {
                selection = it
            }
            links.observe(viewLifecycleOwner) {
                if (it != null) {
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
        TODO("Not yet implemented")
    }

    override suspend fun vote(
        num: Int,
        name: String,
        view: MControllerView,
        tickerView: TickerView
    ) {
        TODO("Not yet implemented")
    }

    override fun ready() {
        TODO("Not yet implemented")
    }

    override fun writeComment(
        view: View,
        parent: ViewGroup,
        comment: Comment,
        root: Boolean,
        depth: Int
    ) {
        TODO("Not yet implemented")
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
        (activity as MainActivity).navigateToProfile(user)
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
}