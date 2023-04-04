package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.skillbox.humblr.data.interfaces.Created

@JsonClass(generateAdapter = true)
data class Message(
    override val created: Long,
    override val createdUTC: Long,
    val author: String,
    val body: String,
    val likes: Boolean?,
    @Json(name = "link_title")
    val linkTitle: String,
    val name: String,
    val unread: Boolean,
    @Json(name = "parent_id")
    val parentId: String?,
    val message: Message?,
    val replies: String,
    val subject: String,
    val subreddit: String?
) : Created {
    override fun getParent(): String? {
        return parentId
    }

    override fun getDepth2(): Int? {
        return 0
    }

    override fun getIds(): String? {
        return name
    }

}