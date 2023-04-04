package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaYouTube(val content: String?, val width: Int?, val height: Int?)