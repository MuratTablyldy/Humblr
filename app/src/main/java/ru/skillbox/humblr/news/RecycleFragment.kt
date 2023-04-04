package ru.skillbox.humblr.news

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import kohii.v1.core.*
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import ru.skillbox.humblr.MainNavGraphDirections
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.data.interfaces.MListener
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.databinding.RecycleFragmentBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.*
import ru.skillbox.humblr.utils.adapters.MViewHolder
import ru.skillbox.humblr.utils.adapters.NewsAdapter
import ru.skillbox.humblr.utils.adapters.RecyclePagerAdapter.Companion.ARG_TYPE
import kotlin.properties.Delegates


@AndroidEntryPoint
class RecycleFragment : Fragment(), LCEERecyclerView2.OnLoad, MListener, CallBack {
    private var _kohii: Kohii? = null
    private val kohiiM: Kohii
        get() = _kohii!!
    private var _binding: RecycleFragmentBinding? = null
    val binding: RecycleFragmentBinding
        get() = _binding!!
    private lateinit var manager: Manager
    private lateinit var adapter: NewsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var pos = 0
    var choise = 0
    var loading = false
    private var subsribers: HashMap<Int, MViewHolder.YoutubeViewHolder>? = null
    val viewModel: RecycleViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecycleFragmentBinding.inflate(inflater, container, false)
        subsribers = HashMap()
        adapter = NewsAdapter(this, this)
        layoutManager = binding.rec.recyclerView.layoutManager as LinearLayoutManager
        binding.rec.recyclerView.adapter = adapter
        prepareTransitions()
        //postponeEnterTransition()
        binding.rec.setOnRetryClickListener {
            if (choise == 0) {
                viewModel.exceptions.postValue(null)
                viewModel.state.postValue(RecycleViewModel.State.HOT)
            } else {
                viewModel.exceptions.postValue(null)
                viewModel.state.postValue(RecycleViewModel.State.NEW)
            }
        }
        return binding.root
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _kohii = KohiiProvider.get(requireContext())
        //scrollToPosition()
        bind()
        binding.rec.setOnRetryClickListener {
            invoke()
        }
        (activity as MainActivity).setBarVisible()
        if (savedInstanceState == null) {
            arguments?.takeIf { it.containsKey(ARG_TYPE) }?.apply {
                when (getInt(ARG_TYPE)) {
                    0 -> {
                        choise = 0
                        viewModel.state.postValue(RecycleViewModel.State.HOT)
                    }
                    1 -> {
                        choise = 1
                        viewModel.state.postValue(RecycleViewModel.State.NEW)
                    }
                }
            }
        }

        manager =
            kohiiM.register(this, memoryMode = MemoryMode.HIGH).addBucket(binding.rec.recyclerView)

