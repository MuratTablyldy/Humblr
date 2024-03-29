package ru.skillbox.humblr.data.repositories

import okhttp3.Interceptor
import okhttp3.Response

class MInterceptor(private var cancelList: MutableList<String>) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val value = request.header("fragment")

        if (value != null && cancelList.contains(value)) {
            chain.call().cancel()
            cancelList.remove(value)
        }
        return chain.proceed(
            chain.request().newBuilder().removeHeader("fragment")
                .build()
        )
    }

    fun addToCancelList(url: String) {
        cancelList.add(url)
    }

    fun removeDeathPile(url: String) {
        cancelList = ArrayList()
    }
}