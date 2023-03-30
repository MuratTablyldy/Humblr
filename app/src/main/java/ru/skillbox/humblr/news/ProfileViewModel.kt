package ru.skillbox.humblr.news

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.*
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(val repository: MainRepository) : ViewModel() {

    private val errors = MutableLiveData<Exception>()
    val account = MutableLiveData<Account>()
    val subreddits = MutableLiveData<List<SubReddit>>()
    val mineComments = MutableLiveData<List<Comment>?>()
    fun getMe() {
        viewModelScope.launch {
            when (val me = repository.getMe()) {
                is Result.Success -> {
                    val data = me.data.data
                    account.postValue(data)
                }
                is Result.Error -> {
                    errors.postValue(me.exception)
                }
            }
        }
    }

    fun getSubreddits(before: String?, after: String?, count: Int?, limit: Int?) {
        viewModelScope.launch {
            when (val subreddits = repository.getMineSubreddits(
                before = before,
                after = after,
                count = count,
                limit = limit,
                true
            )) {
                is Result.Success -> {
                    this@ProfileViewModel.subreddits.postValue(subreddits.data.data.children)
                }
                is Result.Error -> {
                    this@ProfileViewModel.errors.postValue(subreddits.exception)
                }
            }
        }
    }

    fun getCommentsMine(
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
            val result = repository.getCommentsMine(
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
                    val things = result.data.data.children?.map { it.data }
                    mineComments.postValue(things)
                }
                is Result.Error -> {
                    errors.postValue(result.exception)
                }
            }
        }
    }

    suspend fun getCommentsSaved(
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort
    ) = repository.getCommentsSaved(
        account.value?.name!!,
        after,
        before,
        count,
        limit,
        srDetail,
        time,
        context,
        sort
    )

    suspend fun save(
        category: String,
        fullname: String
    ) = repository.save(category, fullname)

    suspend fun unsave(
        fullname: String
    ) = repository.unsave(fullname)

    suspend fun getSavedSubreddit(
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort
    ) = repository.getSubredditSaved(
        account.value?.name!!,
        after,
        before,
        count,
        limit,
        srDetail,
        time,
        context,
        sort
    )

    fun exit() {
        repository.revokeToken()
    }
}