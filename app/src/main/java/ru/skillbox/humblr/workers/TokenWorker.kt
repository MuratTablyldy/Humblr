package ru.skillbox.humblr.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.*
import ru.skillbox.humblr.data.repositories.AuthSessionService
import ru.skillbox.humblr.data.repositories.SessionManager
import ru.skillbox.humblr.utils.State
import kotlin.coroutines.resume

class TokenWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val authSessionService = AuthSessionService()
    private val sessionManager = SessionManager(context = context)

    override suspend fun doWork(): Result {
        val token = inputData.getString(TOKEN)
        val tokenType = inputData.getString(TOKEN_TYPE)
        val revokePath = inputData.getString(REVOKE_PATH)
        return if (token != null && tokenType != null && revokePath != null) {
            val job = suspendCancellableCoroutine<Result> { continuation ->
                continuation.invokeOnCancellation {
                    continuation.cancel(it)
                }
                val respose = authSessionService.revokeToken(
                    revokePath,
                    token,
                    tokenType
                )
                if (respose.isSuccess) {
                    State.getInstance().expired.value = true
                    sessionManager.removeToken()
                    continuation.resume(Result.success())
                } else {
                    val failure = respose.exceptionOrNull()
                    val data = Data.Builder().putString(ERROR, failure.toString()).build()
                    State.getInstance().error.value = failure
                    continuation.resume(Result.failure(data))
                }
            }
            return job
        } else {
            val data = Data.Builder()
                .putString(
                    ERROR,
                    "token=$token, token_type=$tokenType, revoke_path=$revokePath"
                ).build()
            return Result.failure(data)
        }
    }

    companion object {
        const val TOKEN = "token"
        const val TOKEN_TYPE = "token_type"
        const val REVOKE_PATH = "revoke_path"
        const val ERROR = "error"
    }
}