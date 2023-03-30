package ru.skillbox.humblr.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.*
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.utils.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class YoutubeViewModel @Inject constructor(val repository: MainRepository):ViewModel() {
    private val _comments = MutableLiveData<List<Comment>?>(null)
    val comments: LiveData<List<Comment>?>
        get() = _comments
    val pageList = MutableLiveData<MutableList<Comment>>(null)
    val exceptions = SingleLiveEvent<Exception>(null)
    val me = MutableLiveData<Account?>()
    var currentPage=1
    var time:Float=0f

    val state = MutableLiveData(State.INIT)

    val linkItem = MutableLiveData<Link.LinkYouTube>(null)
    val subInfo=MutableLiveData<SubredditInfo>()
    private var previousVisibility = false
    private val _bottomViewVisibility = MutableLiveData<Boolean>(false)
    suspend fun vote(dir: Int, id: String, rank: Int?) =
        repository.vote(dir, id, rank)

    suspend fun save(fullname:String,category:String):Boolean{
        return when(repository.save(fullname = fullname, category = category)){
            is Result.Success->{
                true
            }
            is Result.Error->{
                false
            }
        }
    }
    suspend fun unsave(fullname:String):Boolean{
        return when(repository.unsave(fullname = fullname)){
            is Result.Success->{
                true
            }
            is Result.Error->{
                false
            }
        }
    }

    fun getComments(
        link: String,
        commentID: String?,
        context: Int?,
        depth: Int?,
        limit: Int?,
        showedits: Boolean,
        showmedia: Boolean,
        showmore: Boolean,
        showtitle: Boolean,
        sort: String,
        threaded: Boolean,
        onEmpty: () -> Unit,
    ) {
        viewModelScope.launch {
            val value = repository.getComments(
                link = link,
                comment = commentID,
                context = context,
                depth = depth,
                limit = limit,
                showedits = showedits,
                showmedia = showmedia,
                showmore = showmore,
                showtitle = showtitle,
                sort = sort,
                threaded = threaded
            )
            val comments: List<Comment>?
            when (value) {
                is Result.Success -> {
                    val array = value.data
                    val linkq = array[0].data.children?.first()?.data as Link.LinkYouTube
                    linkItem.postValue(linkq)
                    comments = mutableListOf()
                    array[1].data.children?.forEach {
                        when (it.data) {
                            is Comment -> {
                                comments.add(it.data)
                            }
                            is More -> {
                                from(it.data, comments, linkq.name!!)
                            }
                        }
                    }
                    if (comments.isEmpty()) {
                        onEmpty()
                        return@launch
                    }
                    _comments.postValue(comments)
                }
                else -> {
                    exceptions.postValue((value as Result.Error).exception)
                }
            }
        }
    }

    suspend fun postComment(thingId: String, text: String): Comment? {
        return when (val result = repository.postComment(thingId, text)) {
            is Result.Success -> {
                val resultq = result.data.jquery.first().comment?.data
                resultq
            }
            is Result.Error -> {
                exceptions.postValue(result.exception)
                null
            }
        }
    }

    fun getMe() {
        viewModelScope.launch {
            when (val result = repository.getMe()) {
                is Result.Success -> {
                    if (result.data.data.icon == null) {
                        result.data.data.icon =
                            "https://www.redditstatic.com/avatars/defaults/v2/avatar_default_2.png"
                    }
                    me.postValue(result.data.data)
                }
                is Result.Error -> {
                    exceptions.postValue(result.exception)
                }
            }
        }
    }

    suspend fun getAccounts(ids: String): UserHolder? {
        return when (val result = repository.getAccounts(ids)) {
            is Result.Success -> {
                result.data
            }
            is Result.Error -> {
                exceptions.postValue(result.exception)
                null
            }
        }
    }


    suspend fun from(more: More, parentList: MutableList<Comment>, linkId: String) {
        if (!more.children.isNullOrEmpty()) {
            val ids = more.children.reduce { first, second -> "$first,$second" }
            val children = getChildren(
                depth = null,
                id = null,
                children = ids,
                limitChildren = false,
                linkId = linkId,
                "top",
                "json"
            )
            if (more.parentId.startsWith("t3")) {
                val list = children?.data?.data?.things
                list?.forEach {
                    when (it.data) {
                        is Comment -> {
                            parentList.add(it.data)
                        }
                        is More -> {
                            from(it.data, parentList, linkId)
                        }
                    }
                }
            } else {
                val parent =
                    parentList.find { created -> created.getIds() == more.parentId } as Comment
                if (parent.replies.data.children == null) {
                    parent.replies.data.children = mutableListOf()
                }
                val l = parent.replies.data.children as MutableList
                children?.data?.data?.things?.let { l.addAll(it) }
            }
        }
    }

    suspend fun getInfo(subName: String) = repository.getAccountInfo(subName)

    suspend fun getChildren(
        depth: Int?,
        id: String?,
        children: String,
        limitChildren: Boolean,
        linkId: String,
        sort: String,
        type: String
    ): FromMore? {

        val result = repository.getChildren(
            depth,
            id,
            children,
            limitChildren,
            linkId,
            sort,
            type
        )
        return when (result) {
            is Result.Success -> {
                result.data
            }
            is Result.Error -> {
                exceptions.postValue(result.exception)
                null
            }
        }
    }

    suspend fun subscribe(action: RedditApi.SubscibeType, skip:Boolean?, srName:String)=repository.subscribe(action,skip,srName)
    suspend fun getSubredditAbout(subreddit:String)=
        repository.getSubredditAbout(subreddit)

    enum class State {
        PREVIEW, EXPANDED, LOADING, INIT, ERROR, LOADING_ACCOUNT, LOADING_PREVIEW, LOADED_ACCOUNT, LOADED_PREVIEW
    }
}