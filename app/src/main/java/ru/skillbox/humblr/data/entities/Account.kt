package ru.skillbox.humblr.data.entities


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.skillbox.humblr.data.interfaces.Created

@JsonClass(generateAdapter = true)
data class Account(
    val id: String?,
    val carma: Int?,

    @Json(name = "is_employee")
    val isEmployee: Boolean?,
    @Json(name = "awardee_karma")
    val karma: Int?,
    @Json(name = "has_mail")
    val hasMail: Boolean?,
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
    @Json(name = "mod_hash")
    val modHash: String?,
    val name: String?,
    @Json(name = "over_18")
    val over18: Boolean?,
    val subreddit: SubReddit?,
    override val created: Long?,
    @Json(name = "created_utc")
    override val createdUTC: Long?,
    @Json(name = "icon_img")
    var icon: String?,
    @Json(name = "snoovatar_img")
    val snoovatar: String?
) : Created {
    override fun getParent(): String {
        return ""
    }

    override fun getDepth2(): Int {
        return 0
    }

    override fun getIds(): String? {
        return id
    }
}



