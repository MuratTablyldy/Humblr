package ru.skillbox.humblr.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kohii.v1.core.Rebinder
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.launch
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.*
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import javax.inject.Inject

@HiltViewModel
class ResFragViewModel @Inject constructor(val repository: MainRepository) : ViewModel() {
    val links = MutableLiveData<List<Link>>(emptyList())
    val exceptions = MutableLiveData<Exception>()
    val comments = MutableLiveData<List<Comment>>(emptyList())
    val me = MutableLiveData<Account>()
    val recyclerViewVolume = MutableLiveData(VolumeInfo(true, 1F))
    private val _selectedRebinder= MutableLiveData<Pair<Int, Rebinder?>>(-1 to null)
    fun setRebinder(value: Pair<Int, Rebinder?>){
        _selectedRebinder.postValue(value)
    }
    val rebinder: LiveData<Pair<Int, Rebinder?>>
        get() = _selectedRebinder

    fun getSavedSubreddits(
        username: String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort,
        progress: RoundCornerProgressBar,
        onEmpty: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.getSavedSubreddit(
                username,
                after,
                before,
                count,
                limit,
                srDetail,
                "given",
                time,
                context,
                sort
            )
            when (result) {
                is Result.Success -> {
                    val data=result.data.data.children?.map { it.data }
                    if(data.isNullOrEmpty()){
                        onEmpty.invoke()
                    }else{
                        progress.max=data.size.toFloat()
                        var index=0f
                        val ids=data.map { link->link.getSubredditI() }.reduce{first,second->"$first,$second"}
                        val res=repository.getSubredditsAbout(ids)
                        var infos:List<SubredditInfo>?=null
                        when(res){
                            is Result.Success->{
                                infos=res.data.data.children?.map { it.data }
                            }
                            is Result.Error->{
                                exceptions.postValue(res.exception)
                            }
                        }
                        data.forEach { link->
                            val info=infos?.find { it.displayName==link.getSubredditI() }
                            progress.progress=index++
                            link.subInfo=info
                        }
                        links.postValue(data!!)
                    }
                }
                is Result.Error -> {
                    exceptions.postValue(result.exception)
                }
            }
        }
    }

    fun getSavedComments(
        username: String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort
    ) {
        viewModelScope.launch {
            val result = repository.getCommentsSaved(
                username,
                after,
                before,
                count,
                limit,
                srDetail,
                time,
                context,
                sort
            )
            when (result) {
                is Result.Success -> {
                    comments.postValue(result.data.data.children?.map { it.data })
                }
                is Result.Error -> {
                    exceptions.postValue(result.exception)
                }
            }
        }
    }

    fun getMe() {
        viewModelScope.launch {
            val result = repository.getMe()
            when (result) {
                is Result.Success -> {
                    me.postValue(result.data.data!!)
                }
                is Result.Error -> {
                    exceptions.postValue(result.exception)
                }
            }
        }
    }

    fun getMineComments(
        username: String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort
    ) {
        viewModelScope.launch {
            when (val result = repository.getCommentsMine(
                username,
                after,
                before,
                count,
                limit,
                srDetail,
                time,
                context,
                sort
            )) {
                is Result.Success -> {
                    comments.postValue(result.data.data.children?.map { it.data })
                }
                is Result.Error -> {
                    exceptions.postValue(result.exception)
                }
            }
        }
    }

    fun getSubredditsMine(
        userName:String,before: String?, after: String?, count: Int?, limit: Int?,srDetail:Boolean,onEmpty:()->Unit,progress: RoundCornerProgressBar
    ) {
        viewModelScope.launch {
            val result = repository.getUserSubreddits(
                userName, before, after, count, limit, srDetail
            )
            when (result) {
                is Result.Success -> {
                    val data=result.data.data.children?.map { it.data }

                    if(data.isNullOrEmpty()){
                        onEmpty.invoke()
                    }else{
                        progress.max=data.size.toFloat()
                        var index=0f
                        /*data.forEach { link->
                            progress.progress=index++
                            when(val info=repository.getSubredditAbout(link.getSubredditI())){
                                is Result.Success->{
                                    link.subInfo=info.data.data
                                }
                                is Result.Error->{
                                }
                            }
                        }*/
                        val ids=data.map { link->link.getSubredditI() }.reduce{first,second->"$first,$second"}
                        val res=repository.getSubredditsAbout(ids)
                        var infos:List<SubredditInfo>?=null
                        when(res){
                            is Result.Success->{
                                infos=res.data.data.children?.map { it.data }
                            }
                            is Result.Error->{
                                exceptions.postValue(res.exception)
                            }
                        }
                        data.forEach { link->
                            val info=infos?.find { it.displayName==link.getSubredditI() }
                            progress.progress=index++
                            link.subInfo=info
                        }
                        links.postValue(data!!)
                    }

                }
                is Result.Error -> {
                    exceptions.postValue(result.exception)
                }
            }
        }
    }
    suspend fun getInfo(subName:String)=repository.getAccountInfo(subName)

    suspend fun subscribe(action: RedditApi.SubscibeType, skip:Boolean?, srName:String)=repository.subscribe(action,skip,srName)
    suspend fun getSubredditAbout(subreddit:String)=
        repository.getSubredditAbout(subreddit)
    suspend fun vote(dir:Int,id:String,rank:Int?)=repository.vote(dir,id,rank)
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
}