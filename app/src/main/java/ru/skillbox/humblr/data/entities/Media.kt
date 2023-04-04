package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Media(val reddit_video: RedditVideo?, val type: String?, val oembed: YouTubeVideo?)