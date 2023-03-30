package ru.skillbox.humblr.news

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.skillbox.humblr.data.repositories.MainRepository
import javax.inject.Inject

@HiltViewModel
class WriteMessageViewModel @Inject constructor(val repository: MainRepository):ViewModel() {


    suspend fun sendMessage(subject:String,text:String,to:String)=repository.sendMessage(subject, text, to)
}