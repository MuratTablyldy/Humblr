package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageUrl(val images: List<ImageIn>)