package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageIn(val source: ImageObj, val resolutions: List<ImageObj>)