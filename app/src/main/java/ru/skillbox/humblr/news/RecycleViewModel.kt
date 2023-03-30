package ru.skillbox.humblr.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kohii.v1.core.Rebinder
import kohii.v1.media.VolumeInfo
import kotlinx.coroutines.launch
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Link
import ru.skillbox.humblr.data.entities.SubredditInfo
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import javax.inject.Inject

@HiltViewModel
class RecycleViewModel @Inject constructor(val repository: MainRepository) : ViewModel() {
    val links = MutableLiveData<List<Link>>()
    private val _selectedRebinder = MutableLiveData<Pair<Int, Rebinder?>>(-1 to null)
    fun setRebinder(value: Pair<Int, Rebinder?>) {
        _selectedRebinder.postValue(value)
    }

    val exceptions = MutableLiveData<Exception>(null)
    val rebinder: LiveData<Pair<Int, Rebinder?>>
        get() = _selectedRebinder

    val recyclerViewVolume = MutableLiveData(VolumeInfo(true, 1F))

    val state = MutableLiveData(State.INIT)

    suspend fun getInfo(subName: String) = repository.getAccountInfo(subName)
    suspend fun subscribe(action: RedditApi.SubscibeType, skip: Boolean?, srName: String) =
        repository.subscribe(action, skip, srName)

    suspend fun vote(dir: Int, id: String, rank: Int?) = repository.vote(dir, id, rank)


    fun getSubredditHot(
        language: String?,
        before: String?,
        after: String?,
        progress: RoundCornerProgressBar
    ) {
        viewModelScope.launch {
            var prog = 0f
            when (val result = repository.getSubredditsHot(language, before, after, 10)) {
                is Result.Success -> {
                    prog++
                    progress.progress = prog
                    val linksList = result.data.data.children?.map { thing -> thing.data }
                    if (linksList != null) {
                        val names = linksList.map { link ->
                            link.getSubredditI()
                        }.reduce { first, second -> "$first,$second" }
                        val about = repository.getSubredditsAbout(names)
                        var info: List<SubredditInfo>? = null
                        when (about) {
                            is Result.Success -> {
                                info = about.data.data.children!!.map { it.data }
                            }
                            is Result.Error -> {
                                exceptions.postValue(about.exception)
                            }
                        }
                        progress.progress = ++prog
                        linksList.forEach { link ->

                            val data = info?.find { link.getSubredditI() == it.displayName }
                            link.subInfo = data

                        }
                        progress.progress = ++prog
                        links.postValue(linksList!!)
                        state.postValue(State.HOT_LOADED)
                    } else {
                        exceptions.postValue(java.lang.NullPointerException("unreached"))
                    }
                }
                is Result.Error -> {
                    exceptions.postValue(result.exception)
                }
            }
        }
    }

    fun getSubredditNew(
        language: String?,
        before: String?,
        after: String?,
        progress: RoundCornerProgressBar
    ) {
        viewModelScope.launch {
            var prog = 0f
            when (val result = repository.getSubredditsNew(language, before, after, 10)) {
                is Result.Success -> {
                    progress.progress = ++prog
                    val linksList = result.data.data.children?.map { thing -> thing.data }
                    if (linksList != null) {
                        progress.max = linksList.size.toFloat()
                        val names = linksList.map { link ->
                            link.getSubredditI()
                        }.reduce { first, second -> "$first,$second" }
                        val about = repository.getSubredditsAbout(names)
                        var info: List<SubredditInfo>? = null
                        when (about) {
                            is Result.Success -> {
                                info = about.data.data.children!!.map { it.data }
                                progress.progress = ++prog
                            }
                            is Result.Error -> {
                                exceptions.postValue(about.exception)
                            }
                        }
                        linksList.forEach { link ->
                            val data = info?.find { link.getSubredditI() == it.displayName }
                            link.subInfo = data
                        }
                        progress.progress = ++prog
                        links.postValue(linksList!!)
                        state.postValue(State.NEW_LOADED)
                    } else {
                        exceptions.postValue(java.lang.NullPointerException("unreached"))
                    }
                }
                is Result.Error -> {
                    exceptions.postValue(result.exception)
                }
            }
        }

    }

    enum class State {
        HOT, ERROR, NEW, INIT, HOT_LOADED, NEW_LOADED
    }

}