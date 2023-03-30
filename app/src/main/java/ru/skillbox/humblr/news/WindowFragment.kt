package ru.skillbox.humblr.news

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.transition.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.*
import ru.skillbox.humblr.data.interfaces.OnBottomSheetCallbacks
import ru.skillbox.humblr.databinding.FragmentWindowBinding
import ru.skillbox.humblr.databinding.WriteCommentLayoutBinding
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.*
import ru.skillbox.humblr.utils.adapters.CommentAdapter
import ru.skillbox.humblr.utils.adapters.CommentsDelegateAdapter


@InternalCoroutinesApi
@AndroidEntryPoint
class WindowFragment : BottomSheetDialogFragment(), OnBottomSheetCallbacks, OnLink,
    CommentAdapter.CommentHandler {

    val viewModel: WindowFragViewModel by viewModels()
    private var couldScroll = false
    private var currentState: Int = BottomSheetBehavior.STATE_EXPANDED
    private var _binding: FragmentWindowBinding? = null
    val binding: FragmentWindowBinding
        get() = _binding!!
    var link: String? = null
    private lateinit var recycle: LCEERecyclerView
    private lateinit var scene: Scene
    private lateinit var scene2: Scene
    private var _binding2: WriteCommentLayoutBinding? = null
    val binding2: WriteCommentLayoutBinding
        get() = _binding2!!
    lateinit var adapter: CommentsDelegateAdapter
    lateinit var mainFragment: FullScreenFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Com.Companion.NullComment.pages = emptyList()
        Com.Companion.NullComment.setPagesCount(0)
        _binding2 = WriteCommentLayoutBinding.inflate(inflater, container, false)
        mainFragment = parentFragment as FullScreenFragment
        mainFragment.setOnBottomSheetCallbacks(this, this)
        _binding = FragmentWindowBinding.inflate(inflater, container, false)
        scene = Scene(binding.sceneHolder, binding2.root)
        scene2 = Scene(binding.sceneHolder, binding.roo)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CommentsDelegateAdapter(lifecycleScope, this)
        recycle = binding.rec
        binding.rec.recyclerView.adapter = adapter
        bind()
        binding.rec.setOnRetryClickListener {
            viewModel.exceptions.postValue(null)
            viewModel.state.postValue(WindowFragViewModel.State.INIT)
        }
        binding.commentButton.setOnClickListener {
            if (binding.commentButton.isExtended) {
                viewModel.state.postValue(WindowFragViewModel.State.EXPANDED)
            } else {
                prepareWriteComment()
            }
        }
        binding.writeComment.setOnClickListener {
            prepareWriteComment()
        }
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
        binding.rec.recyclerView.setOnScrollChangeListener { recycle, _, scollY, _, prevScrollY ->
            val canscroll = recycle.canScrollVertically(1)
            if (!canscroll && couldScroll && mainFragment.isBottomSheetIsHulf() && !mainFragment.isLand()) {
                mainFragment.expandBottomSheet()
            }

        }
    }


    fun prepareWriteComment() {
        TransitionManager.go(scene)
        val link =
            mainFragment.viewModel.linkItem.value as Link.LinkRedditVideo
        binding2.editor.focusEditor()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding2.editor, InputMethodManager.SHOW_IMPLICIT)
        binding2.title.text = link.title
        val instant = link.createdUTC.let { Instant.ofEpochSecond(it) }
        val res = binding2.userName.context.resources
        val prefix =
            if (link.edited != null) res.getString(R.string.edited) else {
                ""
            }
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        when {
            duration.toDays() > 0 -> {
                binding2.userName.text = "${link.author} ${
                    String.format(
                        res.getString(R.string.days_ago),
                        prefix,
                        duration.toDays()
                    )
                }"
            }
            duration.toHours() > 0 -> {
                "${link.author} ${
                    String.format(
                        res.getString(R.string.hours_ago),
                        prefix,
                        duration.toDays()
                    )
                }"
            }
            duration.toMinutes() > 0 -> {
                "${link.author} ${
                    String.format(
                        res.getString(R.string.minutes_ago),
                        prefix,
                        duration.toDays()
                    )
                }"
            }
            else -> {
                "${link.author} ${
                    String.format(
                        res.getString(R.string.now),
                        prefix,
                        duration.toDays()
                    )
                }"
            }
        }
        mainFragment.expandBottomSheet()
        binding2.post.setOnClickListener {
            val linkq = viewModel.linkG.value as Link.LinkRedditVideo
            if (binding2.editor.html == null || binding2.editor.html!!.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Nothing to post because message is empty",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val comment = viewModel.postComment(linkq.name!!, binding2.editor.html!!)
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
                    binding.rec.recyclerView.scrollToPosition(list.lastIndex)
                }
            }
        }
    }


    override fun onStateChanged(bottomSheet: View, newState: Int) {
        currentState = newState
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
            }
            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
            }
        }
    }

    private fun getComments(link: String) {
        val status = (activity as MainActivity).internetStatus()
        if (status) {
            Log.d("status", "starus")
            binding.rec.showLoadingView()
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
                        binding.rec.showEmptyView(resources.getString(R.string.empty))
                    }
                })
        }

    }

    @FlowPreview
    fun bind() {
        viewModel.linkG.observe(viewLifecycleOwner) { link ->
            if (link != null) {
                mainFragment.setLink(link)
            }
        }
        viewModel.exceptions.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.state.postValue(WindowFragViewModel.State.ERROR)
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                WindowFragViewModel.State.INIT -> {
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                    binding.rec.recyclerView.visibility = View.INVISIBLE
                    (parentFragment as FullScreenFragment).getLink(this)
                    viewModel.getMe()
                    viewModel.state.postValue(WindowFragViewModel.State.LOADING)
                }
                WindowFragViewModel.State.LOADING -> {
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                WindowFragViewModel.State.PREVIEW -> {
                    adapter.initPreview()
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                WindowFragViewModel.State.EXPANDED -> {
                    adapter.initFirst()
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                WindowFragViewModel.State.LOADING_ACCOUNT -> {
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                    binding.rec.showLoadingView()
                }
                WindowFragViewModel.State.LOADING_PREVIEW -> {
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                WindowFragViewModel.State.ERROR -> {
                    binding.rec.showErrorView(viewModel.exceptions.value?.message)
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                WindowFragViewModel.State.LOADED_PREVIEW -> {
                    binding.rec.hideAllViews()
                    binding.rec.recyclerView.visibility = View.VISIBLE
                    couldScroll = true
                    binding.writeComment.show()
                    binding.commentButton.show()
                    binding.commentButton.extend()
                }
                WindowFragViewModel.State.LOADED_ACCOUNT -> {
                    binding.rec.hideAllViews()
                    binding.rec.recyclerView.visibility = View.VISIBLE
                    binding.writeComment.visibility = View.GONE
                    binding.writeComment.hide()
                    binding.commentButton.show()
                    binding.commentButton.shrink()
                    couldScroll = true

                }
            }
        }
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            if (comments != null) {
                if (viewModel.state.value == WindowFragViewModel.State.LOADING) {
                    viewModel.state.postValue(WindowFragViewModel.State.PREVIEW)
                }
            }
        }
        viewModel.pageList.observe(viewLifecycleOwner) {
            if (it != null) {
                if (viewModel.state.value == WindowFragViewModel.State.LOADED_ACCOUNT ||
                    viewModel.state.value == WindowFragViewModel.State.LOADED_PREVIEW
                ) {
                    binding.rec.hideAllViews()
                    binding.rec.recyclerView.visibility = View.VISIBLE

                    if (viewModel.state.value == WindowFragViewModel.State.LOADED_PREVIEW) {
                        adapter.setPage(it, true)
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
                    binding.rec.recyclerView.scrollToPosition(0)
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
                        (viewModel.linkG.value as Link.LinkRedditVideo).name!!,
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
        val ids = list.map { comment ->
            comment.autorName
        }.reduce { first, second -> "$first,$second" }
        if (ids != null) {
            val accounts = viewModel.getAccounts(ids)
            val autorHolder = accounts?.users
            if (autorHolder != null) {
                for (comment in list) {
                    val info = autorHolder[comment.autorName]
                    comment.account = info
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _binding2 = null

    }

    override fun getLink(link: String) {
        getComments(link)
    }

    override fun getPage(index: Int, preview: Boolean) {
        if (viewModel.state.value != WindowFragViewModel.State.LOADING_PREVIEW && viewModel.state.value != WindowFragViewModel.State.LOADING_ACCOUNT) {
            if (preview) {
                viewModel.state.postValue(WindowFragViewModel.State.LOADING_PREVIEW)
            } else {
                viewModel.currentPage = index
                viewModel.state.postValue(WindowFragViewModel.State.LOADING_ACCOUNT)
            }
            viewModel.viewModelScope.launch {
                binding.rec.showLoadingView()
                couldScroll = false
                binding.rec.recyclerView.visibility = View.INVISIBLE
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
                if (list.last().account == null) {
                    getCommentsUsers(list)
                    if (preview) {
                        viewModel.state.postValue(WindowFragViewModel.State.LOADED_PREVIEW)
                    } else {
                        viewModel.state.postValue(WindowFragViewModel.State.LOADED_ACCOUNT)
                    }
                    viewModel.pageList.postValue(list)
                } else {
                    if (preview) {
                        viewModel.state.postValue(WindowFragViewModel.State.LOADED_PREVIEW)
                    } else {
                        viewModel.state.postValue(WindowFragViewModel.State.LOADED_ACCOUNT)
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
        binding.writeComment.visibility = View.VISIBLE
        binding.commentButton.visibility = View.VISIBLE
        binding.commentButton.extend()
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
            (parentFragment as FullScreenFragment).viewModel.linkItem.value as Link.LinkRedditVideo
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
        mainFragment.expandBottomSheet()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _binding2 = null
    }

}