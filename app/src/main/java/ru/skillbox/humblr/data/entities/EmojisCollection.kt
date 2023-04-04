package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(
    generateAdapter = true,
    generator = "ru.skillbox.humblr.data.entities.EmojisCollectionJsonAdapter"
)
data class EmojisCollection(val map: HashMap<String, HashMap<String, Emoji?>>)