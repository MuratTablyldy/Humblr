package ru.skillbox.humblr.data.entities

import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.data.interfaces.Votable

@JsonClass(generateAdapter = true, generator = "ru.skillbox.humblr.data.entities.LinkJsonAdapter")
sealed class Link(

) {
    data class LinkText(
        override val created: Long,
        @Json(name = "created_utc")
        override val createdUTC: Long,
        override val ups: Int,
        override val downs: Int,
        override val likes: Boolean?,
        val author: String?,
        val clicked: Boolean,
        val domain: String,
        val hidden: Boolean,
        @Json(name = "is_self")
        val isSelf: Boolean,
        val locked: Boolean,
        @Json(name = "secure_media")
        val mediaEmbed: Media?,
        @Json(name = "num_comments")
        val numComments: String,
        val preview: ImageUrl?,
        @Json(name = "over_18")
        val over18: Boolean,
        val permalink: String,
        val saved: Boolean,
        var score: Int,
        val selftext: String,
        val subreddit: String,
        @Json(name = "subreddit_id")
        val subredditId: String,
        val thumbnail: String,
        val title: String,
        val url: String,
        val edited: Any?,
        val distinguished: String?,
        val stickied: Boolean,
        val name: String?
    ) : Votable, Created, Link() {
        override fun getSubredditI(): String {
            return subreddit
        }

        override fun getLink(): String {

            val perma = permalink.substring(1)
            Handler(Looper.getMainLooper()).post {
                Log.d("text", perma)
            }
            return perma
        }

        override fun getParent(): String {
            return subredditId
        }

        override fun getDepth2(): Int {
            return 0
        }

        override fun getIds(): String? {
            return name
        }

        override fun getAutor(): String {
            if (author == null) return ""
            return author
        }
    }

    data class LinkYouTube(
        override val created: Long,
        @Json(name = "created_utc")
        override val createdUTC: Long,
        override val ups: Int,
        override val downs: Int,
        override val likes: Boolean?,
        val author: String?,
        val clicked: Boolean,
        val domain: String,
        val hidden: Boolean,
        @Json(name = "is_self")
        val isSelf: Boolean,
        val locked: Boolean,
        val media: MediaYouTube?,
        @Json(name = "secure_media")
        val secureMedia: Media?,
        @Json(name = "num_comments")
        val numComments: String,
        val preview: ImageUrl?,
        @Json(name = "over_18")
        val over18: Boolean,
        val permalink: String,
        val saved: Boolean,
        var score: Int,
        val selftext: String,
        val subreddit: String,
        @Json(name = "subreddit_id")
        val subredditId: String,
        val thumbnail: String,
        val title: String,
        val url: String,
        val edited: Any?,
        val distinguished: String?,
        val stickied: Boolean,
        val name: String?
    ) : Votable, Created, Link() {
        val youtubeId: String?
            get() = "/embed/(.+)\\?".toRegex()
                .find(secureMedia!!.oembed!!.html)?.groupValues?.get(1)

        override fun getSubredditI(): String {
            return subreddit
        }

        override fun getLink(): String {
            return url
        }

        override fun getParent(): String {
            return subredditId
        }

        override fun getDepth2(): Int {
            return 0
        }

        override fun getIds(): String? {
            return name
        }

        override fun getAutor(): String {
            if (author == null) return ""
            return author
        }
    }

    data class LinkPict(
        override val created: Long,
        @Json(name = "created_utc")
        override val createdUTC: Long,
        override val ups: Int,
        override val downs: Int,
        override val likes: Boolean?,
        val author: String?,
        val clicked: Boolean,
        val domain: String,
        val hidden: Boolean,
        @Json(name = "is_self")
        val isSelf: Boolean,
        val locked: Boolean,
        @Json(name = "secure_media")
        val mediaEmbed: Media?,
        @Json(name = "num_comments")
        val numComments: String,
        val preview: ImageUrl?,
        @Json(name = "over_18")
        val over18: Boolean,
        val permalink: String,
        val saved: Boolean,
        var score: Int,
        val selftext: String,
        val subreddit: String,
        @Json(name = "subreddit_id")
        val subredditId: String,
        val thumbnail: String,
        val title: String,
        val url: String,
        val edited: Any?,
        val distinguished: String?,
        val stickied: Boolean,
        val mediaMetadata: MediaMetadata?,
        val name: String?
    ) : Votable, Created, Link() {
        override fun getSubredditI(): String {
            return subreddit
        }

        override fun getLink(): String {
            val perma = permalink.substring(1)
            Handler(Looper.getMainLooper()).post {
                Log.d("text", perma)
            }
            return perma
        }

        override fun getParent(): String {
            return subredditId
        }

        override fun getDepth2(): Int {
            return 0
        }

        override fun getIds(): String? {
            return name
        }

        fun getImages(): List<String> {
            return preview?.images?.map { it.source.url }
                ?: mediaMetadata?.list!!.map { it.value.p?.last()!!.u }
        }

        override fun getAutor(): String {
            if (author == null) return ""
            return author
        }
    }

    data class LinkRedditVideo(
        override val created: Long,
        @Json(name = "created_utc")
        override val createdUTC: Long,
        override val ups: Int,
        override val downs: Int,
        override val likes: Boolean?,
        val author: String?,
        val clicked: Boolean,
        val domain: String,
        val hidden: Boolean,
        @Json(name = "is_self")
        val isSelf: Boolean,
        val locked: Boolean,
        @Json(name = "secure_media")
        val mediaEmbed: Media?,
        @Json(name = "num_comments")
        val numComments: String,
        val preview: ImageUrl?,
        @Json(name = "over_18")
        val over18: Boolean,
        val permalink: String,
        val saved: Boolean,
        var score: Int,
        val selftext: String,
        val subreddit: String,
        @Json(name = "subreddit_id")
        val subredditId: String,
        val thumbnail: String,
        val title: String,
        val url: String,
        val edited: Any?,
        val distinguished: String?,
        val stickied: Boolean,
        val name: String?
    ) : Votable, Created, Link() {
        override fun getSubredditI(): String {
            return subreddit
        }

        override fun getLink(): String {
            val perma = permalink.substring(1)
            return perma
        }

        override fun getParent(): String {
            return subredditId
        }

        override fun getDepth2(): Int {
            return 0
        }

        override fun getIds(): String? {
            return name
        }

        override fun getAutor(): String {
            if (author == null) return ""
            return author
        }
    }

    data class LinkOut(
        override val created: Long,
        @Json(name = "created_utc")
        override val createdUTC: Long,
        override val ups: Int,
        override val downs: Int,
        override val likes: Boolean?,
        val author: String?,
        val clicked: Boolean,
        val domain: String,
        val hidden: Boolean,
        @Json(name = "is_self")
        val isSelf: Boolean,
        val locked: Boolean,
        @Json(name = "secure_media")
        val mediaEmbed: Media?,
        @Json(name = "num_comments")
        val numComments: String,
        val preview: ImageUrl?,
        @Json(name = "over_18")
        val over18: Boolean,
        val permalink: String,
        val saved: Boolean,
        var score: Int,
        val selftext: String,
        val subreddit: String,
        @Json(name = "subreddit_id")
        val subredditId: String,
        val thumbnail: String,
        val title: String,
        val url: String,
        val edited: Any?,
        val distinguished: String?,
        val stickied: Boolean,
        val mediaMetadata: MediaMetadata?,
        val name: String?
    ) : Votable, Created, Link() {
        override fun getSubredditI(): String {
            return subreddit
        }

        override fun getLink(): String {
            val perma = permalink.substring(1)
            return perma
        }

        override fun getAutor(): String {
            if (author == null) return ""
            return author
        }

        override fun getParent(): String {
            return subredditId
        }

        override fun getDepth2(): Int {
            return 0
        }

        override fun getIds(): String? {
            return name
        }

        fun getImages(): List<String> {
            return preview?.images?.map { it.source.url }
                ?: mediaMetadata?.list!!.map { it.value.p?.last()!!.u }
        }
    }

    var subInfo: SubredditInfo? = null

    abstract fun getSubredditI(): String
    abstract fun getLink(): String
    abstract fun getAutor(): String

    object LoadingLink : Link() {
        override fun getSubredditI(): String {
            return ""
        }

        override fun getLink(): String {
            return ""
        }

        override fun getAutor(): String {
            return ""
        }
    }
}
