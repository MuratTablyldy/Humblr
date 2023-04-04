package ru.skillbox.humblr.data.entities
import com.squareup.moshi.*
import ru.skillbox.humblr.data.interfaces.Created
import ru.skillbox.humblr.data.interfaces.Votable

@JsonClass(
    generateAdapter = true,
    generator = "ru.skillbox.humblr.data.entities.CommentJsonAdapter"
)
data class Comment(
    val id: String?,
    override val created: Long?,
    @Json(name = "created_utc")
    override val createdUTC: Long?,
    override val ups: Int?,
    override val downs: Int?,
    override val likes: Boolean?,
    @Json(name = "approved_by")
    val approvedBy: String?,
    val author: String?,
    val name: String?,
    @Json(name = "banned_by")
    val bannedBy: String?,
    var body: String?,
    val gilded: Int?,
    @Json(name = "link_author")
    val linkAuthor: String?,
    @Json(name = "link_id")
    val linkId: String?,
    @Json(name = "link_title")
    val linkTitle: String?,
    val edited: Long?,
    @Json(name = "link_url")
    val linkUrl: String?,
    @Json(name = "num_reports")
    val numReports: Int?,
    @Json(name = "parent_id")
    val parentId: String?,
    var replies: Thing<Listing<Thing2>>,
    val saved: Boolean?,
    var score: Int?,
    @Json(name = "score_hidden")
    val scoreHidden: Boolean?,
    val subreddit: String?,
    @Json(name = "subreddit_id")
    val subredditID: String?,
    val distinguished: String?,
    val archived: Boolean?,
    val depth: Int?,
    val autorName: String?,
    var account: UserInfo?
) : Created, Votable {
    override fun getParent(): String? {
        return parentId
    }

    override fun getDepth2(): Int? {
        return depth
    }

    override fun getIds(): String? {
        return name
    }

    object LoadingComment : Created {
        override val created: Long?
            get() = null
        override val createdUTC: Long?
            get() = null

        override fun getParent(): String? {
            return null
        }

        override fun getDepth2(): Int? {
            return null
        }

        override fun getIds(): String? {
            return null
        }

    }

}
