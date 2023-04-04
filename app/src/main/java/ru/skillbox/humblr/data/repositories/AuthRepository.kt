package ru.skillbox.humblr.data.repositories

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import ru.skillbox.humblr.data.repositories.modules.TokenHolder
import ru.skillbox.humblr.utils.State
import ru.skillbox.humblr.workers.TokenWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authConfig: AuthProperties,
    private val sessionManager: SessionManager
) {
    fun getAuthUri(): Uri {
        return Uri.parse(
            "${authConfig.authUri}?client_id=${authConfig.clientId}&response_type=${authConfig.responseType}&" +
                    "state=${authConfig.state}&redirect_uri=${authConfig.redirectUri}&scope=${authConfig.scope}"
        )
    }

    fun handleFragment(fragment: String): TokenHolder {
        val values =
            """(access_token=.+&|$)(token_type=.+&|$)(state=.+&|$)(expires_in=.+&|$)""".toRegex()
                .find(fragment)?.groupValues
        val parameters = values!!.associate { parameter ->
            val parts = parameter.split("=")
            val key = parts[0]
            val value = if (parts[1].contains("&")) {
                parts[1].removeSuffix("&")
            } else parts[1]
            key to value
        }
        val tokenHolder = TokenHolder(parameters)
        sessionManager.saveAuthToken(tokenHolder)
        State.getInstance().expired.value = false
        return tokenHolder
    }

    fun startWorker(token: String, expTime: Long, tokenType: String, context: Context) {
        val workConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workData =
            workDataOf(
                TokenWorker.REVOKE_PATH to authConfig.revokePath,
                TokenWorker.TOKEN_TYPE to tokenType,
                TokenWorker.TOKEN to token
            )
        val workRequest = OneTimeWorkRequestBuilder<TokenWorker>()
            .setConstraints(workConstraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 20L, TimeUnit.SECONDS)
            .setInitialDelay(expTime, TimeUnit.SECONDS)
            .setInputData(workData)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORKER_ID, ExistingWorkPolicy.REPLACE, workRequest)
    }

    fun subscribeWorkInfo(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        onWork: (WorkInfo) -> Unit
    ) {
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(WORKER_ID)
            .observe(lifecycleOwner) {
                it.forEach { work -> onWork(work) }
            }
    }

    fun fetch(): TokenHolder {
        return sessionManager.fetchAuthToken()
    }

    fun isWorkerRunning(context: Context): Boolean {
        return WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(WORKER_ID)
            .hasActiveObservers()
    }

    companion object {
        const val WORKER_ID = "grgr"
    }

    fun isFirstTime(): Boolean {
        return sessionManager.isFirstTime()
    }

    fun setFirstTime() {
        sessionManager.setFirst()
    }

}
