package ru.skillbox.humblr.ui.login

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.skillbox.humblr.data.repositories.AuthRepository
import ru.skillbox.humblr.data.repositories.modules.TokenHolder
import ru.skillbox.humblr.utils.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _openAuthPageEvent = SingleLiveEvent<Uri>()
    private val _isRegistered=SingleLiveEvent<TokenHolder>(null)
    val isRegistered:LiveData<TokenHolder> =_isRegistered
    var hasToken=false

    val openAuthPageEvent: LiveData<Uri> = _openAuthPageEvent

    fun handleFragment(fragment:String){
        if(!fragment.contains("error")){
            _isRegistered.postValue(authRepository.handleFragment(fragment))
        } else{
            val error = """error=(.+)$""".toRegex().find(fragment)?.groupValues?.get(1)
            handleError(error!!)
        }

    }
    fun fetchLogin():TokenHolder{
        return authRepository.fetch()
    }
    fun openLoginPage() {
        val authUri=authRepository.getAuthUri()
        _openAuthPageEvent.postValue(authUri)
    }
    fun handleError(error:String){
        Log.d("error",error)
    }

    fun startWorker(token:String, expTime:Long, tokenType:String, context: Context){
        authRepository.startWorker(token, expTime, tokenType, context)
    }
    fun subscribeWorker(context: Context,lifecycleOwner: LifecycleOwner,onWork:(WorkInfo)->Unit){
        authRepository.subscribeWorkInfo(context,lifecycleOwner,onWork)
    }
    fun isWorkerRunning(context: Context):Boolean{
        return authRepository.isWorkerRunning(context)
    }
    fun isFirstTime():Boolean{
        return authRepository.isFirstTime()
    }
    fun setFirst(){
        return authRepository.setFirstTime()
    }

}