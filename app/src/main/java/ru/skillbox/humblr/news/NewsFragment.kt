package ru.skillbox.humblr.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kohii.v1.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.databinding.NewsFragmentBinding
import ru.skillbox.humblr.databinding.SearchHelperBinding
import ru.skillbox.humblr.databinding.ViewPageBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.ZoomTransformer
import ru.skillbox.humblr.utils.adapters.RecyclePagerAdapter
import ru.skillbox.humblr.utils.adapters.SearchAdapter
import ru.skillbox.humblr.utils.subscribeFlow

@InternalCoroutinesApi
@AndroidEntryPoint
class NewsFragment : Fragment(R.layout.news_fragment),
    Manager.OnSelectionListener {

    private var _binding: NewsFragmentBinding? = null
    private val binding: NewsFragmentBinding
        get() = _binding!!
    private var _searchBinding: SearchHelperBinding? = null
    val searchBinding: SearchHelperBinding
        get() = _searchBinding!!
    val newsViewModel: NewsViewModel by viewModels()
    lateinit var scene: Scene
    lateinit var scene2: Scene
    private var adapter: RecyclePagerAdapter? = null
    var _pagerBinding: ViewPageBinding? = null
    val pagerBinding: ViewPageBinding
        get() = _pagerBinding!!
    private val slide: Transition = Slide()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewsFragmentBinding.inflate(inflater, container, false)
        val searchView = inflater.inflate(R.layout.search_helper, container, false)
        _searchBinding = SearchHelperBinding.bind(searchView)
        val view = inflater.inflate(R.layout.view_page, container, false)
        _pagerBinding = ViewPageBinding.bind(view)
        scene = Scene(binding.container, pagerBinding.root)
        scene2 = Scene(binding.container, searchBinding.root)
        TransitionManager.go(scene, slide)
        return _binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclePagerAdapter(this)
        pagerBinding.pager.adapter = adapter
        pagerBinding.pager.isUserInputEnabled = false
        pagerBinding.pager.offscreenPageLimit = 2
        pagerBinding.pager.setPageTransformer(ZoomTransformer())
        val searchAdapter = SearchAdapter { link ->
            when (link) {
                is Link.LinkText -> {
                    val direction =
                        NewsFragmentDirections.actionNewsFragmentToDetailTextFragment(link.permalink)
                    findNavController().navigate(direction)
                }
                is Link.LinkOut -> {
                    val direction =
                        NewsFragmentDirections.actionNewsFragmentToDetailLinkFragment(link.permalink)
                    findNavController().navigate(direction)
                }
                is Link.LinkPict -> {
                    val direction =
                        NewsFragmentDirections.actionNewsFragmentToDetainFragment(link.permalink)
                    findNavController().navigate(direction)
                }
                is Link.LinkYouTube -> {
                    val direction = NewsFragmentDirections.actionNewsFragmentToYoutubeFragment(
                        link.permalink,
                        0,
                        link.youtubeId!!
                    )
                    findNavController().navigate(direction)
                }
                is Link.LinkRedditVideo -> {
                    val reb = Rebinder("it")
                    reb.with {
                        controller = object : Playback.Controller {
                            override fun kohiiCanPause(): Boolean {
                                return true
                            }

                            override fun kohiiCanStart(): Boolean {
                                return true
                            }

                            override fun setupRenderer(playback: Playback, renderer: Any?) {
                                super.setupRenderer(playback, renderer)
                                if (renderer is PlayerView) {
                                    renderer.useController = true
                                }
                            }

                            override fun teardownRenderer(playback: Playback, renderer: Any?) {
                                super.teardownRenderer(playback, renderer)
                                if (renderer is PlayerView) {
                                    renderer.useController = false
                                }
                            }
                        }
                        repeatMode = Player.REPEAT_MODE_ALL
                        threshold = 1f
                    }
                    val direction = NewsFragmentDirections.actionNewsFragmentToFullScreenFragment(
                        reb,
                        0,
                        link.permalink
                    )
                    findNavController().navigate(direction)
                }
                else -> {
                    throw IllegalArgumentException("not supported")
                }
            }
        }
        searchBinding.listView.recyclerView.adapter = searchAdapter
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.searchList.collect { list ->
                    if (!list.isNullOrEmpty()) {
                        searchAdapter.setList(list)
                        TransitionManager.go(scene2, slide)
                        binding.group.visibility = View.GONE
                    } else {
                        TransitionManager.go(scene, slide)
                        binding.group.visibility = View.VISIBLE
                    }
                }
            }
        }
        bindFlow()
        binding.search.setOnCloseListener {
            binding.search.clearFocus()
            true
        }
        TabLayoutMediator(binding.group, pagerBinding.pager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = resources.getText(R.string.hot)
                }
                1 -> {
                    tab.text = resources.getText(R.string.newP)
                }
            }

        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        newsViewModel.denyJob()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun bindFlow() {
        val callBack = (activity as MainActivity).networkCallback
        newsViewModel.bind(getTitle(), callBack)
    }

    override fun onSelection(selection: Collection<Playback>) {
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun getTitle(): Flow<String> {
        return binding.search.subscribeFlow().mapLatest { it }
            .distinctUntilChanged()
            .debounce(500)
    }

    override fun onPause() {
        super.onPause()
        binding.search.setQuery("", false)
        binding.search.clearFocus()
    }

}
