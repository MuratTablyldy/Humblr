package ru.skillbox.humblr.news

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kohii.v1.core.*
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.entities.SubredditInfo
import ru.skillbox.humblr.data.entities.Thing
import ru.skillbox.humblr.data.interfaces.OnBottomSheetCallbacks
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.databinding.FullScreenMdBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.*


@InternalCoroutinesApi
@AndroidEntryPoint
class FullScreenFragment : Fragment(R.layout.full_screen_md) {
    val viewModel: FullScreenModel by viewModels()
    private var _binding: FullScreenMdBinding? = null
    var onLink: OnLink? = null
    lateinit var manager: Manager
    lateinit var kohii: Kohii
    private var listener: OnBottomSheetCallbacks? = null
    val binding: FullScreenMdBinding
        get() = _binding!!
    private var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null
    lateinit var playImage: Drawable
    lateinit var pauseImage: Drawable
    lateinit var playPauseButton: ImageView
    lateinit var windowM: WindowFragment
    private lateinit var voteNumber: TickerView
    private lateinit var commentNumber: TickerView
    private lateinit var redditName: AppCompatTextView
    private lateinit var redditIconView: CircleImageView
    private lateinit var backButton: AppCompatImageButton
    private lateinit var joinButton: MControllerView
    private lateinit var voteUpB: MControllerView
    private lateinit var voteDownB: MControllerView
    private lateinit var commentB: ImageView
    lateinit var titleView: AppCompatTextView
    lateinit var avatarView: CircleImageView
    lateinit var userName: AppCompatTextView
    private var land: Boolean = false
    var index: Int = 0
    val args: FullScreenFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Com.Companion.NullComment.pages = emptyList()
        val link = args.link
        viewModel.link.postValue(link)
        index = args.position
        land = resources.getBoolean(R.bool.land)
    }

    var playback: Playback? = null
    var set: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FullScreenMdBinding.inflate(inflater, container, false)
        playImage =
            AppCompatResources.getDrawable(requireContext(), R.drawable.exo_styled_controls_play)!!
        pauseImage =
            AppCompatResources.getDrawable(requireContext(), R.drawable.exo_styled_controls_pause)!!
        return _binding!!.root
    }

    fun isLand(): Boolean {
        return land
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kohii = KohiiProvider.get(requireContext())
        manager = kohii.register(this, MemoryMode.HIGH)
        kohii.prepare(manager)
        binding.mRoot.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        manager.addBucket(binding.container)
        if (savedInstanceState == null) {
            ViewCompat.setTransitionName(binding.container, "full_screen")
            viewModel.rebinder.postValue(args.rebinder)
        }
        bind()
        /*requireActivity().onBackPressedDispatcher.addCallback {
            if (mBottomSheetBehavior?.state != BottomSheetBehavior.STATE_COLLAPSED) {
                closeBottomSheet()
                return@addCallback
            }
            val options = activity?.let {
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    it,
                    androidx.core.util.Pair.create(view, "on_recycle")
                )
            }
            val extras = ActivityNavigatorExtras(options)
            val bundle = Bundle()
            bundle.putString("index", "$index")
            findNavController().navigate(
                R.id.action_fullScreenFragment_to_newsFragment,
                args = bundle,
                null,
                extras
            )
        }*/
        (activity as MainActivity).setBarNoVisibility()
        configureBackdrop()
        prepareFullScreenMode()
        mBottomSheetBehavior?.state =
            BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).setBarNoVisibility()
    }

    fun getLink(onLink: OnLink) {
        this.onLink = onLink
        val link = viewModel.link.value
        if (link != null)
            onLink.getLink(link)
    }

    fun setLink(link: Link) {
        viewModel.linkItem.postValue(link)
    }

    override fun onResume() {
        super.onResume()
        mBottomSheetBehavior?.state =
            BottomSheetBehavior.STATE_HIDDEN
        (activity as MainActivity).setBarNoVisibility()
    }

    private fun prepareFullScreenMode() {
        windowM.binding.root.visibility = View.GONE
        if (!land) {
            val shareButton = binding.exoPlayer.findViewById<ImageView>(R.id.share)
            shareButton.setOnClickListener {
                val item = viewModel.linkItem.value as Link.LinkRedditVideo
                val url = if (!item.url.contains("http")) {
                    "https://${item.url}"
                } else item.url
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setType("text/plain")
                intent.putExtra(Intent.EXTRA_TEXT, Uri.parse(url))
                requireContext().startActivity(Intent.createChooser(intent, item.title))
            }
            binding.exoPlayer.controllerShowTimeoutMs = 0
            voteNumber = binding.exoPlayer.findViewById<TickerView>(R.id.vote_number)
            commentNumber = binding.exoPlayer.findViewById(R.id.comments_number)
            voteNumber.setCharacterLists(TickerUtils.provideNumberList())
            commentNumber.setCharacterLists(TickerUtils.provideNumberList())
            userName = binding.exoPlayer.findViewById(R.id.user_name)
            userName.setOnClickListener {
                val autor = (viewModel.linkItem.value as Link.LinkRedditVideo).author
                if (autor != null)
                    (activity as MainActivity).navigateToProfile(autor)
            }
            avatarView = binding.exoPlayer.findViewById(R.id.user_avatar)
            titleView = binding.exoPlayer.findViewById(R.id.title)
            commentB = binding.exoPlayer.findViewById(R.id.comment)
            voteDownB = binding.exoPlayer.findViewById(R.id.down_vote)
            voteUpB = binding.exoPlayer.findViewById(R.id.up_vote)
            voteUpB.isEnabled = false
            voteDownB.isEnabled = false
            joinButton = binding.exoPlayer.findViewById(R.id.toggleInfo_im)
            redditIconView = binding.exoPlayer.findViewById(R.id.reddit_icon)
            redditName = binding.exoPlayer.findViewById(R.id.reddit_name)
            backButton = binding.exoPlayer.findViewById(R.id.back)
            val buttonSave = binding.exoPlayer.findViewById<MControllerView>(R.id.save)
            buttonSave.onClickListener {
                if (it == MControllerView.State.RELEASED) {
                    lifecycleScope.launch {
                        val link = viewModel.linkItem.value
                        if (link != null) {
                            link as Link.LinkRedditVideo
                            val result = viewModel.save(link.name!!, "link")
                            if (!result) {
                                buttonSave.state = MControllerView.State.RELEASED
                            } else {
                                buttonSave.state = MControllerView.State.SELECTED
                            }
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        val link = viewModel.linkItem.value
                        if (link != null) {
                            link as Link.LinkRedditVideo
                            val result = viewModel.unsave(link.name!!)
                            if (!result) {
                                buttonSave.state = MControllerView.State.SELECTED
                            } else {
                                buttonSave.state = MControllerView.State.RELEASED
                            }
                        }

                    }
                }
            }
            backButton.setOnClickListener {
                if (mBottomSheetBehavior?.state != BottomSheetBehavior.STATE_COLLAPSED) {
                    closeBottomSheet()
                    return@setOnClickListener
                }
                val options = activity?.let {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        it,
                        androidx.core.util.Pair.create(view, "on_recycle")
                    )
                }
                val extras = ActivityNavigatorExtras(options)
                val bundle = Bundle()
                bundle.putString("index", "$index")
                (activity as MainActivity).setBarVisible()
                findNavController().navigate(
                    R.id.action_fullScreenFragment_to_newsFragment,
                    args = bundle,
                    null,
                    extras
                )
            }
            voteDownB.onClickListener {
                val name = (viewModel.linkItem.value as Link.LinkRedditVideo).name

                if (it == MControllerView.State.SELECTED) {
                    if (name != null) {
                        lifecycleScope.launch {
                            when (viewModel.vote(0, name, null)) {
                                is Result.Success -> {
                                    val numberText = voteNumber.text
                                    if (!numberText.contains("[km]".toRegex())) {
                                        var number = numberText.toString().toInt()
                                        voteNumber.text = "${++number}"
                                    }
                                }
                                is Result.Error -> {
                                    voteDownB.changeState(MControllerView.State.SELECTED)
                                }
                            }
                        }
                    }
                } else {
                    if (name != null) {
                        lifecycleScope.launch {
                            when (viewModel.vote(-1, name, null)) {
                                is Result.Success -> {
                                    val numberText = voteNumber.text
                                    if (!numberText.contains("[km]".toRegex())) {
                                        var number = numberText.toString().toInt()
                                        voteNumber.text = "${--number}"
                                    }
                                }
                                is Result.Error -> {
                                    voteDownB.changeState(MControllerView.State.RELEASED)
                                }
                            }
                        }
                    }
                }
            }

            voteUpB.onClickListener {
                val name = (viewModel.linkItem.value as Link.LinkRedditVideo).name
                if (it == MControllerView.State.SELECTED) {
                    if (name != null) {
                        lifecycleScope.launch {
                            when (viewModel.vote(0, name, null)) {
                                is Result.Success -> {
                                    val numberText = voteNumber.text
                                    if (!numberText.contains("[km]".toRegex())) {
                                        var number = numberText.toString().toInt()
                                        voteNumber.text = "${--number}"
                                    }
                                }
                                is Result.Error -> {
                                    voteUpB.changeState(MControllerView.State.SELECTED)
                                }
                            }
                        }
                    }
                } else {
                    if (name != null) {
                        lifecycleScope.launch {
                            when (viewModel.vote(1, name, null)) {
                                is Result.Success -> {
                                    val numberText = voteNumber.text
                                    if (!numberText.contains("[km]".toRegex())) {
                                        var number = numberText.toString().toInt()
                                        voteNumber.text = "${++number}"
                                    }
                                }
                                is Result.Error -> {
                                    voteUpB.changeState(MControllerView.State.RELEASED)
                                }
                            }
                        }
                    }
                }
            }
            commentB.setOnClickListener {
                val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
                commentB.startAnimation(anim)
                openBottomSheet()
            }


            joinButton.onClickListener {
                if (joinButton.state == MControllerView.State.SELECTED) {
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
                                    joinButton.changeState(MControllerView.State.SELECTED)
                                }
                            }
                        }

                    }

                } else if (joinButton.state == MControllerView.State.RELEASED) {
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
                                    joinButton.changeState(MControllerView.State.RELEASED)
                                }
                            }
                        }
                    }
                }
            }
        }

        playPauseButton = binding.exoPlayer.findViewById(R.id.exo_play_pause)
        playPauseButton.setImageDrawable(pauseImage)
        playPauseButton.setOnClickListener {
            viewModel.playState.value = viewModel.playState.value != true
        }

        val volumeButton = binding.exoPlayer.findViewById<ImageView>(R.id.volume_off)
        volumeButton.setOnClickListener {
            val current = viewModel.overlayVolume.value!!
            viewModel.overlayVolume.postValue(VolumeInfo(!current.mute, current.volume))
        }
    }

    fun closeBottomSheet() {
        set = false
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        if (binding.exoPlayer.layoutParams?.height != binding.mRoot.height) {
            binding.exoPlayer.showController()
            binding.exoPlayer.layoutParams?.height = binding.mRoot.height
            binding.exoPlayer.animate()
        }
        windowM.binding.root.visibility = View.GONE
    }

    fun openBottomSheet() {
        set = false
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        if (binding.exoPlayer.layoutParams?.height != binding.mRoot.height / 2) {
            binding.exoPlayer.showController()
            binding.exoPlayer.layoutParams?.height =
                binding.mRoot.height / 2
        }
        windowM.binding.root.visibility = View.VISIBLE
    }

    fun expandBottomSheet() {
        set = false
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun isBottomSheetIsHulf(): Boolean {
        return mBottomSheetBehavior?.state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun setOnBottomSheetCallbacks(
        onBottomSheetCallbacks: OnBottomSheetCallbacks,
        fragment: WindowFragment
    ) {
        this.listener = onBottomSheetCallbacks
        windowM = fragment
    }

    private fun configureBackdrop() {

        val fragment = childFragmentManager.findFragmentById(R.id.filter_fragment)
        fragment?.view?.let {
            BottomSheetBehavior.from(it).let { bs ->
                bs.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {


                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        val hight = windowM.binding.commentButton.height.toFloat()
                        windowM.binding.commentButton.animate()?.y(
                            ((binding.root.parent as FrameLayout).height - 100.dp) * slideOffset + hight - 20.dp
                        )!!.setDuration(0).start()
                        windowM.binding.writeComment.animate()?.y(
                            //windowM.binding.card.height
                            ((binding.root.parent as FrameLayout).height - 100.dp) * slideOffset - 20.dp + hight - windowM.binding.commentButton.height
                        )!!.setDuration(0).start()
                        val upperState = 0.6
                        val collapse = 0.3
                        if (bs.state == BottomSheetBehavior.STATE_SETTLING) {
                            if (set) {
                                when {
                                    slideOffset > upperState -> mBottomSheetBehavior?.state =
                                        BottomSheetBehavior.STATE_EXPANDED
                                    slideOffset < upperState && slideOffset > collapse -> {
                                        openBottomSheet()
                                    }
                                    else -> {
                                        closeBottomSheet()
                                    }
                                }
                            }
                        } else if (!set && bs.state == BottomSheetBehavior.STATE_DRAGGING) {
                            set = true
                        } else if (bs.state == BottomSheetBehavior.STATE_DRAGGING) {
                            if (!binding.exoPlayer.isControllerVisible) {
                                binding.exoPlayer.showController()
                            }
                            if (slideOffset < 0.5f)
                                binding.exoPlayer.layoutParams.height =
                                    (binding.mRoot.height * (1 - slideOffset)).toInt()
                        }
                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        listener?.onStateChanged(bottomSheet, newState)
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                closeBottomSheet()
                                //window.binding.root.y=-100.dp.toFloat()
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {

                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {

                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {

                            }
                            BottomSheetBehavior.STATE_SETTLING -> {

                            }
                        }
                    }
                })
                bs.state = BottomSheetBehavior.STATE_COLLAPSED
                mBottomSheetBehavior = bs
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun bind() {
        viewModel.apply {
            link.observe(viewLifecycleOwner) {
                if (it != null) {
                    val link = "r$it"
                    onLink?.getLink(link)
                }

            }
            rebinder.observe(viewLifecycleOwner) { rebinder ->
                rebinder?.with {
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
                rebinder?.bind(kohii, binding.exoPlayer) {
                    it.config
                    playback = it
                    kohii.stick(it)
                }
            }
            linkItem.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (!land) {
                        voteUpB.isEnabled = true
                        voteDownB.isEnabled = true
                        lifecycleScope.launch {
                            it as Link.LinkRedditVideo
                            commentNumber.text = it.numComments
                            voteNumber.text = (it.ups - it.downs).toString()
                            userName.text = it.author
                            redditName.text = it.subreddit
                            titleView.text = it.title

                            val info = it.author?.let { it1 ->
                                viewModel.getInfo(it1)
                            }
                            when (info) {
                                is Result.Success -> {
                                    val iconLink = info.data.data.icon?.replace("amp;", "")
                                    Glide.with(requireContext()).load(iconLink).into(avatarView)
                                        .onLoadFailed(
                                            AppCompatResources.getDrawable(
                                                requireContext(),
                                                R.drawable.empty
                                            )
                                        )
                                }
                                is Result.Error -> {

                                }
                                else -> {

                                }
                            }
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
            subInfo.observe(viewLifecycleOwner) {
                if (!land) {
                    if (it.userIsSubscriber == true) {
                        joinButton.changeState(MControllerView.State.SELECTED)
                    }
                    Glide.with(requireContext()).load(it.iconImage).into(redditIconView)
                }
            }
            overlayVolume.observe(viewLifecycleOwner) {
                val volumeButton = binding.exoPlayer.findViewById<ImageView>(R.id.volume_off)
                if (it.mute) {
                    val image = AppCompatResources.getDrawable(
                        requireContext(), R.drawable.ic_baseline_volume_off_24
                    )
                    volumeButton.setImageDrawable(image)
                } else {
                    val image = AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_baseline_volume_up_24
                    )
                    volumeButton.setImageDrawable(image)
                }
                manager.applyVolumeInfo(it, binding.container, Scope.MANAGER)
            }
            playState.observe(viewLifecycleOwner) {
                if (it) {
                    playback?.playable?.let { it1 -> manager.pause(it1) }
                    playPauseButton.setImageDrawable(playImage)
                } else {
                    playback?.playable?.let { it1 -> manager.play(it1) }
                    playPauseButton.setImageDrawable(pauseImage)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}