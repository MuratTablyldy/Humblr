package ru.skillbox.humblr.data.repositories


import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import retrofit2.Response
import retrofit2.http.*
import ru.skillbox.humblr.data.entities.*
import retrofit2.http.Url

import okhttp3.ResponseBody
import retrofit2.Call

import retrofit2.http.GET


interface RedditApi {
    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/me")
    suspend fun getMe(
        @Header("Authorization") token: String,
        @Header("User-Agent") agent: String
    ): Thing<Account>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/me/karma")
    suspend fun getKarma(@Header("Authorization") token: String): UserAccount

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/me/prefs")
    suspend fun getPrefs(@Header("Authorization") token: String): Prefs

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @PATCH("/api/v1/me/prefs")
    suspend fun savePrefs(
        @Header("Authorization") token: String,
        @Header("User-Agent") agent: String,
        @Body prefs: PrefsSave
    ): Response<Unit>
    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/me/trophies")
    suspend fun getTrophies(@Header("Authorization") token: String): Thing<Listing2<Thing<Trophie>>>
    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/collections/collection")
    suspend fun getCollection(
        @Header("Authorization") token: String,
        @Query("collection_id") id: String,
        @Query("include_links") include: Boolean
    ): Thing<Listing<Any>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/collections/subreddit_collections")
    suspend fun getCollections(
        @Header("Authorization") token: String,
        @Query("sr_fullname") name: String,
    ): Thing<Listing<SubReddit>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/by_id/{names}.json")
    suspend fun getByID(
        @Header("Authorization") token: String,
        @Path("names") names: String
    ): Thing<Listing<Thing<Link>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/hot.json")
    suspend fun getSubredditsHot(
        @Header("Authorization") token: String,
        @Query("g") language: String?,
        @Query("before") beforeID: String?,
        @Query("after") afterID: String?,
        @Query("limit")limit:Int
    ): Thing<Listing<Thing<Link>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/new.json")
    suspend fun getSubredditsNew(
        @Header("Authorization") token: String,
        @Query("g") language: String?,
        @Query("before") beforeID: String?,
        @Query("after") afterID: String?,
        @Query("limit")limit:Int
    ): Thing<Listing<Thing<Link>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/r/{subreddit}/{listing}.json")
    suspend fun getBySome(
        @Header("Authorisation") token: String,
        @Path("subreddit") subreddit: String,
        @Path("listing") srName: String?
    ): Thing<Listing<Thing<Link>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/subreddits/search")
    suspend fun findSubreddit(
        @Header("Authorisation") token: String,
        @Header("Accept") type: String,
        @Header("User-Agent") agent: String,
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("sort") sort: String
    ): Thing<Listing<Thing<SubReddit>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/v1/collections/follow_collection")
    suspend fun followCollection(
        @Header("Authorization") token: String,
        @Query("collection_id") id: String,
        @Query("follow") follow: Boolean
    ): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/v1/collections/delete_collection")
    suspend fun deleteCollection(
        @Header("Authorization") token: String,
        @Query("collection_id") id: String
    ): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/v1/collections/remove_post_in_collection")
    suspend fun deletePostInCollection(
        @Header("Authorization") token: String,
        @Query("collection_id") id: String,
        @Query("link_fullname") link: String
    ): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/collections/subreddit_collections")
    suspend fun fetchSubredditCollection(
        @Header("Authorization") token: String,
        @Query("sr_fullname") srFullname: String
    ): Response<String>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/r/{subreddit}/api/deleteflair")
    suspend fun deleteFlair(
        @Header("Authorization") token: String,
        @Path("subreddit") subreddit: String,
        @Query("api_type") type: String,
        @Query("name") name: String,
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/r/{subreddit}/api/clearflairtemplates")
    fun clearFlairTemplate(
        @Header("Authorisation") token: String,
        @Path("subreddit") path: String,
        @Query("api_type") type: String,
        @Query("flair_type") flair_type: String
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/r/{subreddit}/api/flair")
    fun getFlair(@Query("api_type") type: String): More

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/r/{subreddit}/about.json")
    suspend fun getSubredditAbout(
        @Header("Authorization") token: String,
        @Path("subreddit") subreddit: String
    ): Thing<SubredditInfo>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/r/subreddit/api/info")
    suspend fun getSubredditsAbout(
        @Header("Authorization") token: String,
        @Query("sr_name")srName:String
    ):Thing<Listing<Thing<SubredditInfo>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/follow_post")
    suspend fun follow(
        @Header("Authorization") token: String,
        @Query("follow") follow: Boolean,
        @Query("fullname") fullname: String
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/user/{username}/overview")
    suspend fun getAll(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Query("show") given: String,
        @Query("sort") sort: String,
        @Query("t") time: String,
        @Query("type") type: Type,
        @Query("after") after: String,
        @Query("before") before: String,
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/user/{username}/saved")
    suspend fun getSaved(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Query("show") given: String,
        @Query("sort") sort: String,
        @Query("t") time: String,
        @Query("type") type: Type,
        @Query("after") after: String,
        @Query("before") before: String,
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/v1/collections/follow_collection")
    suspend fun followCollection(
        @Query("collection_id") collectionId: String,
        @Query("follow") follow: Boolean
    ): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/v1/collections/remove_post_in_collection")
    suspend fun removePostFromCollection(
        @Query("collection_id") collectionId: String,
        @Query("link_fullname") link_fullname: String
    ): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/collections/subreddit_collections")
    suspend fun getSubredditCollection(
        @Query("sr_fullname") srFullName: String
    ): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/{subreddit}/emojis/all.json")
    suspend fun getEmojis(
        @Header("Authorization") token: String,
        @Path("subreddit") subreddit: String
    ): EmojisCollection

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/vote")
    suspend fun vote(
        @Header("Authorization") token: String,
        @Query("dir") dir: Int,
        @Query("id") id: String,
        @Query("rank") rank: Int?
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/site_admin")
    suspend fun createOrEditSubreddit(
        @Header("Authorization") token: String,
        @Query("accept_followers") acceptFollowers: Boolean?,
        @Query("admin_override_spam_comments") overrideSpam: Boolean?,
        @Query("admin_override_spam_links") overrideSpamLinks: Boolean?,
        @Query("admin_override_spam_selfposts") overrideSpamSelfPosts: Boolean?,
        @Query("all_original_content") allOriginalContent: Boolean?,
        @Query("allow_chat_post_creation") allowChatPostCreation: Boolean?,
        @Query("allow_discovery") allowDiscovery: Boolean?,
        @Query("allow_galleries") allowGalleries: Boolean?,
        @Query("allow_images") allowImages: Boolean?,
        @Query("allow_polls") allowPolls: Boolean?,
        @Query("allow_post_crossposts") allowPostCrossposts: Boolean?,
        @Query("allow_prediction_contributors") allowPredictionContributors: Boolean?,
        @Query("allow_predictions") allowPredictions: Boolean?,
        @Query("allow_predictions_tournament") allowPredictionsTournament: Boolean?,
        @Query("allow_talks") allowTalks: Boolean?,
        @Query("allow_top") allowTop: Boolean?,
        @Query("allow_videos") allowVideos: Boolean?,
        //json
        @Query("api_type") api_type: String?,
        //an integer between 0 and 3
        @Query("ban_evasion_threshold") banEvasionThreshold: Int?,
        @Query("collapse_deleted_comments") collapseDeletedComments: Boolean?,
        @Query("comment_contribution_settings") comment_contribution_settings: ContrSettings?,
        @Query("comment_score_hide_mins") commentScoreHideMins: Int?,
        @Query("crowd_control_chat_level") crowdControlChatLevel: Int?,
        @Query("crowd_control_filter") crowdControlFilter: Boolean?,
        //an integer between 0 and 3
        @Query("crowd_control_level") crowdControlLevel: Int?,
        @Query("crowd_control_mode") crowdControlMode: Int?,
        //an integer between 0 and 3
        @Query("crowd_control_post_level") crowdControlPostLevel: Int?,
        //raw markdown text
        @Query("description") description: String?,
        @Query("disable_contributor_requests") disableContributorRequests: Boolean?,
        @Query("exclude_banned_modqueue") excludeBannedModqueue: Boolean?,
        @Query("free_form_reports") freeFormReports: Boolean?,
        @Query("g-recaptcha-response") gRecaptchaResponse: String?,
        //an integer between 0 and 3
        @Query("hateful_content_threshold_abuse") hatefulContentThresholdAbuse: Int?,
        @Query("hateful_content_threshold_identity") hatefulContentThresholdIdentity: Int?,
        @Query("header-title") headerTitle: String,
        @Query("hide_ads") hideAds: Boolean?,
        //a 6-digit rgb hex color, e.g. #AABBCC
        @Query("key_color") keyColor: String,
        //one of (any, link, self)
        @Query("link_type") linkType: String,
        @Query("modmail_harassment_filter_enabled") modmailHarassmentFilterEnabled: Boolean?,
        //subreddit name
        @Query("name") name: String?,
        @Query("new_pinned_post_pns_enabled") new_pinned_post_pns_enabled: Boolean?,
        @Query("original_content_tag_enabled") original_content_tag_enabled: Boolean?,
        @Query("over_18") over_18: Boolean?,
        @Query("sr") sr: String?,
        @Query("restrict_commenting") restrict_commenting: Boolean?,
        @Query("restrict_posting") restrict_posting: Boolean?,
        @Query("title") title: String?,
        @Query("submit_text") submit_text: String?,
        @Query("prediction_leaderboard_entry_type") prediction_leaderboard_entry_type: Int?,
        @Query("public_description") public_description: String?,
        @Query("user_flair_pns_enabled") user_flair_pns_enabled: Boolean?,
        @Query("welcome_message_enabled") welcome_message_enabled: Boolean?,
        @Query("welcome_message_text") welcome_message_text: String?,
        @Query("should_archive_posts") should_archive_posts: Boolean?,
        @Query("show_media") show_media: Boolean?,
        @Query("show_media_preview") show_media_preview: Boolean?,
        @Query("spoilers_enabled") spoilers_enabled: Boolean?,
        //one of (low, high, all)
        @Query("spam_comments") spam_comments: String?,
        @Query("spam_links") spam_links: String?,
        @Query("spam_selfposts") spam_selfposts: String?,
        @Query("submit_link_label") submit_link_label: String?,
        @Query("submit_text_label") submit_text_label: String?,
        //one of (confidence, top, new, controversial, old, random, qa, live)
        @Query("suggested_comment_sort") suggested_comment_sort: String?,
        //an integer between 0 and 1
        @Query("toxicity_threshold_chat_level") toxicity_threshold_chat_level: Int?,
        //one of (gold_restricted, archived, restricted, private, employees_only, gold_only, public, user)
        @Query("type") type: String?,
        //an integer between 0 and 36600 (default: 0)
        @Query("wiki_edit_age") wiki_edit_age: Int?,
        //an integer between 0 and 1000000000 (default: 0)
        @Query("wiki_edit_karma") wiki_edit_karma: Int?,
        //one of (disabled, modonly, anyone)
        @Query("wikimode") wikimode: String?
    ): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/morechildren")
    suspend fun getChildren(
        @Header("Authorization") token: String,
        @Query("depth") depth: Int?,
        @Query("id") id: String?,
        @Query("limit_children") limitChildren: Boolean,
        @Query("link_id") linkId: String,
        @Query("sort") sort: String,
        @Query("children") children: String,
        @Query("api_type") type: String?
    ): FromMore

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/r/{subreddit}/about")
    suspend fun getSubredditInfo(
        @Header("Authorization") token: String,
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET(" /r/{subreddit}/comments/{id}/{article}")
    suspend fun getSubredditComments(
        @Header("Authorization") token: String,
        @Path("subreddit") subreddit: String,
        @Path("id") id: String,
        @Path("article") article: String,
        @Query("comment") comment: String?,
        @Query("context") context: Int?,
        @Query("depth") depth: Int?,
        @Query("limit") limit: Int?,
        @Query("showedits") showedits: Boolean,
        @Query("showmedia") showmedia: Boolean,
        @Query("showmore") showmore: Boolean,
        @Query("showtitle") showtitle: Boolean,
        @Query("sort") sort: String,
        @Query("threaded") threaded: Boolean
    ): Array<Thing<Listing<Thing2>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET(" {link}")
    suspend fun getSubredditComments2(
        @Header("Authorization") token: String,
        @Path("link") link: String,
        @Query("comment") comment: String?,
        @Query("context") context: Int?,
        @Query("depth") depth: Int?,
        @Query("limit") limit: Int?,
        @Query("showedits") showedits: Boolean,
        @Query("showmedia") showmedia: Boolean,
        @Query("showmore") showmore: Boolean,
        @Query("showtitle") showtitle: Boolean,
        @Query("sort") sort: String,
        @Query("threaded") threaded: Boolean,
        @Query("sr_detail") detail: Boolean
    ): Array<Thing<Listing<Thing2>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/subscribe")
    suspend fun subscribe(
        @Header("Authorization") token: String,
        @Query("action") action: SubscibeType?,
        @Query("skip_initial_defaults") skip: Boolean?,
        @Query("sr") srName: String
    ): Unit

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/comment")
    suspend fun postComment(
        @Header("Authorization") token: String,
        @Query("api_type: json") apiType: Boolean,
        @Query("thing_id") thingId: String,
        @Query("text") text: String
    ): Comm

    @GET("/api/user_data_by_account_ids")
    suspend fun getAccounts(
        @Header("Authorization") token: String,
        @Query("ids") ids: String
    ): UserHolder

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/v1/me/friends")
    suspend fun getFriends(
        @Header("Authorization") token: String,
        @Query("after") after: String?,
        @Query("before") before: String?,
        @Query("count") count: Int?,
        @Query("limit") limit: Int?,
        @Query("sr_detail")srDetail:Boolean?,
        @Query("show")show:String
    ): Thing<Listing<Account>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/subreddits/mine/moderator")
    suspend fun getSubreddits(
        @Header("Authorization") token: String,
        @Query("after") after: String?,
        @Query("before") before: String?,
        @Query("count") count: Int?,
        @Query("limit") limit: Int?,
        @Query("sr_detail")srDetail:Boolean?,
        @Query("show")show:String
    ):Thing<Listing<SubReddit>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/user/username/saved")
    suspend fun getSubredditsSaved(
        @Header("Authorization") token: String,
        @Path("username")username:String,
        @Query("after") after: String?,
        @Query("before") before: String?,
        @Query("count") count: Int?,
        @Query("limit") limit: Int?,
        @Query("sr_detail")srDetail:Boolean?,
        @Query("show")show:String,
        @Query("type")type:Type,
        @Query("t")time:Time,
        @Query("context")context:Int,
        @Query("sort")sort: Sort
    ):Thing<Listing<SubReddit>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/user/{username}/comments")
    suspend fun getCommentsMine(
        @Header("Authorization") token: String,
        @Path("username")username:String,
        @Query("after") after: String?,
        @Query("before") before: String?,
        @Query("count") count: Int?,
        @Query("limit") limit: Int?,
        @Query("sr_detail")srDetail:Boolean?,
        @Query("show")show:String,
        @Query("type")type:Type,
        @Query("t")time:Time,
        @Query("context")context:Int,
        @Query("sort")sort: Sort
    ):Thing<Listing<Thing<Comment>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/user/{username}/saved")
    suspend fun getCommentsSaved(
        @Header("Authorization") token: String,
        @Path("username")username:String,
        @Query("after") after: String?,
        @Query("before") before: String?,
        @Query("count") count: Int?,
        @Query("limit") limit: Int?,
        @Query("sr_detail")srDetail:Boolean?,
        @Query("show")show:String,
        @Query("type")type:Type,
        @Query("t")time:Time,
        @Query("context")context:Int,
        @Query("sort")sort: Sort
    ):Thing<Listing<Thing<Comment>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/user/{username}/saved")
    suspend fun getSubredditSaved(
        @Header("Authorization") token: String,
        @Path("username")username:String,
        @Query("after") after: String?,
        @Query("before") before: String?,
        @Query("count") count: Int?,
        @Query("limit") limit: Int?,
        @Query("sr_detail")srDetail:Boolean?,
        @Query("show")show:String,
        @Query("type")type:Type,
        @Query("t")time:Time,
        @Query("context")context:Int,
        @Query("sort")sort: Sort
    ):Thing<Listing<Thing<Link>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/user/{username}/submitted")
    suspend fun getSubredditUser(
        @Header("Authorization") token: String,
        @Path("username")username:String,
        @Query("after") after: String?,
        @Query("before") before: String?,
        @Query("count") count: Int?,
        @Query("limit") limit: Int?,
        @Query("sr_detail")srDetail:Boolean?,
        @Query("show")show:String,
        @Query("type")type:Type,
        @Query("t")time:Time,
        @Query("context")context:Int,
        @Query("sort")sort: Sort
    ):Thing<Listing<Thing<Link>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/save")
    suspend fun save(
        @Header("Authorization") token: String,
        @Query("category") category: String,
        @Query("id") fullname: String
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/unsave")
    suspend fun unsave(
        @Header("Authorization") token: String,
        @Query("id") fullname: String
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/saved_categories")
    suspend fun getSavedCategories(
        @Header("Authorization") token: String
    ):String

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/sendreplies")
    suspend fun sendReplies(
        @Query("id") id:String,
        @Query("state")state:Boolean
    )

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/compose")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Query("api_type")apiType:String,
        @Query("subject")subject:String,
        @Query("text")text:String,
        @Query("to")to:String
    ):String

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/v1/me/friends/{username}")
    suspend fun setAsFriend(
        @Header("Authorization") token: String,
    )

    enum class Time{
        hour, day, week, month, year, all
    }
    enum class Type {
        links, comments
    }

    enum class Sort{
        hot, new, top, controversial
    }
    enum class SubscibeType {
        sub, unsub
    }

}
