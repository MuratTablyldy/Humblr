package ru.skillbox.humblr.news

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kohii.v1.core.Rebinder
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import retrofit2.http.Query
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import ru.skillbox.humblr.utils.MNetworkCallBack
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(val repository: MainRepository) : ViewModel() {


    val exceptions = MutableLiveData<Exception>(null)
    private val _job = MutableStateFlow<Job?>(null)
    val job: StateFlow<Job?> = _job
    val searchList= MutableStateFlow<List<Link>?>(null)

    val state = MutableLiveData(State.HOT)

    suspend fun getInfo(subName: String) = repository.getAccountInfo(subName)
    suspend fun subscribe(action: RedditApi.SubscibeType, skip: Boolean?, srName: String) =
        repository.subscribe(action, skip, srName)

    suspend fun getSubredditAbout(subreddit: String) =
        repository.getSubredditAbout(subreddit)

    suspend fun vote(dir: Int, id: String, rank: Int?) = repository.vote(dir, id, rank)

    fun bind(titleFlow: Flow<String>, callBack: MNetworkCallBack) {
        _job.value = titleFlow.debounce(200).mapLatest { query ->
            if (callBack.isAvailable()) {
                when(val result=getSearch(query)){
                    is Result.Success->{
                        val data=result.data.data.children?.map { it.data }
                        if(data.isNullOrEmpty()){
                            searchList.value= emptyList()
                        } else{
                            searchList.value=data
                        }
                    }
                    is Result.Error->{
                        exceptions.postValue(result.exception)
                    }
                }
            } else {
                exceptions.postValue(ConnectException("internet not available"))
            }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun denyJob() {
        _job.value?.cancel()
        _job.value = null
    }
    suspend fun getSearch(query: String)=repository.searchLink(query)

}

enum class State {
    LOADING, HOT, ERROR, NEW
}

//}