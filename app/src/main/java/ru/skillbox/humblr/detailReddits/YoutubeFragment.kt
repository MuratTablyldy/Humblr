package ru.skillbox.humblr.detailReddits

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.DefaultPlayerUiController
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import ru.skillbox.humblr.MainNavGraphDirections
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.*
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.databinding.FullScreenLayoutMBinding
import ru.skillbox.humblr.databinding.FullscreenYoutubeBinding
import ru.skillbox.humblr.databinding.LoadingViewPrevBinding
import ru.skillbox.humblr.databinding.WriteCommentLayoutBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.*
import ru.skillbox.humblr.utils.adapters.CommentAdapter
import ru.skillbox.humblr.utils.adapters.CommentsDelegateAdapter

@AndroidEntryPoint
class YoutubeFragment : Fragment(), CommentAdapter.CommentHandler,CallBack {
    private lateinit var scene: Scene
    private lateinit var scene2: Scene
    var land: Boolean = false
    var isFullscreen = false
    private var _binding2: WriteCommentLayoutBinding? = null
    private val binding2: WriteCommentLayoutBinding
        get() = _binding2!!
    private var _binding: FullscreenYoutubeBinding? = null
    val binding: FullscreenYoutubeBinding
        get() = _binding!!
    lateinit var adapter: CommentsDelegateAdapter
    val viewModel: YoutubeViewModel by viewModels()
    private var couldScroll = false
    private val args: YoutubeFragmentArgs by navArgs()
    private var _bindingMain: FullScreenLayoutMBinding? = null
    private val fadeTransition: Transition = Fade()
    private val bindingMain: FullScreenLayoutMBinding
        get() = _bindingMain!!
    private var _bindingLoad: LoadingViewPrevBinding? = null
    private val bindingLoad: LoadingViewPrevBinding
        get() = _bindingLoad!!
    private lateinit var screneLoading: Scene


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFullscreen) {
                binding.playerView.toggleFullScreen()
            } else {
                activity?.onBackPressed()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Com.Companion.NullComment.setPagesCount(0)
        Com.Companion.NullComment.pages = emptyList()
        land = resources.getBoolean(R.bool.land)
        _bindingLoad = LoadingViewPrevBinding.inflate(inflater, container, false)
        _binding2 = WriteCommentLayoutBinding.inflate(inflater, container, false)
        _binding = FullscreenYoutubeBinding.inflate(inflater, container, false)
        _bindingMain = FullScreenLayoutMBinding.inflate(inflater, container, false)
        scene = Scene(bindingMain.root, binding2.root)
        scene2 = Scene(bindingMain.root, binding.root)
        screneLoading = Scene(bindingMain.root, bindingLoad.root)
        if (!land && !viewModel.fullscreenTransition) {
            TransitionManager.go(screneLoading, fadeTransition)
        } else {
            TransitionManager.go(scene2)
        }
        binding.voteNumber?.setCharacterLists(TickerUtils.provideNumberList())
        binding.commentNumber?.setCharacterLists(TickerUtils.provideNumberList())
        return _bindingMain!!.root
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(onBackPressedCallback)
        adapter = CommentsDelegateAdapter(lifecycleScope, this)
        binding.rec?.recyclerView?.adapter = adapter
        bindingLoad.progressL.max = 3f
        bind()
        binding.playerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                val defaultPlayerUiController =
                    DefaultPlayerUiController(binding.playerView, youTubePlayer)
                val muteButton = MButton(binding.playerView.context)
                muteButton.setOnClickListener {
                    if (muteButton.isOff) {
                        youTubePlayer.unMute()
                        muteButton.isOff = false
                    } else {
                        muteButton.isOff = true
                        youTubePlayer.mute()
                    }
                }
                val fullScreenButton =
                    defaultPlayerUiController.rootView.findViewById<ImageView>(R.id.fullscreen_button)
                if (land) {
                    binding.playerView.enterFullScreen()

                    fullScreenButton.setImageDrawable(
                        AppCompatResources.getDrawable(
                            fullScreenButton.context,
                            R.drawable.ic_baseline_fullscreen_exit_24
                        )
                    )
                    (activity as MainActivity).setBarNoVisibility()
                } else {
                    fullScreenButton.setImageDrawable(
                        AppCompatResources.getDrawable(
                            fullScreenButton.context,
                            R.drawable.ic_baseline_fullscreen_24
                        )
                    )
                }
                fullScreenButton.setOnClickListener {
                    if (binding.playerView.isFullScreen()) {
                        fullScreenButton.setImageDrawable(
                            AppCompatResources.getDrawable(
                                fullScreenButton.context,
                                R.drawable.ic_baseline_fullscreen_24
                            )
                        )
                        binding.playerView.exitFullScreen()
                        binding.playerView.toggleFullScreen()
                        requireActivity().requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        land = true
                    } else {
                        binding.playerView.toggleFullScreen()
                        binding.playerView.enterFullScreen()
                        requireActivity().requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        land = true
                        viewModel.fullscreenTransition = true
                    }
                }
                val newId = View.generateViewId()
                muteButton.id = newId
                defaultPlayerUiController.addView(muteButton)
                binding.playerView.setCustomPlayerUi(defaultPlayerUiController.rootView)
                val youtubeId = args.id
                if (viewModel.time == 0f) {
                    youTubePlayer.loadVideo(youtubeId, args.time.toFloat())
                } else {
                    youTubePlayer.loadVideo(youtubeId, viewModel.time)
                }
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                viewModel.time = second

            }
        }, IFramePlayerOptions.Builder().controls(0).build())
        initialize()
    }

    private fun initialize() {
        binding.userName?.setOnClickListener {
            val autor = viewModel.linkItem.value?.author
            if (autor != null) {
                val direction = MainNavGraphDirections.actionGlobalProfileGrapth(autor)
                findNavController().navigate(direction)
            }
        }
        binding.back?.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.save?.onClickListener {
            if (it == MControllerView.State.RELEASED) {
                lifecycleScope.launch {
                    val result = viewModel.save(viewModel.linkItem.value?.name!!, "link")
                    if (!result) {
                        binding.save?.state = MControllerView.State.RELEASED
                    } else {
                        binding.save?.state = MControllerView.State.SELECTED
                    }
                }
            } else {
                lifecycleScope.launch {
                    val result = viewModel.unsave(viewModel.linkItem.value?.name!!)
                    if (!result) {
                        binding.save?.state = MControllerView.State.SELECTED
                    } else {
                        binding.save?.state = MControllerView.State.RELEASED
                    }
                }
            }
        }
        binding.downVote?.setOnClick(object : MFloatingActionButton.OnClick {
            override fun onClick(state: MFloatingActionButton.State) {
                val name = (viewModel.linkItem.value as Link.LinkYouTube).name
                if (state == MFloatingActionButton.State.CLICKED) {
                    if (name != null) {
                        lifecycleScope.launch {
                            when (viewModel.vote(-1, name, null)) {
                                is Result.Success -> {
                                    val numberText = binding.voteNumber!!.text
                                    if (!numberText.contains("[km]".toRegex())) {
                                        var number = numberText.toString().toInt()
                                        binding.voteNumber?.text = "${--number}"
                                    }
                                }
                                is Result.Error -> {
                                    binding.downVote!!.state = MFloatingActionButton.State.RELEASED
                                }
                            }
                        }

                    }

                } else {
                    if (name != null) {
                        lifecycleScope.launch {
                            when (viewModel.vote(0, name, null)) {
                                is Result.Success -> {
                                    val numberText = binding.voteNumber!!.text
                                    if (!numberText.contains("[km]".toRegex())) {
                                        var number = numberText.toString().toInt()
                                        binding.voteNumber!!.text = "${++number}"
                                    }
                                }
                                is Result.Error -> {
                                    binding.downVote?.state = MFloatingActionButton.State.CLICKED
                                }
                            }
                        }

                    }
                }
            }
        })
        binding.join?.onClickListener {
            if (binding.join?.state == MControllerView.State.SELECTED) {
                lifecycleScope.launch {
                    val name = viewModel.subInfo.value?.name
                    if (name != null) {
                        when (viewModel.subscribe(RedditApi.SubscibeType.unsub, null, name)) {
                            is Result.Success -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Вы отписались от ${viewModel.subInfo.value!!.displayName}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is Result.Error -> {
                                binding.join?.changeState(MControllerView.State.SELECTED)
                            }
                        }
                    }

                }

            } else if (binding.join?.state == MControllerView.State.RELEASED) {
                lifecycleScope.launch {
                    val name = viewModel.subInfo.value?.name
                    if (name != null) {
                        when (viewModel.subscribe(RedditApi.SubscibeType.sub, true, name)) {
                            is Result.Success -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Вы подписались на ${viewModel.subInfo.value!!.displayName}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is Result.Error -> {
                                binding.join?.changeState(MControllerView.State.RELEASED)
                            }
                        }
                    }
                }
            }
        }
        binding.share?.setOnClickListener {
            val url = if (viewModel.linkItem.value?.url?.contains("http") == false) {
                "https://${viewModel.linkItem.value?.url}"
            } else viewModel.linkItem.value?.url
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, Uri.parse(url))
            requireContext().startActivity(
                Intent.createChooser(
                    intent,
                    viewModel.linkItem.value?.title
                )
            )
        }
        binding.upVote?.setOnClick(object : MFloatingActionButton.OnClick {
            override fun onClick(state: MFloatingActionButton.State) {
                val name = (viewModel.linkItem.value as Link.LinkYouTube).name
                if (state == MFloatingActionButton.State.CLICKED) {
                    if (name != null) {
                        lifecycleScope.launch {
                            when (viewModel.vote(1, name, null)) {
                                is Result.Success -> {
                                    val numberText = binding.voteNumber?.text
                                    if (!numberText!!.contains("[km]".toRegex())) {
                                        var number = numberText.toString().toInt()
                                        binding.voteNumber?.text = "${++number}"
                                    }
                                }
                                is Result.Error -> {
                                    binding.upVote!!.state = MFloatingActionButton.State.RELEASED
                                }
                            }
                        }
                    }
                } else {
                    if (name != null) {
                        lifecycleScope.launch {
                            when (viewModel.vote(0, name, null)) {
                                is Result.Success -> {
                                    val numberText = binding.voteNumber?.text
                                    if (!numberText!!.contains("[km]".toRegex())) {
                                        var number = numberText.toString().toInt()
                                        binding.voteNumber?.text = "${--number}"
                                    }
                                }
                                is Result.Error -> {
                                    binding.upVote!!.state = MFloatingActionButton.State.CLICKED
                                }
                            }
                        }
                    }
                }
            }
        })
        binding2.exit.setOnClickListener {
            binding2.editor.html = ""
            TransitionManager.go(scene2)
        }
        binding2.editor.setPlaceholder("What are you thoughts?")
        binding2.actionItalic.setonClick {
            binding2.editor.setItalic()
        }
        binding2.actionBold.setonClick {
            binding2.editor.setBold()
        }
        binding2.actionUnderline.setonClick {
            binding2.editor.setUnderline()
        }
        binding2.actionAlignCenter.setonClick {
            binding2.editor.setAlignCenter()
        }
        binding2.actionAlignLeft.setonClick {
            binding2.editor.setAlignLeft()
        }
        binding2.actionAlignRight.setonClick {
            binding2.editor.setAlignRight()
        }
        binding2.actionIndent.setonClick {
            binding2.editor.setIndent()
        }
        binding2.actionOutdent.setonClick {
            binding2.editor.setOutdent()
        }
        binding2.actionStrikethrough.setonClick {
            binding2.editor.setStrikeThrough()
        }
        binding2.actionBulet.setonClick {
            binding2.editor.setBullets()
        }
        binding2.actionUndo.setOnClickListener {
            binding2.editor.undo()
        }
        binding2.actionRedo.setOnClickListener {
            binding2.editor.redo()
        }
        binding.rec?.setOnRetryClickListener {
            retry()
        }
        binding.commentButton?.setOnClickListener {
            if (binding.commentButton?.isExtended == true) {
                viewModel.state.postValue(YoutubeViewModel.State.EXPANDED)
            } else {
                prepareWriteComment()
            }
        }
        binding.writeComment?.setOnClickListener {
            prepareWriteComment()
        }
    }

    private fun retry() {
        viewModel.exceptions.postValue(null)
        viewModel.state.postValue(YoutubeViewModel.State.INIT)
    }

    fun prepareWriteComment() {
        TransitionManager.go(scene)
        val link =
            viewModel.linkItem.value
        binding2.editor.focusEditor()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding2.editor, InputMethodManager.SHOW_IMPLICIT)
        binding2.title.text = link?.title
        val instant = link?.createdUTC?.let {
            Instant.ofEpochSecond(it)
        }
        val res = binding2.userName.context.resources
        val prefix =
            if (link?.edited != null) res.getString(R.string.edited) else {
                ""
            }
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        when {
            duration.toDays() > 0 -> {
                binding2.userName.text =
                    String.format(
                        res.getString(R.string.days_ago2),
                        link?.author,
                        prefix,
                        duration.toDays()
                    )
            }

            duration.toHours() > 0 -> {
                if (link != null) {
                    binding2.userName.text =
                        String.format(
                            res.getString(R.string.hours_ago2),
                            link.author,
                            prefix,
                            duration.toHours()
                        )
                }
            }
            duration.toMinutes() > 0 -> {
                binding2.userName.text =
                    String.format(
                        res.getString(R.string.minutes_ago2),
                        link?.author,
                        prefix,
                        duration.toMinutes()
                    )

            }
            else -> {
                binding2.userName.text =
                    String.format(
                        res.getString(R.string.now2),
                        link?.author,
                        prefix,
                        duration.toDays()
                    )

            }
        }
        binding2.post.setOnClickListener {
            val linkq = viewModel.linkItem.value
            if (binding2.editor.html == null || binding2.editor.html!!.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Nothing to post because message is empty",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val comment = viewModel.postComment(linkq!!.name!!, binding2.editor.html!!)
                if (comment != null && viewModel.pageList.value != null) {
                    val me = viewModel.me.value
                    comment.account = UserInfo(me?.name!!, me.icon!!)
                    val list = viewModel.pageList.value!! + (comment)
                    if (list.size > 2) {
                        adapter.setPage(list, false)
                    } else {
                        adapter.setPage(list, true)
                    }

                    binding2.editor.html = ""
                    TransitionManager.go(scene2)
                    binding.rec?.recyclerView?.scrollToPosition(list.lastIndex)
                } else {
                    if (comment != null) {
                        val me = viewModel.me.value
                        comment.account = UserInfo(me?.name!!, me.icon!!)
                        val list = mutableListOf(comment)
                        viewModel.pageList.postValue(list)
                        if (list.size > 2) {
                            adapter.setPage(list, false)
                        } else {
                            adapter.setPage(list, true)
                        }
                        binding2.editor.html = ""
                        TransitionManager.go(scene2)
                        binding.rec?.recyclerView?.scrollToPosition(list.lastIndex)
                    } else{
                        TransitionManager.go(scene2)
                        Toast.makeText(requireContext(),"something went wrong, try later",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun getComments(link: String) {
        val status = (activity as MainActivity).internetStatus()
        if (status) {
            binding.rec?.showLoadingView()
            viewModel.getComments(
                link = link,
                commentID = null,
                context = null,
                depth = null,
                limit = null,
                showedits = true,
                showmedia = true,
                showmore = true,
                showtitle = true,
                sort = "top",
                threaded = true,
                onEmpty = {
                    Handler(Looper.getMainLooper()).post {
                        binding.rec?.showEmptyView(resources.getString(R.string.empty))
                        binding.commentButton?.show()
                        binding.commentButton?.shrink()
                    }
                })
        }

    }

    override fun getPage(index: Int, preview: Boolean) {
        if (viewModel.state.value != YoutubeViewModel.State.LOADING_PREVIEW && viewModel.state.value != YoutubeViewModel.State.LOADING_ACCOUNT) {
            if (preview) {
                viewModel.state.postValue(YoutubeViewModel.State.LOADING_PREVIEW)
            } else {
                viewModel.currentPage = index
                viewModel.state.postValue(YoutubeViewModel.State.LOADING_ACCOUNT)
            }
            viewModel.viewModelScope.launch {
                binding.rec?.showLoadingView()
                couldScroll = false
                binding.rec?.recyclerView?.visibility = View.INVISIBLE
                val currentIndex = (index - 1) * 10
                var toIndex = if (preview) {
                    0
                } else {
                    currentIndex + 9
                }
                val size = viewModel.comments.value?.size
                toIndex =
                    if (toIndex >= size!!)
                        size - 1 else toIndex
                val list = mutableListOf<Comment>()
                val comments = viewModel.comments.value
                for (i in currentIndex..toIndex) {
                    comments?.get(i)?.let { list.add(it) }
                }
                if (list.isEmpty()) {
                    if (preview) {
                        viewModel.state.postValue(YoutubeViewModel.State.LOADED_PREVIEW)
                    } else {
                        viewModel.state.postValue(YoutubeViewModel.State.LOADED_ACCOUNT)
                    }
                    return@launch
                }
                if (list.last().account == null) {
                    getCommentsUsers(list)
                    if (preview) {
                        viewModel.state.postValue(YoutubeViewModel.State.LOADED_PREVIEW)
                    } else {
                        viewModel.state.postValue(YoutubeViewModel.State.LOADED_ACCOUNT)
                    }
                    viewModel.pageList.postValue(list)
                } else {
                    if (preview) {
                        viewModel.state.postValue(YoutubeViewModel.State.LOADED_PREVIEW)
                    } else {
                        viewModel.state.postValue(YoutubeViewModel.State.LOADED_ACCOUNT)
                    }
                    viewModel.pageList.postValue(list)
                }
            }
        }
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
        binding.writeComment?.visibility = View.VISIBLE
        binding.commentButton?.visibility = View.VISIBLE
        binding.commentButton?.extend()
    }

    override fun writeComment(
        view: View,
        parent: ViewGroup,
        comment: Comment,
        root: Boolean,
        depth: Int
    ) {
        TransitionManager.go(scene)

        val link =
            viewModel.linkItem.value as Link.LinkYouTube
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding2.editor, InputMethodManager.SHOW_IMPLICIT)
        binding2.title.text =
            comment.body?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
        val instant = link.createdUTC.let { Instant.ofEpochSecond(it) }
        val res = binding2.userName.context.resources
        val prefix =
            if (comment.edited != null) res.getString(R.string.edited) else {
                ""
            }

        val now = Instant.now()
        val duration = Duration.between(instant, now)
        val pref = "$${comment.author} $prefix"
        when {
            duration.toDays() > 0 -> {
                binding2.userName.text =
                    String.format(
                        res.getString(R.string.days_ago),
                        pref,
                        duration.toDays()
                    )

            }
            duration.toHours() > 0 -> {
                binding2.userName.text =
                    String.format(
                        res.getString(R.string.hours_ago),
                        pref,
                        duration.toDays()
                    )

            }
            duration.toMinutes() > 0 -> {
                binding2.userName.text =
                    String.format(
                        res.getString(R.string.minutes_ago),
                        pref,
                        duration.toDays()
                    )
            }
            else -> {
                binding2.userName.text =
                    String.format(
                        res.getString(R.string.now),
                        pref,
                        duration.toDays()
                    )
            }
        }
        binding2.post.setOnClickListener {
            if (binding2.editor.html == null || binding2.editor.html!!.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Nothing to post because message is empty",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val commentR = viewModel.postComment(comment.name!!, binding2.editor.html!!)
                if (commentR != null && viewModel.pageList.value != null) {
                    val me = viewModel.me.value
                    commentR.account = UserInfo(me?.name!!, me.icon!!)
                    if (root) {
                        val group = parent as LinearLayoutCompat
                        val layout = LayoutInflater.from(group.context)
                            .inflate(R.layout.comment_view_reply, group, false)
                        bind(layout, commentR)
                        group.addView(layout, 0)
                        binding2.editor.html = ""
                        TransitionManager.go(scene2)
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
                        binding2.editor.html = ""
                        TransitionManager.go(scene2)
                    }
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

    private suspend fun getCommentsUsers(things: MutableList<Comment>) {
        val list = mutableListOf<Comment>()
        val tasks = mutableListOf<Thing2>()

        for (comment in things) {
            if (comment.replies.data.children.isNullOrEmpty()) {
                list.add(comment)
            } else {
                tasks.add(Thing2(null, null, "comment", comment))
            }
        }
        while (tasks.isNotEmpty()) {
            val thing = tasks.first()
            tasks.remove(thing)
            if (thing.data is More) {
                val comment = thing.data.parent
                val child = comment?.replies?.data?.children as MutableList<Thing2>
                child.remove(thing)
                if (!thing.data.children.isNullOrEmpty()) {
                    val ids = thing.data.children.reduce { first, second -> "$first,$second" }
                    val children = viewModel.getChildren(
                        null,
                        null,
                        ids,
                        false,
                        (viewModel.linkItem.value as Link.LinkYouTube).name!!,
                        "top",
                        "json"
                    )?.data?.data?.things
                    if (children != null) {
                        for (chil in children) {
                            when (chil.data) {
                                is Comment -> {
                                    child.add(chil)
                                    tasks.add(chil)
                                }
                                is More -> {
                                    tasks.add(chil)
                                    chil.data.parent = comment
                                }
                            }
                        }
                    }
                }
                continue
            }
            list.add(thing.data as Comment)
            val parents = thing.data.replies.data.children
            for (thin in parents!!) {
                when (thin.data) {
                    is Comment -> {
                        list.add(thin.data)
                        if (!thin.data.replies.data.children.isNullOrEmpty()) {
                            tasks.add(thin)
                        }
                    }
                    is More -> {
                        thin.data.parent = thing.data
                        tasks.add(thin)
                    }
                }
            }
        }
        val accounts = getAccounts(list)
        for (comment in list) {
            val info = accounts[comment.autorName]
            comment.account = info
        }
    }

    suspend fun getAccounts(list: List<Comment>): HashMap<String, UserInfo> {
        if (list.isEmpty()) {
            return HashMap()
        }
        val size = list.size
        if (size > 200) {
            val pages = size / 200
            val remainder = size % 200
            var from = 0
            var to = 199
            val result = HashMap<String, UserInfo>()
            for (i in 0..pages) {
                val sub = list.subList(from, to)
                val ids = sub.map { comment -> comment.autorName }
                    .reduce { first, second -> "$first,$second" }
                result += viewModel.getAccounts(ids!!)!!.users
                from += 200
                val prevto = to
                to = if (prevto + 200 < size) to + 200 else to + remainder
            }
            return result
        } else {
            val ids = list.map { comment -> comment.autorName }
                .reduce { first, second -> "$first,$second" }
            return viewModel.getAccounts(ids!!)!!.users
        }
    }

    fun bind(root: View, comment: Comment) {
        lifecycleScope.launch {
            val icon = comment.account?.profileImg
            Glide.with(root).load(icon)
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

    @OptIn(InternalCoroutinesApi::class)
    @FlowPreview
    fun bind() {
        viewModel.apply {
            linkItem.observe(viewLifecycleOwner) {
                if (it != null) {
                    bindingLoad.progressL.progress = 1f
                    if (!land) {
                        binding.upVote?.isEnabled = true
                        binding.downVote?.isEnabled = true
                        lifecycleScope.launch {
                            bindingLoad.progressL.progress = 2f
                            binding.commentNumber?.text = it.numComments
                            binding.voteNumber?.text = (it.ups - it.downs).toString()
                            binding.userName?.text = it.author
                            binding.redditName?.text = it.subreddit
                            binding.title?.text = it.title
                            if (viewModel.info.value == null) {
                                val info = it.author?.let { it1 ->
                                    viewModel.getInfo(it1)
                                }
                                when (info) {
                                    is Result.Success -> {
                                        viewModel.info.postValue(info.data.data)
                                    }
                                    is Result.Error -> {
                                        viewModel.exceptions.postValue(info.exception)
                                    }
                                    else -> {
                                        exceptions.postValue(java.lang.NullPointerException())
                                    }
                                }

                            }
                            if (viewModel.subInfo.value == null) {
                                when (val subinfo = viewModel.getSubredditAbout(it.subreddit)) {
                                    is Result.Success<Thing<SubredditInfo>> -> {
                                        viewModel.subInfo.postValue(subinfo.data.data)
                                    }
                                    is Result.Error -> {
                                    }
                                }
                            }
                        }
                    }
                }
            }
            subInfo.observe(viewLifecycleOwner) { subInfo ->
                if (subInfo != null) {
                    if (!land) {
                        bindingLoad.progressL.progress = 3f
                        TransitionManager.go(scene2, fadeTransition)
                        if (subInfo.userIsSubscriber == true) {
                            binding.join?.changeState(MControllerView.State.SELECTED)
                        }
                    }
                }

            }
            info.observe(viewLifecycleOwner) { info ->
                if (info != null) {
                    val iconLink = info.icon?.replace("amp;", "")
                    binding.avatarView?.let { it1 ->
                        Glide.with(requireContext()).load(iconLink).into(it1)
                            .onLoadFailed(
                                AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.empty
                                )
                            )
                    }
                }
            }
        }

        viewModel.exceptions.observe(viewLifecycleOwner) {
            if (it != null) {
                if(it is MainRepository.TokenISInvalidException){
                    (activity as MainActivity).onTokenExpired(this)
                    return@observe
                }
                viewModel.state.postValue(YoutubeViewModel.State.ERROR)
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                YoutubeViewModel.State.INIT -> {
                    binding.rec?.showLoadingView()
                    binding.writeComment?.hide()
                    binding.commentButton?.hide()
                    binding.rec?.recyclerView?.visibility = View.INVISIBLE
                    getComments(args.link)
                    viewModel.getMe()
                    viewModel.state.postValue(YoutubeViewModel.State.LOADING)
                }
                YoutubeViewModel.State.LOADING -> {
                    binding.rec?.showLoadingView()
                    binding.writeComment?.hide()
                    binding.commentButton?.hide()
                }
                YoutubeViewModel.State.PREVIEW -> {
                    adapter.initPreview()
                    binding.rec?.showLoadingView()
                    binding.writeComment?.hide()
                    binding.commentButton?.hide()
                }
                YoutubeViewModel.State.EXPANDED -> {
                    adapter.initFirst()
                    binding.rec?.showLoadingView()
                    binding.writeComment?.hide()
                    binding.commentButton?.hide()
                }
                YoutubeViewModel.State.LOADING_ACCOUNT -> {
                    binding.writeComment?.hide()
                    binding.commentButton?.hide()
                    binding.rec?.showLoadingView()
                }
                YoutubeViewModel.State.LOADING_PREVIEW -> {
                    binding.rec?.showLoadingView()
                    binding.writeComment?.hide()
                    binding.commentButton?.hide()
                }
                YoutubeViewModel.State.ERROR -> {
                    binding.rec?.showErrorView(viewModel.exceptions.value?.message)
                    binding.writeComment?.hide()
                    binding.commentButton?.hide()
                }
                YoutubeViewModel.State.LOADED_PREVIEW -> {
                    if (viewModel.pageList.value.isNullOrEmpty()) {
                        binding.rec?.showEmptyView(resources.getString(R.string.empty))
                        binding.commentButton?.show()
                        binding.commentButton?.shrink()
                        binding.writeComment?.hide()
                    } else {
                        binding.rec?.hideAllViews()
                        binding.rec?.recyclerView?.visibility = View.VISIBLE
                        couldScroll = true
                        binding.writeComment?.show()
                        binding.commentButton?.show()
                        binding.commentButton?.extend()
                    }

                }
                YoutubeViewModel.State.LOADED_ACCOUNT -> {
                    if (viewModel.pageList.value.isNullOrEmpty()) {
                        binding.rec?.showEmptyView(resources.getString(R.string.empty))
                        binding.commentButton?.show()
                        binding.commentButton?.shrink()
                        binding.writeComment?.hide()
                    } else {
                        binding.rec?.hideAllViews()
                        binding.rec?.recyclerView?.visibility = View.VISIBLE
                        binding.writeComment?.visibility = View.GONE
                        binding.writeComment?.hide()
                        binding.commentButton?.show()
                        binding.commentButton?.shrink()
                        couldScroll = true
                    }
                }
            }
        }
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            if (comments != null) {
                if (comments.isEmpty()) {
                    binding.rec?.showEmptyView()
                }
                if (viewModel.state.value == YoutubeViewModel.State.LOADING) {
                    viewModel.state.postValue(YoutubeViewModel.State.PREVIEW)
                }
            }
        }
        viewModel.pageList.observe(viewLifecycleOwner) {
            if (it != null) {
                if (viewModel.state.value == YoutubeViewModel.State.LOADED_ACCOUNT ||
                    viewModel.state.value == YoutubeViewModel.State.LOADED_PREVIEW
                ) {
                    binding.rec?.hideAllViews()
                    binding.rec?.recyclerView?.visibility = View.VISIBLE

                    if (viewModel.state.value == YoutubeViewModel.State.LOADED_PREVIEW) {
                        adapter.setPage(it, false)
                    } else {
                        val size = viewModel.comments.value!!.size
                        var pages = size / 10
                        val remainder = size % 10
                        if (remainder != 0) {
                            pages++
                        }
                        adapter.setPagesCount(pages)
                        Com.Companion.NullComment.setCurrentPage(viewModel.currentPage)
                        adapter.setPage(it, false)
                    }
                    binding.rec?.recyclerView?.scrollToPosition(0)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.playerView.release()
        _binding = null
        _binding2 = null
        onBackPressedCallback.remove()
    }

    override fun invoke() {
        viewModel.state.postValue(YoutubeViewModel.State.INIT)
        viewModel.exceptions.postValue(null)
    }

}