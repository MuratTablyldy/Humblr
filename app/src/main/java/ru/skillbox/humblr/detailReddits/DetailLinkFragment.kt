package ru.skillbox.humblr.detailReddits

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.core.app.SharedElementCallback
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
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
import ru.skillbox.humblr.databinding.*
import ru.skillbox.humblr.mainPackage.MainActivity
import ru.skillbox.humblr.utils.CallBack
import ru.skillbox.humblr.utils.Com
import ru.skillbox.humblr.utils.MControllerView
import ru.skillbox.humblr.utils.adapters.CommentAdapter
import ru.skillbox.humblr.utils.adapters.CommentsDelegateAdapter
import ru.skillbox.humblr.utils.dp

@AndroidEntryPoint
class DetailLinkFragment : Fragment(), CommentAdapter.CommentHandler,CallBack {
    private var _binding: DetailLinkBinding? = null
    private var land: Boolean = false
    private val binding: DetailLinkBinding
        get() = _binding!!
    private val viewModel: DetailLinkViewModel by viewModels()
    lateinit var adapter: CommentsDelegateAdapter
    private var couldScroll = false
    private val args: DetailLinkFragmentArgs by navArgs()
    private var _binding2: WriteCommentLayoutBinding? = null
    private val binding2: WriteCommentLayoutBinding
        get() = _binding2!!
    private lateinit var scene: Scene
    private lateinit var scene2: Scene
    private var _bindingMain: FullScreenLayoutMBinding? = null
    private val fadeTransition: Transition = Fade()
    private val bindingMain: FullScreenLayoutMBinding
        get() = _bindingMain!!
    private var _bindingLoad: LoadingViewPrevBinding? = null
    val bindingLoad: LoadingViewPrevBinding
        get() = _bindingLoad!!
    private lateinit var screneLoading: Scene
    var url: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Com.Companion.NullComment.setPagesCount(0)
        Com.Companion.NullComment.pages = emptyList()
        val transition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.image_shared_element)
        sharedElementEnterTransition = transition
        land = resources.getBoolean(R.bool.land)
        _bindingLoad = LoadingViewPrevBinding.inflate(inflater, container, false)
        url = args.url
        _bindingMain = FullScreenLayoutMBinding.inflate(inflater, container, false)
        _binding2 = WriteCommentLayoutBinding.inflate(inflater, container, false)
        _binding = DetailLinkBinding.inflate(inflater, container, false)
        scene = Scene(bindingMain.root, binding2.root)
        scene2 = Scene(bindingMain.root, binding.root)
        screneLoading = Scene(bindingMain.root, bindingLoad.root)
        if (url != null) {
            prepareSharedElementTransition()
            if (savedInstanceState == null) {
                postponeEnterTransition()
            }
            binding.rich.setLink(url!!, lifecycleScope) {
                startPostponedEnterTransition()
            }
            TransitionManager.go(scene2)
        } else {
            TransitionManager.go(screneLoading, fadeTransition)
        }
        return bindingMain.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCharacterList(binding.voteNumber)
        setCharacterList(binding.commentNumber)
        adapter = CommentsDelegateAdapter(lifecycleScope, this)
        binding.rec.recyclerView.adapter = adapter
        bind()
        initialize()
    }
    fun setCharacterList(tickerView: TickerView){
        tickerView.setCharacterLists(TickerUtils.provideNumberList())
    }
    private fun retry(){
        viewModel.exceptions.postValue(null)
        viewModel.state.postValue(DetailLinkViewModel.State.INIT)
    }
    private fun initialize(){
        binding.back.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.userName.setOnClickListener {
            val autor = viewModel.link.value?.author
            if (autor != null) {
                val direction = MainNavGraphDirections.actionGlobalProfileGrapth(autor)
                findNavController().navigate(direction)
            }
        }
        binding.save.onClickListener {
            if (it == MControllerView.State.RELEASED) {
                lifecycleScope.launch {
                    val result = viewModel.save(viewModel.link.value?.name!!, "link")
                    if (!result) {
                        binding.save.state = MControllerView.State.RELEASED
                    } else {
                        binding.save.state = MControllerView.State.SELECTED
                    }
                }
            } else {
                lifecycleScope.launch {
                    val result = viewModel.unsave(viewModel.link.value?.name!!)
                    if (!result) {
                        binding.save.state = MControllerView.State.SELECTED
                    } else {
                        binding.save.state = MControllerView.State.RELEASED
                    }
                }
            }
        }
        binding.rec.setOnRetryClickListener {
            retry()
        }
        binding.share.setOnClickListener {
            val url = if (viewModel.link.value?.url?.contains("http") == false) {
                "https://${viewModel.link.value?.url}"
            } else viewModel.link.value?.url

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, Uri.parse(url))
            requireContext().startActivity(
                Intent.createChooser(
                    intent, viewModel.link.value?.title
                )
            )
        }
        binding.commentButton.setOnClickListener {
            if (binding.commentButton.isExtended) {
                viewModel.state.postValue(DetailLinkViewModel.State.EXPANDED)
            } else {
                prepareWriteComment()
            }
        }
        binding.writeComment.setOnClickListener {
            prepareWriteComment()
        }
        binding.downVote.onClickListener {
            val name = (viewModel.link.value as Link.LinkOut).name
            if (it == MControllerView.State.SELECTED) {
                if (name != null) {
                    lifecycleScope.launch {
                        when (viewModel.vote(-1, name, null)) {
                            is Result.Success -> {
                                val numberText = binding.voteNumber.text
                                if (!numberText.contains("[km]".toRegex())) {
                                    var number = numberText.toString().toInt()
                                    binding.voteNumber.text = "${--number}"
                                }
                            }
                            is Result.Error -> {
                                binding.downVote.state = MControllerView.State.RELEASED
                            }
                        }
                    }
                }
            } else {
                if (name != null) {
                    lifecycleScope.launch {
                        when (viewModel.vote(0, name, null)) {
                            is Result.Success -> {
                                val numberText = binding.voteNumber.text
                                if (!numberText.contains("[km]".toRegex())) {
                                    var number = numberText.toString().toInt()
                                    binding.voteNumber.text = "${++number}"
                                }
                            }
                            is Result.Error -> {
                                binding.downVote.state = MControllerView.State.SELECTED
                            }
                        }
                    }
                }
            }
        }
        binding.join.onClickListener {
            if (binding.join.state == MControllerView.State.SELECTED) {
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
                                binding.join.changeState(MControllerView.State.SELECTED)
                            }
                        }
                    }
                }
            } else if (binding.join.state == MControllerView.State.RELEASED) {
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
                                binding.join.changeState(MControllerView.State.RELEASED)
                            }
                        }
                    }
                }
            }
        }
        binding.upVote.onClickListener {
            val name = (viewModel.link.value as Link.LinkOut).name
            if (it == MControllerView.State.SELECTED) {
                if (name != null) {
                    lifecycleScope.launch {
                        when (viewModel.vote(1, name, null)) {
                            is Result.Success -> {
                                val numberText = binding.voteNumber.text
                                if (!numberText!!.contains("[km]".toRegex())) {
                                    var number = numberText.toString().toInt()
                                    binding.voteNumber.text = "${++number}"
                                }
                            }
                            is Result.Error -> {
                                binding.upVote.state = MControllerView.State.RELEASED
                            }
                        }
                    }
                }
            } else {
                if (name != null) {
                    lifecycleScope.launch {
                        when (viewModel.vote(0, name, null)) {
                            is Result.Success -> {
                                val numberText = binding.voteNumber.text
                                if (!numberText!!.contains("[km]".toRegex())) {
                                    var number = numberText.toString().toInt()
                                    binding.voteNumber.text = "${--number}"
                                }
                            }
                            is Result.Error -> {
                                binding.upVote.state = MControllerView.State.SELECTED
                            }
                        }
                    }
                }
            }
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
    }

    private fun prepareWriteComment() {
        TransitionManager.go(scene)
        val link = viewModel.link.value
        binding2.editor.focusEditor()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding2.editor, InputMethodManager.SHOW_IMPLICIT)
        binding2.title.text = link?.title
        val instant = link?.createdUTC?.let {
            Instant.ofEpochSecond(it)
        }
        val res = binding2.userName.context.resources
        val prefix = if (link?.edited != null) res.getString(R.string.edited) else {
            ""
        }
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        when {
            duration.toDays() > 0 -> {
                binding2.userName.text = link?.author?.let {
                    String.format(
                        it, res.getString(R.string.days_ago2), prefix, duration.toDays()
                    )
                }
            }
            duration.toHours() > 0 -> {
                binding2.userName.text = link?.author?.let {
                    String.format(
                        it, res.getString(R.string.hours_ago2), prefix, duration.toHours()
                    )
                }
            }
            duration.toMinutes() > 0 -> {
                binding2.userName.text = link?.author?.let {
                    String.format(
                        it, res.getString(R.string.minutes_ago2), prefix, duration.toMinutes()
                    )
                }
            }
            else -> {
                binding2.userName.text = link?.author?.let {
                    String.format(
                        it, res.getString(R.string.now), prefix, duration.toMinutes()
                    )
                }
            }
        }
        binding2.post.setOnClickListener {
            val linkq = viewModel.link.value
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
                    binding.rec.recyclerView.scrollToPosition(list.lastIndex)
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
                        binding.rec.recyclerView.scrollToPosition(list.lastIndex)
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
            binding.rec.showLoadingView()
            viewModel.getComments(link = link,
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
                        binding.commentButton.show()
                        binding.commentButton.shrink()
                    }
                })
        }

    }

    override fun getPage(index: Int, preview: Boolean) {
        if (viewModel.state.value != DetailLinkViewModel.State.LOADING_PREVIEW && viewModel.state.value != DetailLinkViewModel.State.LOADING_ACCOUNT) {
            if (preview) {
                viewModel.state.postValue(DetailLinkViewModel.State.LOADING_PREVIEW)
            } else {
                viewModel.currentPage = index
                viewModel.state.postValue(DetailLinkViewModel.State.LOADING_ACCOUNT)
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
                toIndex = if (toIndex >= size!!) size - 1 else toIndex
                val list = mutableListOf<Comment>()
                val comments = viewModel.comments.value
                for (i in currentIndex..toIndex) {
                    comments?.get(i)?.let { list.add(it) }
                }
                if (list.last().account == null) {
                    getCommentsUsers(list)
                    if (preview) {
                        viewModel.state.postValue(DetailLinkViewModel.State.LOADED_PREVIEW)
                    } else {
                        viewModel.state.postValue(DetailLinkViewModel.State.LOADED_ACCOUNT)
                    }
                    viewModel.pageList.postValue(list)
                } else {
                    if (preview) {
                        viewModel.state.postValue(DetailLinkViewModel.State.LOADED_PREVIEW)
                    } else {
                        viewModel.state.postValue(DetailLinkViewModel.State.LOADED_ACCOUNT)
                    }
                    viewModel.pageList.postValue(list)
                }
            }
        }
    }

    override suspend fun vote(
        num: Int, name: String, view: MControllerView, tickerView: TickerView
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
        view: View, parent: ViewGroup, comment: Comment, root: Boolean, depth: Int
    ) {
        TransitionManager.go(scene)

        val link = viewModel.link.value as Link.LinkOut
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding2.editor, InputMethodManager.SHOW_IMPLICIT)
        binding2.title.text =
            comment.body?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
        val instant = link.createdUTC.let { Instant.ofEpochSecond(it) }
        val res = binding2.userName.context.resources
        val prefix = if (comment.edited != null) res.getString(R.string.edited) else {
            ""
        }

        val now = Instant.now()
        val duration = Duration.between(instant, now)
        val pref = "$${comment.author} $prefix"
        when {
            duration.toDays() > 0 -> {
                binding2.userName.text = String.format(
                    res.getString(R.string.days_ago), pref, duration.toDays()
                )

            }
            duration.toHours() > 0 -> {
                binding2.userName.text = String.format(
                    res.getString(R.string.hours_ago), pref, duration.toDays()
                )

            }
            duration.toMinutes() > 0 -> {
                binding2.userName.text = String.format(
                    res.getString(R.string.minutes_ago), pref, duration.toDays()
                )
            }
            else -> {
                binding2.userName.text = String.format(
                    res.getString(R.string.now), pref, duration.toDays()
                )
            }
        }
        binding2.post.setOnClickListener {
            if (binding2.editor.html == null || binding2.editor.html!!.isEmpty()) {
                Toast.makeText(
                    requireContext(), "Nothing to post because message is empty", Toast.LENGTH_LONG
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
                        (viewModel.link.value as Link.LinkOut).name!!,
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
            val prefix = if (comment.edited != null) res.getString(R.string.edited) else {
                ""
            }
            val now = Instant.now()
            val duration = Duration.between(instant, now)
            val timeView = root.findViewById<TextView>(R.id.time)
            when {
                duration.toDays() > 0 -> {
                    timeView.text = String.format(
                        res.getString(R.string.days_ago), prefix, duration.toDays()
                    )
                }
                duration.toHours() > 0 -> {
                    timeView.text = String.format(
                        res.getString(R.string.hours_ago), prefix, duration.toHours()
                    )
                }
                duration.toMinutes() > 0 -> {
                    timeView.text = String.format(
                        res.getString(R.string.minutes_ago), prefix, duration.toMinutes()
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
            link.observe(viewLifecycleOwner) {
                if (it != null) {
                    bindingLoad.progressL.progress = 1f
                    if (!land) {
                        val instant =
                            viewModel.link.value?.createdUTC?.let { Instant.ofEpochSecond(it) }
                        val prefix = ""
                        val now = Instant.now()
                        val duration = Duration.between(instant, now)
                        when {
                            duration.toDays() > 0 -> {
                                binding.time.text = String.format(
                                    resources.getString(R.string.days_ago),
                                    prefix,
                                    duration.toDays()
                                )
                            }
                            duration.toHours() > 0 -> {
                                binding.time.text = String.format(
                                    resources.getString(R.string.hours_ago),
                                    prefix,
                                    duration.toHours()
                                )
                            }
                            duration.toMinutes() > 0 -> {
                                binding.time.text = String.format(
                                    resources.getString(R.string.minutes_ago),
                                    prefix,
                                    duration.toMinutes()
                                )
                            }
                            else -> {
                                binding.time.text =
                                    String.format(resources.getString(R.string.now), prefix)
                            }
                        }
                        binding.upVote.isEnabled = true
                        binding.downVote.isEnabled = true
                        lifecycleScope.launch {

                            binding.commentNumber.text = it.numComments
                            binding.voteNumber.text = (it.ups - it.downs).toString()
                            binding.userName.text = it.author
                            binding.redditName.text = it.subreddit
                            binding.title.text = it.title
                            binding.text.text = it.selftext
                            if (url == null) {
                                binding.rich.setLink(it.url, lifecycleScope)
                            }
                            val info = it.author?.let { it1 ->
                                viewModel.getInfo(it1)
                            }
                            when (info) {
                                is Result.Success -> {
                                    bindingLoad.progressL.progress = 2f
                                    val iconLink = info.data.data.icon?.replace("amp;", "")
                                    binding.avatarView.let { it1 ->
                                        Glide.with(requireContext()).load(iconLink).into(it1)
                                    }
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
                    bindingLoad.progressL.progress = 3f
                    TransitionManager.go(scene2, fadeTransition)
                    if (it.userIsSubscriber == true) {
                        binding.join.changeState(MControllerView.State.SELECTED)
                    }
                }
            }
        }

        viewModel.exceptions.observe(viewLifecycleOwner) {
            if (it != null) {
                if(it is MainRepository.TokenISInvalidException){
                    (activity as MainActivity).onTokenExpired(this@DetailLinkFragment)
                    return@observe
                }
                viewModel.state.postValue(DetailLinkViewModel.State.ERROR)
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                DetailLinkViewModel.State.INIT -> {
                    bindingLoad.progressL.max = 3f
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                    binding.rec.recyclerView.visibility = View.INVISIBLE
                    getComments(args.link)
                    viewModel.getMe()
                    viewModel.state.postValue(DetailLinkViewModel.State.LOADING)
                }
                DetailLinkViewModel.State.LOADING -> {
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                DetailLinkViewModel.State.PREVIEW -> {
                    adapter.initPreview()
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                DetailLinkViewModel.State.EXPANDED -> {
                    adapter.initFirst()
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                DetailLinkViewModel.State.LOADING_ACCOUNT -> {
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                    binding.rec.showLoadingView()
                }
                DetailLinkViewModel.State.LOADING_PREVIEW -> {
                    binding.rec.showLoadingView()
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                DetailLinkViewModel.State.ERROR -> {
                    binding.rec.showErrorView(viewModel.exceptions.value?.message)
                    binding.writeComment.hide()
                    binding.commentButton.hide()
                }
                DetailLinkViewModel.State.LOADED_PREVIEW -> {
                    binding.rec.hideAllViews()
                    binding.rec.recyclerView.visibility = View.VISIBLE
                    couldScroll = true
                    binding.writeComment.show()
                    binding.commentButton.show()
                    binding.commentButton.extend()
                }
                DetailLinkViewModel.State.LOADED_ACCOUNT -> {
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
                if (viewModel.state.value == DetailLinkViewModel.State.LOADING) {
                    viewModel.state.postValue(DetailLinkViewModel.State.PREVIEW)
                }
            }
        }
        viewModel.pageList.observe(viewLifecycleOwner) {
            if (it != null) {
                if (viewModel.state.value == DetailLinkViewModel.State.LOADED_ACCOUNT || viewModel.state.value == DetailLinkViewModel.State.LOADED_PREVIEW) {
                    binding.rec.hideAllViews()
                    binding.rec.recyclerView.visibility = View.VISIBLE

                    if (viewModel.state.value == DetailLinkViewModel.State.LOADED_PREVIEW) {
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
                        Log.d("page", it.toString())
                        adapter.setPage(it, false)
                    }
                    binding.rec.recyclerView.scrollToPosition(0)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _binding2 = null
    }

    private fun prepareSharedElementTransition() {
        val transition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.image_shared_element)
        sharedElementEnterTransition = transition

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String?>, sharedElements: MutableMap<String?, View?>
            ) {
                if (sharedElements.isNotEmpty()) {
                    sharedElements[names[0]] = binding.rich
                    sharedElements[names[1]] = binding.rich.textViewTitle
                    sharedElements[names[2]] = binding.rich.textViewDesp
                    sharedElements[names[3]] = binding.rich.textViewUrl
                    sharedElements[names[4]] = binding.rich.imageView
                }
            }
        })
    }

    companion object {
        const val URL = "url"
        const val LINK = "link"
    }

    override fun invoke() {
        retry()
    }
}