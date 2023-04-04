package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(
    generateAdapter = true,
    generator = "ru.skillbox.humblr.data.entities.MediaMetadataJsonAdapter"
)
data class MediaMetadata(val list: HashMap<String, MediaMet>)

@JsonClass(generateAdapter = true)
data class MediaMet(val p: List<ImageArr>?)

@JsonClass(generateAdapter = true)
data class ImageArr(val u: String)