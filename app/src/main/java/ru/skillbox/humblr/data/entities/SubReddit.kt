package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.data.interfaces.Votable

@JsonClass(generateAdapter = true)
data class SubReddit(
    val id:String?,
    @Json(name = "account_active")
    val accountActive: Int?,
    @Json(name = "active_user_count")
    val activeUserCount:Int?,
    @Json(name = "comment_score_hide_mins")
    val commentsHideMins: Int?,
    val description: String?,
    @Json(name = "display_name")
    val displayName: String?,
    @Json(name ="header_img" )
    val headerImg: String?,
    @Json(name = "header_title")
    val headerTitle: String?,
    val over18: Boolean?,
    @Json(name = "public_description")
    val publicDescription: String?,
    @Json(name = "public_traffic")
    val publicTraffic: Boolean?,
    val subscribers: Long?,
    @Json(name = "submission_type")
    val submissionType: SubmissionType?,
    @Json(name = "submit_link_table")
    val submitLinkTable: String?,
    @Json(name = "submit_text_label")
    val submitTextLabel: String?,
    @Json(name = "subreddit_type")
    val subredditType: SubredditType?,
    val title: String?,
    val url: String?,
    val name:String?,
    val score:Int?,
    val link_flair_text:String?,
    val gildings:Gildings?,
    val clicked:Boolean?,
    val hidden:Boolean?,
    val link_flair_richtext:List<Flair>?,
    val subreddit_name_prefixed:String?,
    val secure_media_embed:Media?,
    val preview:ImageUrl?,
    val gilded:Int?,
    @Json(name = "user_is_banned")
    val userIsBanned: Boolean?,
    @Json(name = "user_is_contributor")
    val userIsContributor: Boolean?,
    @Json(name = "user_is_moderator")
    val userIsModerator: Boolean?,
    @Json(name = "user_is_subscriber")
    val userIsSubscriber: Boolean?,
    override val ups: Int?,
    override val downs: Int?,
    override val likes: Boolean?,
    override val created: Long?,
    override val createdUTC: Long?,
) :  Votable, Created {
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

enum class SubmissionType { any, link, self }
enum class SubredditType { public, private, restricted, user }