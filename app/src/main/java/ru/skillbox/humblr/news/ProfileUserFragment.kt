package ru.skillbox.humblr.news

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kohii.v1.core.*
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.databinding.ProfileUserBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.news.State
import ru.skillbox.humblr.utils.*
import ru.skillbox.humblr.utils.adapters.MViewHolder
import ru.skillbox.humblr.utils.adapters.NewsAdapter
import kotlin.math.abs
import kotlin.properties.Delegates

@AndroidEntryPoint
class ProfileUserFragment : Fragment(), LCEERecyclerView2.OnLoad, MListener {
    var _binding: ProfileUserBinding? = null
    val binding: ProfileUserBinding
        get() = _binding!!
    val viewModel: ProfileUserViewModel by viewModels()
    val argsLazy: ProfileUserFragmentArgs by navArgs()
    var subscribedIcon: Drawable? = null
    var subscribeIcon: Drawable? = null
    private var _kohii: Kohii? = null
    private val kohiiM: Kohii
        get() = _kohii!!
    private lateinit var manager: Manager
    private lateinit var adapter: NewsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var pos = 0
    var choise = 0
    var loading = false
    var prev: String? = ""
    private lateinit var ivUserAvatar: CircleImageView
    private lateinit var toolbar: Toolbar
    private lateinit var appBarLayout: AppBarLayout
    private var cashCollapseState: Pair<Int, Int>? = null
    private lateinit var titleToolbarText: AppCompatTextView
    private lateinit var background: FrameLayout

    private lateinit var subsribers: HashMap<Int, MViewHolder.YoutubeViewHolder>
    private var playback: Playback? = null

