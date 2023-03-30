package ru.skillbox.humblr.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kohii.v1.core.Rebinder
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.launch
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Account
import ru.skillbox.humblr.data.entities.Comment
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import javax.inject.Inject

@HiltViewModel
class ProfileUserViewModel @Inject constructor(val repository: MainRepository):ViewModel() {

    val userLinks= MutableLiveData<List<Link>?>()
    val errors=MutableLiveData<Exception>()
    val account = MutableLiveData<Account>()
    val comments=MutableLiveData<List<Comment>>()
    val me=MutableLiveData<Account>()
    val linkState=MutableLiveData(State.INIT)
    private val _selectedRebinder= MutableLiveData<Pair<Int, Rebinder?>>(-1 to null)
    fun setRebinder(value: Pair<Int, Rebinder?>){
        _selectedRebinder.postValue(value)
    }

    val exceptions= MutableLiveData<Exception>(null)
    val rebinder: LiveData<Pair<Int, Rebinder?>>
        get() = _selectedRebinder

    val recyclerViewVolume = MutableLiveData(VolumeInfo(true, 1F))

    val state= MutableLiveData(RecycleViewModel.State.INIT)

    suspend fun getSubredditAbout(subreddit:String)=
        repository.getSubredditAbout(subreddit)
    suspend fun vote(dir:Int,id:String,rank:Int?)=repository.vote(dir,id,rank)


    fun getUserLinks(
        username:String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail:Boolean
    ){
        viewModelScope.launch {
            val result=repository.getUserSubreddits(
                username,
                after,
                before,
                count,
                limit,
                srDetail
            )
            when(result){
                is Result.Success->{
                    val things=result.data.data.children?.map { it.data }
                    userLinks.postValue(things)
                }
                is Result.Error->{
                    errors.postValue(result.exception)
                }
            }
        }
    }
    suspend fun getInfo(subName: String){
        when(val info = repository.getAccountInfo(subName)){
            is Result.Success->{
                account.postValue(info.data.data!!)
            }
            is Result.Error->{
                errors.postValue(info.exception)
            }
        }
    }
    fun getCommentsMine(
        username:String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail:Boolean?,

        time: RedditApi.Time,
        context:Int,
        sort: RedditApi.Sort
    ){
        viewModelScope.launch {
            val result=repository.getCommentsMine(
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
            when(result){
                is Result.Success->{
                    val things=result.data.data.children?.map { it.data }
                    if(things!=null){
                        comments.postValue(things!!)
                    }
                }
                is Result.Error->{
                    errors.postValue(result.exception)
                }
            }
        }
    }
    suspend fun subscribe(action: RedditApi.SubscibeType, skip:Boolean?, srName:String)=repository.subscribe(action,skip,srName)
    suspend fun sendMessage(subject:String,text:String,to:String)=repository.sendMessage(subject, text, to)
    suspend fun getMe(){
        when(val res=repository.getMe()){
            is Result.Success->{
                me.postValue(res.data.data!!)
            }
            is Result.Error->{
                errors.postValue(res.exception)
            }
        }
    }

    enum class State{
        EXPANDED,NOT,INIT
    }
    companion object {
        const val SWITCH_BOUND = 0.8f
        const val TO_EXPANDED = 0
        const val TO_COLLAPSED = 1
        const val WAIT_FOR_SWITCH = 0
        const val SWITCHED = 1
    }
}