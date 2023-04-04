package ru.skillbox.humblr.mainPackage

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.*
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.modules.TokenHolder
import ru.skillbox.humblr.utils.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repository: MainRepository) : ViewModel() {
    private val fail: MutableLiveData<Throwable> = MutableLiveData()
    var isActive = false
    val account = MutableLiveData<Account>()
    val prefs = MutableLiveData<Prefs>()
    val tokenHold=MutableLiveData<TokenHolder?>(null)
    val error=SingleLiveEvent<Exception>(null)
    val visible=MutableLiveData(true)
    private val coroutineHandler = CoroutineExceptionHandler { _, throwable ->
        fail.postValue(
            throwable
        )
    }
    val internetAvailable=SingleLiveEvent(true)

    fun fetchAuthToken() {
        tokenHold.postValue(repository.fetchAuthToken())
    }

    fun getMe() {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            val thing = repository.getMe()
            when(thing){
                is Result.Success->{
                    val data=thing.data.data as Account
                    account.postValue(data)
                } else ->{
                    val data=thing as Result.Error
                error.postValue(data.exception)
                }
            }
        }

    }

    fun getTrophies() {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            val trophies = repository.getTrophies(viewModelScope)
            Log.d("trophies", trophies.toString())
        }
    }

    @InternalCoroutinesApi
    fun subscribe(lifecycleOwner: LifecycleOwner, doWork: (Boolean) -> Unit){
        repository.subscribe(lifecycleOwner, doWork)
    }

    fun isTokenValid(tokenHolder: TokenHolder?):Boolean{
        val isValid=if(tokenHolder==null) false else repository.isTokenValid(tokenHolder)
        if(isValid){
            isActive=true
        }
       return isValid
    }

    fun getSubredditAbout(subreddit: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            val list = repository.getSubredditAbout(subreddit)

        }
    }


    fun getSubredditLIst(
        id: Array<String>?,
        names: Array<String>?,
        url: String?,
        subreddit: String
    ) {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            val list = repository.getSubredditList(viewModelScope, id, names, subreddit, url)
            Log.d("subreddit", list.data.toString())
        }
    }

    fun getSubredditComments(
        subreddit: String,
        id: String,
        article: String,
        comment: String?,
        context: Int,
        depth: Int?,
        limit: Int?,
        showedits: Boolean,
        showmedia: Boolean,
        showmore: Boolean,
        showtitle: Boolean,
        sort: String,
        threaded: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            try {
                val list = repository.getSubredditComments(
                    viewModelScope,
                    subreddit,
                    id,
                    article,
                    comment,
                    context,
                    depth,
                    limit,
                    showedits,
                    showmedia,
                    showmore,
                    showtitle,
                    sort,
                    threaded
                )
                list.forEach { Log.d(it.kind, it.data.children.toString()) }
            } catch (e: Exception) {
                Log.d("exception", e.toString())
            }
        }
    }

    fun getSubredditRules(subreddit: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            val list = repository.getSubredditRules(viewModelScope, subreddit)
            Log.d("subreddit", list.toString())
        }
    }

    fun getSubredditEmojis(subreddit: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            val list = repository.getSubredditEmojis(viewModelScope, subreddit)
            Log.d("subreddit", list.toString())
        }
    }

    fun vote(dir: Int, id: String, rank: Int) {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            val list = repository.vote( dir, id, rank)
        }
    }

    fun createOrEditSubreddit(subredditCreator: SubredditCreator) {
        viewModelScope.launch(Dispatchers.IO + coroutineHandler + SupervisorJob()) {
            val list = repository.createOrEditSubreddit(viewModelScope, subredditCreator)
            Log.d("subreddit", list.isSuccessful.toString())
        }
    }

}