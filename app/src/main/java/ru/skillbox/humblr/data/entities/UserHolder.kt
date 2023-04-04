package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(
    generateAdapter = true,
    generator = "ru.skillbox.humblr.data.entities.UserHolderJsonAdapter"
)
data class UserHolder(val users: HashMap<String, UserInfo>)