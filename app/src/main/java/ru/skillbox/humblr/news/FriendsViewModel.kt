package ru.skillbox.humblr.news

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.Account
import ru.skillbox.humblr.data.repositories.MainRepository
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(val repository: MainRepository) : ViewModel() {

    val empty = MutableLiveData(false)
    val friends = MutableLiveData<List<Account>>()
    val errors = MutableLiveData<Exception>()
    fun getFriends(before: String?, after: String?, count: Int?, limit: Int?) {
        viewModelScope.launch {
            val friend = repository.getFriends(
                before = before,
                after = after,
                count = count,
                limit = limit,
                true
            )
            when (friend) {
                is Result.Success -> {
                    val friendq = friend.data.data.children

                    if (friendq != null && friendq.isNotEmpty()) {
                        friends.postValue(friendq!!)
                    } else {
                        empty.postValue(true)
                    }

                }
                is Result.Error -> {
                    errors.postValue(friend.exception)
                }
            }
        }
    }
}