        binding.rec.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            val PERCENT_SHOW = 80
            val PERCENT_HIDE = 50
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastIndex = layoutManager.findLastVisibleItemPosition()
                if (adapter.getLink(lastIndex) is Link.LoadingLink && adapter.itemCount > 0) {
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
                            if (subsribers?.containsKey(index) == true) {
                                val rvRect = Rect()
                                binding.rec.recyclerView.getLocalVisibleRect(rvRect)
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
                                    val holder = subsribers!![index]
                                    holder?.initYoutube(getYoutubePlayer()) {
                                        findNavController().navigate(it)
                                    }
                                } else if (percent < PERCENT_HIDE) {
                                    val holder = subsribers!![index]
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

    internal fun selectRebinder(
        position: Int,
        rebinder: Rebinder
    ) {
        viewModel.setRebinder(position to rebinder)
    }

    internal fun deselectRebinder() {
        viewModel.setRebinder(-1 to null)
    }

    override fun onResume() {
        super.onResume()
        val index = requireArguments().getString("index")?.toInt()
        if (index != null) {
            adapter.notifyItemChanged(index)
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    fun bind() {
        viewModel.apply {
            exceptions.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it is MainRepository.TokenISInvalidException) {
                        (activity as MainActivity).onTokenExpired(this@RecycleFragment)
                        return@observe
                    }
                    binding.rec.showErrorView(it.message)
                }
            }
            state.observe(viewLifecycleOwner) {
                when (it!!) {
                    RecycleViewModel.State.NEW -> {
                        binding.rec.visibility = View.INVISIBLE
                        binding.progress.root.visibility = View.VISIBLE
                        binding.progress.progressL.max = 3f
                        binding.progress.progressL.progress = 0f
                        viewModel.getSubredditNew(null, null, null, binding.progress.progressL)
                    }
                    RecycleViewModel.State.ERROR -> {
                        binding.rec.showErrorView(viewModel.exceptions.value?.message)
                    }
                    RecycleViewModel.State.HOT -> {
                        binding.rec.visibility = View.INVISIBLE
                        binding.progress.root.visibility = View.VISIBLE
                        binding.progress.progressL.max = 3f
                        binding.progress.progressL.progress = 0f
                        viewModel.getSubredditHot(null, null, null, binding.progress.progressL)
                    }
                    RecycleViewModel.State.INIT -> {

                    }
                    RecycleViewModel.State.HOT_LOADED -> {
                        binding.progress.root.visibility = View.INVISIBLE
                        binding.rec.visibility = View.VISIBLE
                        binding.rec.apply {
                            alpha = 0f
                            animate().alpha(1f).setDuration(200).start()
                        }
                    }
                    RecycleViewModel.State.NEW_LOADED -> {
                        binding.progress.root.visibility = View.INVISIBLE
                        binding.rec.visibility = View.VISIBLE
                        binding.rec.apply {
                            alpha = 0f
                            animate().alpha(1f).setDuration(200).start()
                        }
                    }
                }

            }
            recyclerViewVolume.observe(viewLifecycleOwner) {
                manager.applyVolumeInfo(it, binding.rec.recyclerView, Scope.BUCKET)
            }
            rebinder.observe(viewLifecycleOwner) {
                selection = it
            }
            links.observe(viewLifecycleOwner) {
                adapter.addLinks(it)
                loading = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deselectRebinder()
        _binding = null

    }

    private var playback: Playback? = null
    private var selection by Delegates.observable<Pair<Int, Rebinder?>>(
        initialValue = -1 to null,
        onChange = { _, from, to ->
            if (from == to) return@observable
            val (oldPos, oldRebinder) = from
            val (newPos, newRebinder) = to
            if (newRebinder != null) {
                pos = newPos
                val options = activity?.let {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        it,
                        androidx.core.util.Pair.create(view, "full_screen")
                    )
                }
                val extras = ActivityNavigatorExtras(options)
                val bundle = Bundle()
                bundle.putParcelable("rebinder", newRebinder)
                bundle.putInt("position", newPos)
                val link = (binding.rec.recyclerView.adapter as NewsAdapter).getItemLink(newPos)
                bundle.putString("link", link)
                findNavController().navigate(
                    R.id.action_newsFragment_to_fullScreenFragment,
                    args = bundle,
                    null,
                    extras
                )
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

    fun getYoutubePlayer(): YouTubePlayerView {
        val youTubePlayerView = YouTubePlayerView(requireContext())
        youTubePlayerView.enableAutomaticInitialization = false
        return youTubePlayerView
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        subsribers?.forEach {
            it.value.onDetached()
        }
    }

    override fun startLoad(): Boolean {
        if (!loading) {
            loading = true
            val last = viewModel.links.value?.get(viewModel.links.value!!.lastIndex) as Created
            Log.d(viewModel.state.value.toString(), last.toString())
            if (viewModel.state.value == RecycleViewModel.State.NEW_LOADED) {
                viewModel.getSubredditNew(null, null, last.getIds(), binding.progress.progressL)
            } else if (viewModel.state.value == RecycleViewModel.State.HOT_LOADED) {
                viewModel.getSubredditHot(null, null, last.getIds(), binding.progress.progressL)
            }
        }
        return false
    }

    override fun onClick(view: View, item: Link) {
        val link = item as Link.LinkOut
        val direction =
            NewsFragmentDirections.actionNewsFragmentToDetailLinkFragment(link.permalink, null)
        findNavController().navigate(direction)
    }

    override fun onPict(view: View, link: String) {
        view.transitionName = "pager_small"
        val extras = FragmentNavigatorExtras(view to "pager")
        val bundle = Bundle()
        bundle.putString("link", link)
        findNavController().navigate(
            R.id.action_newsFragment_to_detainFragment,
            args = bundle,
            null,
            extras
        )
    }

    override fun onText(view: View, link: String) {

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
        subsribers?.set(postion, holder)
    }

    override fun removeSubscription(index: Int) {
        subsribers?.remove(index)
    }

    override fun getScope(): CoroutineScope {
        return lifecycleScope
    }

    override fun onJoin(view: MControllerView, subredditName: String, textView: MTextView) {
        if (view.state == MControllerView.State.SELECTED) {
            lifecycleScope.launch {
                when (viewModel.subscribe(
                    RedditApi.SubscibeType.unsub,
                    null,
                    subredditName
                )) {
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
                when (viewModel.subscribe(
                    RedditApi.SubscibeType.sub,
                    true,
                    subredditName
                )) {
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

    override fun navigateToUser(user: String) {
        val direction = MainNavGraphDirections.actionGlobalProfileGrapth(user)
        findNavController().navigate(direction)
    }

    override fun onPict2(extras: FragmentNavigator.Extras, link: String) {
        val bundle = Bundle()
        bundle.putString("link", link)
        findNavController().navigate(
            R.id.action_newsFragment_to_detainFragment,
            args = bundle,
            null,
            extras
        )
    }

    override fun onText2(view: View, link: Link.LinkText) {
        (exitTransition as TransitionSet?)?.excludeTarget(view, true)
        val transitioningView = view.findViewById<TextView>(R.id.text_view)
        val content = view.findViewById<TextView>(R.id.content)
        startPostponedEnterTransition()
        val extras =
            FragmentNavigatorExtras(
                transitioningView to "title",
                content to "contentMain"
            )
        findNavController().navigate(
            NewsFragmentDirections.actionNewsFragmentToDetailTextFragment(
                link.permalink,
                link.title,
                link.selftext
            ), extras
        )
    }

    override fun onYoutube(link: String, id: String, second: Long) {
        NewsFragmentDirections.actionNewsFragmentToYoutubeFragment(
            link = link,
            time = second,
            id = id
        )
    }

    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.exit_transition)
    }

    override fun invoke() {
        if (choise == 0) {
            viewModel.exceptions.postValue(null)
            viewModel.state.postValue(RecycleViewModel.State.HOT)
        } else {
            viewModel.exceptions.postValue(null)
            viewModel.state.postValue(RecycleViewModel.State.NEW)
        }
    }
}