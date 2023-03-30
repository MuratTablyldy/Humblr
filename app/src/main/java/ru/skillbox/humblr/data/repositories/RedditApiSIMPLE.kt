package ru.skillbox.humblr.data.repositories

import retrofit2.Response
import retrofit2.http.*
import ru.skillbox.humblr.data.entities.*

interface RedditApiSIMPLE {
    @Headers("Accept : application/json","User-Agent : android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/subreddits/search.json")
    suspend fun findSubreddit(
        @Header("Authorisation") token: String,
        @Query("q") query:String,
        @Query("limit") limit:Int,
        @Query("sort") sort:String
    ): Thing<Listing<Thing<SubReddit>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @PATCH("/api/v1/me/prefs.json")
    suspend fun savePrefs(@Header("Authorization") token: String, @Header("User-Agent")agent:String, @Body prefs: Prefs): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/user/{username}/about.json")
    suspend fun getInfoAboutUser(
        @Header("Authorisation") token: String,
        @Path("username") username: String
    ):Thing<Account>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @PATCH("/api/v1/collections/collection.json")
    suspend fun getCollection(@Header("Authorization") token: String,
                              @Query("collection_id") collectionId:String,
                              @Query("include_links") includeLinks:Boolean,
    ):Thing<Listing<Link>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/r/{subreddit}/about.json")
    suspend fun getSubredditAbout(
        @Header("Authorization") token: String,
        @Path("subreddit") subreddit: String
    ): Thing<SubredditInfo>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @POST("/api/vote")
    suspend fun vote(
        @Header("Authorization") token: String,
        @Query("dir") dir: Int,
        @Query("id") id: String,
        @Query("rank") rank: Int?
    ): Response<Unit>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/r/{subreddit}.json")
    suspend fun getSubredditInfo(@Header("Authorization") token: String,
                                 @Path("subreddit")subreddit: String?,
    ):Thing<Listing<Thing<SubReddit>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET(" /r/{subreddit}/comments/{id}/{article}.json")
    suspend fun getSubredditComments(@Header("Authorization") token: String,
                                     @Path("subreddit") subreddit:String,
                                     @Path("id") id:String,
                                     @Path("article") article:String,
                                     @Query("comment") comment:String?,
                                     @Query("context") context:Int?,
                                     @Query("depth") depth:Int?,
                                     @Query("limit") limit:Int?,
                                     @Query("showedits") showedits:Boolean,
                                     @Query("showmedia") showmedia:Boolean,
                                     @Query("showmore") showmore:Boolean,
                                     @Query("showtitle") showtitle:Boolean,
                                     @Query("sort") sort:String,
                                     @Query("threaded") threaded:Boolean
    ): Array<Thing<Listing<Thing2>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("{link}.json")
    suspend fun getSubredditComments2(@Header("Authorization") token: String,
                                     @Path("link") link:String,
                                     @Query("comment") comment:String?,
                                     @Query("context") context:Int?,
                                     @Query("depth") depth:Int?,
                                     @Query("limit") limit:Int?,
                                     @Query("showedits") showedits:Boolean,
                                     @Query("showmedia") showmedia:Boolean,
                                     @Query("showmore") showmore:Boolean,
                                     @Query("showtitle") showtitle:Boolean,
                                     @Query("sort") sort:String,
                                     @Query("threaded") threaded:Boolean
    ): Array<Thing<Listing<Thing2>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/search.json")
    suspend fun search(
        @Header("Authorization") token: String,
        @Query("q") query:String,
        @Query("sort")sort:String,
        @Query("limit") limit:Int
    ):Thing<Listing<Thing<Link>>>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/r/{subreddit}/about/rules.json")
    suspend fun getRules(
        @Header("Authorization") token: String,
        @Path("subreddit") subreddit:String
    ):Rules

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/api/needs_captcha.json")
    suspend fun needCaptcha(@Header("Authorization") token: String,):Boolean

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)","api_type: json")

    @GET("/api/new_captcha.json")
    suspend fun getCaptcha(@Header("Authorization") token: String,):String

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)")
    @GET("/r/{subreddit}/about.json")
    suspend fun getSubredditAboutS(@Header("Authorization") token: String, @Path("subreddit") subreddit: String): Thing<SubredditInfo>

    @Headers("User-Agent: android:hampApp:v1.0.0 (by /u/MuratTabyldy)","api_type: json")
    @GET("/user/{userName}/about.json")
    suspend fun getAccountInfo(@Header("Authorization") token: String,@Path("userName")userName:String):Thing<Account>

}