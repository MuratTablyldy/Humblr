package ru.skillbox.humblr.data

sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> RESULT_OK
            is Error -> RESULT_FAILED
            else -> {
                ""
            }
        }
    }

    companion object {
        const val RESULT_OK = "resultOk"
        const val RESULT_FAILED = "resultFailed"
    }
}