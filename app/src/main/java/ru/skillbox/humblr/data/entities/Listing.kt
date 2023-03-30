package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Listing<T>(
    val before: String?,
    val after: String?,
    val modhash: String?,
    var children: List<T>?
)