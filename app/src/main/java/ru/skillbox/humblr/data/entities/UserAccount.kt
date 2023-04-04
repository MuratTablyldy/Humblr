package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.skillbox.humblr.data.interfaces.Created

@JsonClass(generateAdapter = true)
data class UserAccount(
    val id: String,
    val carma: Int?,
    @Json(name = "has_mail")
    val hasMail: Boolean,
    @Json(name = "has_mod_mail")
    val hasModMail: Boolean?,
    @Json(name = "has_verified_email")
    val hasVerifiedEmail: Boolean?,
    @Json(name = "inbox_count")
    var inboxCount: Int?,
    @Json(name = "is_friend")
    val isFriend: Boolean?,
    @Json(name = "is_gold")
    val isGold: Boolean?,
    @Json(name = "is_mod")
    val isMod: Boolean?,
    @Json(name = "link_karma")
    val linkKarma: Int?,
    val is_employee: Boolean,
    @Json(name = "mod_hash")
    val modHash: String?,
    val subreddit: SubReddit,
    val name: String,
    @Json(name = "over_18")
    val over18: Boolean?,
    override val created: Long,
    @Json(name = "created_utc")
    override val createdUTC: Long,
    val icon_img: String,
    @Json(name = "snoovatar_img")
    val snoovatarImg: String
) : Created {
    override fun getParent(): String? {
        return null
    }

    override fun getDepth2(): Int? {
        return null
    }

    override fun getIds(): String? {
        return id
    }
}