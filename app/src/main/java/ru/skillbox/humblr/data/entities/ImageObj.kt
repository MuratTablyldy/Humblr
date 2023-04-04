package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageObj(val url: String, val width: Int, val height: Int)