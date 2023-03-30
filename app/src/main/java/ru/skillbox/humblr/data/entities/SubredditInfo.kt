package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubredditInfo(
    @Json(name = "user_flair_background_color")
    val flairColor: String?,
    @Json(name = "restrict_posting")
    val restrict: Boolean?,
    @Json(name = "user_is_banned")
    val isBanned:Boolean?,
    @Json(name = "user_can_flair_in_sr")
    val canFlair:Boolean?,
    @Json(name = "user_is_muted")
    val userIsMuted:Boolean?,
    @Json(name = "display_name")
    val displayName:String?,
    @Json(name = "header_img")
    val headerImage:String?,
    val title:String?,
    @Json(name = "primary_color")
    val primaryColor:String?,
    @Json(name = "active_user_count")
    val activeUserCount:Int?,
    @Json(name = "icon_img")
    val iconImage:String?,
    @Json(name = "accounts_active")
    val accountActive:Int?,
    val subscribers:Int?,
    val name:String?,
    @Json(name = "emojis_enabled")
    val emojisEnabled:Boolean?,
    @Json(name = "public_description")
    val publicDescription:String?,
    @Json(name = "community_icon")
    val comunityIcon:String?,
    @Json(name = "submit_text")
    val submitText:String?,
    @Json(name = "description_html")
    val descriptionHtml:String?,
    @Json(name = "allow_talks")
    val allowTalks:Boolean?,
    @Json(name = "user_is_subscriber")
    val userIsSubscriber:Boolean?,
    @Json(name = "accept_followers")
    val acceptFollowers:Boolean?,
    @Json(name = "allow_videogifs")
    val allowGifs:Boolean?,
    @Json(name = "allow_polls")
    val allowPolls:Boolean?,
    @Json(name = "allow_galleries")
    val allowGalleries:Boolean?,
    @Json(name = "comment_contribution_settings")
    val commentContrSettings:ContSettings?,
    @Json(name = "allow_videos")
    val allowVideos:Boolean?,
    val allow_images:Boolean?,
    val id:String?,
    val key_color:String?,
    val can_assign_user_flair:Boolean?,
    val user_flair_type:String?,
    val mobile_banner_image:String?,
    val user_is_moderator:Boolean?,
    val  description:String?,
    val over18:Boolean?
) {
   /* "collapse_deleted_comments":false,
    "emojis_custom_size":null,
    "public_description_html":"&lt;!-- SC_OFF --&gt;&lt;div class=\"md\"&gt;&lt;p&gt;A subreddit for discussion of Reddit&amp;#39;s API and Reddit API clients.&lt;/p&gt;\n&lt;/div&gt;&lt;!-- SC_ON --&gt;",
    "is_crosspostable_subreddit":true,
    "notification_level":null,
    "should_show_media_in_comments_setting":true,
    "can_assign_link_flair":true,
    "accounts_active_is_fuzzed":false,
    "allow_prediction_contributors":false,
    "submit_text_label":"",
    "link_flair_position":"left",
    "user_sr_flair_enabled":true,
    "user_flair_enabled_in_sr":true,
    "allow_chat_post_creation":false,
    "allow_discovery":true,

    "user_sr_theme_enabled":true,
    "link_flair_enabled":true,
    "disable_contributor_requests":false,
    "subreddit_type":"public",
    "suggested_comment_sort":null,
    "banner_img":"",
    "user_flair_text":null,
    "banner_background_color":"#666666",
    "header_title":"from reddit import code",
   "is_chat_post_feature_enabled":true,
    "submit_link_label":"",
    "user_flair_text_color":null,
    "restrict_commenting":false,*/
}
@JsonClass(generateAdapter = true)
data class ContSettings(
    @Json(name = "allowed_media_types")
    val mediaTypes:Array<String>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContSettings

        if (!mediaTypes.contentEquals(other.mediaTypes)) return false

        return true
    }

    override fun hashCode(): Int {
        return mediaTypes.contentHashCode()
    }
}