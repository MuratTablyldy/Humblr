package ru.skillbox.humblr.news

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kohii.v1.core.Rebinder
import kohii.v1.media.VolumeInfo
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.*
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.RedditApi
import javax.inject.Inject

@HiltViewModel
class FullScreenModel @Inject constructor(val repository: MainRepository) : ViewModel() {
    val overlayVolume = MutableLiveData(VolumeInfo(false, 1F))

    val playState = MutableLiveData(false)
    val link = MutableLiveData<String?>(null)
    val linkItem = MutableLiveData<Link>(null)
    val subInfo = MutableLiveData<SubredditInfo>()
    val rebinder = MutableLiveData<Rebinder?>(null)


    suspend fun getInfo(subName: String) = repository.getAccountInfo(subName)
    suspend fun subscribe(action: RedditApi.SubscibeType, skip: Boolean?, srName: String) =
        repository.subscribe(action, skip, srName)

    suspend fun getSubredditAbout(subreddit: String) =
        repository.getSubredditAbout(subreddit)

    suspend fun vote(dir: Int, id: String, rank: Int?) = repository.vote(dir, id, rank)
    suspend fun save(fullname: String, category: String): Boolean {
        return when (repository.save(fullname = fullname, category = category)) {
            is Result.Success -> {
                true
            }
            is Result.Error -> {
                false
            }
        }
    }

    suspend fun unsave(fullname: String): Boolean {
        return when (repository.unsave(fullname = fullname)) {
            is Result.Success -> {
                true
            }
            is Result.Error -> {
                false
            }
        }
    }
}