    @OptIn(InternalCoroutinesApi::class)
    private var selection by Delegates.observable<Pair<Int, Rebinder?>>(
        initialValue = -1 to null,
        onChange = { _, from, to ->
            if (from == to) return@observable
            val (oldPos, oldRebinder) = from
            val (newPos, newRebinder) = to
            if (newRebinder != null) {
                pos = newPos
                val options = activity?.let { frag ->
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        frag,
                        androidx.core.util.Pair.create(view, "full_screen")
                    )
                }
                val link =
                    (binding.recyclerView.recyclerView.adapter as NewsAdapter).getItemLink(newPos)
                (activity as MainActivity).navigateToRedditVideoFragment(newRebinder, newPos, link)
                binding.recyclerView.recyclerView.adapter?.notifyItemChanged(newPos)
            } else {
                if (oldRebinder != null) {
                    playback?.also {
                        val vh = binding.recyclerView.recyclerView.findViewHolderForAdapterPosition(
                            oldPos
                        )
                        if (vh == null) it.unbind()
                        binding.recyclerView.recyclerView.adapter?.notifyItemChanged(oldPos)
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
        subsribers = HashMap()
        _kohii = KohiiProvider.get(requireContext())
        subscribedIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.subscribed)
        subscribeIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.subscribe)
        _binding = ProfileUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    internal fun selectRebinder(
        position: Int,
        rebinder: Rebinder
    ) {
        viewModel.setRebinder(position to rebinder)
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userName = argsLazy.account
        bind()
        appBarLayout = binding.appBar
        toolbar = binding.animToolbar
        ivUserAvatar = binding.avatarView
        titleToolbarText = binding.fullName
        background = binding.flBackground
        appBarLayout.addOnOffsetChangedListener { appBarLayout, i ->
            updateViews(abs(i / appBarLayout.totalScrollRange.toFloat()))
        }
        manager = kohiiM.register(this, memoryMode = MemoryMode.HIGH)
            .addBucket(binding.recyclerView.recyclerView)
        layoutManager = binding.recyclerView.recyclerView.layoutManager as LinearLayoutManager
        adapter = NewsAdapter(listener = this)
        binding.recyclerView.recyclerView.adapter = adapter
        binding.writeCommentB.setOnClickListener {
            val direction =
                ProfileUserFragmentDirections.actionProfileUserFragmentToWriteMessageFragment(
                    viewModel.me.value?.name!!,
                    viewModel.account.value?.name!!
                )
            findNavController().navigate(direction)
        }
        binding.subscribeB.setOnClickListener {
            val button = it as FloatingActionButton
            if (binding.subscribe.icon == subscribedIcon) {
                button.setImageDrawable(subscribeIcon)
                binding.subscribe.icon = subscribeIcon
                binding.subscribe.setBackgroundColor(resources.getColor(R.color.unsub, null))
                button.setBackgroundColor(resources.getColor(R.color.unsub, null))
                val subreddit = viewModel.account.value?.subreddit?.name
                if (subreddit != null) {
                    lifecycleScope.launch {
                        when (viewModel.subscribe(RedditApi.SubscibeType.sub, false, subreddit)) {
                            is Result.Success -> {

                            }
                            is Result.Error -> {
                                button.setImageDrawable(subscribedIcon)
                                button.setBackgroundColor(
                                    resources.getColor(
                                        R.color.selected,
                                        null
                                    )
                                )
                                binding.subscribe.icon = subscribedIcon
                                binding.subscribe.setBackgroundColor(
                                    resources.getColor(
                                        R.color.selected,
                                        null
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                button.setImageDrawable(subscribedIcon)
                binding.subscribe.icon = subscribedIcon
                button.setBackgroundColor(resources.getColor(R.color.selected, null))
                binding.subscribe.setBackgroundColor(resources.getColor(R.color.selected, null))
                val subreddit = viewModel.account.value?.subreddit?.name
                if (subreddit != null) {
                    lifecycleScope.launch {
                        when (viewModel.subscribe(RedditApi.SubscibeType.sub, false, subreddit)) {
                            is Result.Success -> {

                            }
                            is Result.Error -> {
                                button.setImageDrawable(subscribeIcon)
                                button.setBackgroundColor(
                                    resources.getColor(
                                        R.color.unselected,
                                        null
                                    )
                                )
                                binding.subscribe.icon = subscribeIcon
                                binding.subscribe.setBackgroundColor(
                                    resources.getColor(
                                        R.color.unselected,
                                        null
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        binding.writeComment.setOnClickListener {
            val direction =
                ProfileUserFragmentDirections.actionProfileUserFragmentToWriteMessageFragment(
                    viewModel.me.value?.name!!,
                    viewModel.account.value?.name!!
                )
            findNavController().navigate(direction)
        }
        binding.subscribe.setOnClickListener {
            val button = it as ExtendedFloatingActionButton
            val small = binding.subscribeB
            if (button.icon == subscribedIcon) {
                small.setImageDrawable(subscribeIcon)
                binding.subscribe.icon = subscribeIcon
                small.setBackgroundColor(resources.getColor(R.color.unsub, null))
                binding.subscribe.setBackgroundColor(resources.getColor(R.color.unsub, null))
                val subreddit = viewModel.account.value?.subreddit?.name
                if (subreddit != null) {
                    lifecycleScope.launch {
                        when (viewModel.subscribe(RedditApi.SubscibeType.sub, false, subreddit)) {
                            is Result.Success -> {

                            }
                            is Result.Error -> {
                                small.setImageDrawable(subscribedIcon)
                                small.setBackgroundColor(
                                    resources.getColor(
                                        R.color.selected,
                                        null
                                    )
                                )
                                binding.subscribe.icon = subscribedIcon
                                binding.subscribe.setBackgroundColor(
                                    resources.getColor(
                                        R.color.selected,
                                        null
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                small.setImageDrawable(subscribedIcon)
                small.setBackgroundColor(resources.getColor(R.color.selected, null))
                binding.subscribe.icon = subscribedIcon
                binding.subscribe.setBackgroundColor(resources.getColor(R.color.selected, null))
                val subreddit = viewModel.account.value?.subreddit?.name
                if (subreddit != null) {
                    lifecycleScope.launch {
                        when (viewModel.subscribe(RedditApi.SubscibeType.sub, false, subreddit)) {
                            is Result.Success -> {

                            }
                            is Result.Error -> {
                                small.setImageDrawable(subscribeIcon)
                                small.setBackgroundColor(
                                    resources.getColor(
                                        R.color.unselected,
                                        null
                                    )
                                )
                                binding.subscribe.icon = subscribeIcon
                                binding.subscribe.setBackgroundColor(
                                    resources.getColor(
                                        R.color.unselected,
                                        null
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.getInfo(userName)
            viewModel.getUserLinks(userName, null, null, null, null, true)
            viewModel.getMe()
        }
        binding.recyclerView.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            val PERCENT_SHOW = 80
            val PERCENT_HIDE = 50
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastIndex = layoutManager.findLastVisibleItemPosition()
                if (adapter.getLink(lastIndex) is Link.LoadingLink && adapter.itemCount > 0) {
                    startLoad()
                }
                binding.readMore.setOnClickListener {
                    viewModel.linkState.postValue(ProfileUserViewModel.State.EXPANDED)
                    binding.readMore.visibility = View.INVISIBLE
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
                                binding.recyclerView.recyclerView.getLocalVisibleRect(rvRect)
                                val rowRect = Rect()
                                layoutManager.findViewByPosition(index)
                                    ?.getGlobalVisibleRect(rowRect)
                                val percent: Int = if (rowRect.top >= rvRect.bottom) {
                                    val visibleHeight = rowRect.top - rvRect.bottom
                                    (visibleHeight * 100) / layoutManager.findViewByPosition(index)!!.height
                                } else {
                                    val visibleHeight = rowRect.bottom - rvRect.top
                                    (visibleHeight * 100) / layoutManager.findViewByPosition(index)!!.height
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

    fun getYoutubePlayer(): YouTubePlayerView {
        val youTubePlayerView = YouTubePlayerView(requireContext())
        youTubePlayerView.enableAutomaticInitialization = false
        return youTubePlayerView
    }

    fun bind() {
        viewModel.apply {
            recyclerViewVolume.observe(viewLifecycleOwner) {
                manager.applyVolumeInfo(it, binding.recyclerView.recyclerView, Scope.BUCKET)
            }
            account.observe(viewLifecycleOwner) {
                if (it != null) {
                    val avatar = it.icon?.replace("amp;", "")
                    Glide.with(requireContext()).load(avatar).dontAnimate().centerCrop()
                        .into(binding.avatar)
                    binding.name.text = it.name
                    Glide.with(this@ProfileUserFragment)
                        .load(avatar)
                        .dontAnimate()
                        .centerCrop()
                        .into(binding.avatarView)
                    binding.fullName.text = it.name
                    binding.nickName.text = it.id
                    viewModel.getCommentsMine(
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
            userLinks.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (viewModel.linkState.value == ProfileUserViewModel.State.INIT) {
                        viewModel.linkState.postValue(ProfileUserViewModel.State.NOT)
                        if (it.isNotEmpty()) {
                            binding.readMore.visibility = View.VISIBLE
                        }
                    } else if (viewModel.linkState.value == ProfileUserViewModel.State.NOT) {

                    } else {
                        binding.readMore.visibility = View.INVISIBLE
                        val last = it.last()
                        val list = adapter.list
                        if (!list.contains(last)) {
                            adapter.addLinks(it)
                        } else {
                            adapter.removeLoad()
                        }
                    }

                }
            }
            linkState.observe(viewLifecycleOwner) {
                if (it == ProfileUserViewModel.State.NOT) {
                    val link = viewModel.userLinks.value?.first()
                    if (link != null) {
                        adapter.addLink(link)
                    } else {
                        binding.recyclerView.showEmptyView()
                    }
                } else if (it == ProfileUserViewModel.State.EXPANDED) {
                    val links = viewModel.userLinks.value
                    val lastIndex = links?.lastIndex!!
                    val data = mutableListOf<Link>()
                    for (i in 1..lastIndex) {
                        data.add(links[i])
                    }
                    adapter.addLinks(data)
                }
            }
            errors.observe(viewLifecycleOwner) {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
            comments.observe(viewLifecycleOwner) {
                val num = it.size.toString()
                binding.comments.text = num
                binding.commentsM.text = num
            }
        }
    }

    override fun startLoad(): Boolean {
        if (!loading) {
            loading = true
            val last =
                viewModel.userLinks.value?.lastIndex?.let { viewModel.userLinks.value?.get(it) } as Created
            val link = viewModel.userLinks.value?.get(viewModel.userLinks.value?.lastIndex!!)
            val author = link?.getAutor()
            if (prev != last.getIds()) {
                prev = last.getIds()
            } else {
                return true
            }
            if (author != null) {
                viewModel.getUserLinks(author, last.getIds(), null, null, null, true)
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        subsribers.forEach {
            it.value.onDetached()
        }
    }

    private fun updateViews(offset: Float) {
        binding.card.apply {
            alpha = (1 - offset)
        }
        when {
            offset < ProfileUserViewModel.SWITCH_BOUND -> Pair(
                ProfileUserViewModel.TO_EXPANDED,
                cashCollapseState?.second ?: ProfileUserViewModel.WAIT_FOR_SWITCH
            )
            else -> Pair(
                ProfileUserViewModel.TO_COLLAPSED,
                cashCollapseState?.second ?: ProfileUserViewModel.WAIT_FOR_SWITCH
            )
        }.apply {
            when {
                cashCollapseState != null && cashCollapseState != this -> {
                    when (first) {
                        ProfileUserViewModel.TO_EXPANDED -> {
                            ivUserAvatar.translationX = 0F
                            background.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.secondaryColor
                                )
                            )
                            binding.mini.apply {
                                alpha = 1F
                                animate().setDuration(50).alpha(0.0f)
                                visibility = View.INVISIBLE
                            }
                        }
                        ProfileUserViewModel.TO_COLLAPSED -> background.apply {
                            alpha = 0F
                            setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.primaryColor
                                )
                            )
                            animate().setDuration(250).alpha(1.0F)
                            binding.mini.apply {
                                alpha = 0f
                                animate().setDuration(100).alpha(1f).startDelay = 250
                                visibility = View.VISIBLE
                            }
                        }
                    }
                    cashCollapseState = Pair(first, ProfileUserViewModel.SWITCHED)
                }
                else -> {
                    cashCollapseState = Pair(first, ProfileUserViewModel.WAIT_FOR_SWITCH)
                }
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onClick(view: View, item: Link) {
        val link = item as Link.LinkOut
        (activity as MainActivity).navigateToLinkFragment(link.permalink)
        /*val direction =
            NewsFragmentDirections.actionNewsFragmentToDetailLinkFragment(link.permalink)
        findNavController().navigate(direction)*/
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onPict(view: View, link: String) {
        (activity as MainActivity).navigateToPictFragment(link)
        /*findNavController().navigate(
            NewsFragmentDirections.actionNewsFragmentToDetainFragment(
                link
            )
        )*/
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onText(view: View, link: String) {
        (activity as MainActivity).navigateToTextFragment(link)
        /*findNavController().navigate(
            NewsFragmentDirections.actionNewsFragmentToDetailTextFragment(
                link
            )
        )*/
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

    override fun onJoin(view: MControllerView, subredditName: String, textView: MTextView) {
        if (view.state == MControllerView.State.SELECTED) {
            lifecycleScope.launch {
                when (viewModel.subscribe(RedditApi.SubscibeType.unsub, null, subredditName)) {
                    is Result.Success -> {
                        textView.setColor(resources.getColor(R.color.unselected, null))
                    }
                    is Result.Error -> {
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
}