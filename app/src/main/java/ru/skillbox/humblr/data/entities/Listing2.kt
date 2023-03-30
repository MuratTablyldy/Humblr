package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Listing2<T>(
    val trophies: List<T>
)