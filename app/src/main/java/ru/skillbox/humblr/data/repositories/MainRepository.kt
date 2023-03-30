package ru.skillbox.humblr.data.repositories

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.*
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import retrofit2.Response
import ru.skillbox.humblr.data.Result
import ru.skillbox.humblr.data.entities.*
import ru.skillbox.humblr.data.repositories.modules.TokenHolder
import ru.skillbox.humblr.utils.State
import javax.inject.Inject
import kotlin.coroutines.resume

class MainRepository @Inject constructor(
    private val sessionManager: FetchSessionManager,
    networking: Networking
) {
    private val authSessionService = AuthSessionService()

    private val repository = networking.repository
    private val searchRepo = networking.repositorySearch
    private var tokenHolder: TokenHolder? = null
    private val handler =
        CoroutineExceptionHandler { _, exception -> Log.e("exception", "$exception") }

    fun revokeToken() {
        val token = tokenHolder?.access_token
        val type = tokenHolder?.token_type
        if (token != null && type != null) {
            authSessionService.revokeToken(
                "https://www.reddit.com/api/v1/revoke_token",
                token,
                type
            )
            sessionManager.removeToken()
        }
    }

    private fun getDurationBeforeExpires(): Duration? {
        val expires = tokenHolder?.expires_when?.toLong()
        return if (expires != null) {
            val now = Instant.now()
            val end = Instant.ofEpochMilli(expires)
            Duration.between(now, end)
        } else null
    }

    fun getDurationBeforeExpires(tokenHolder: TokenHolder): Duration? {
        val expires = tokenHolder.expires_when?.toLong()
        return if (expires != null) {
            val now = Instant.now()
            val end = Instant.ofEpochMilli(expires)
            Duration.between(now, end)
        } else null
    }

    suspend fun getSubredditsAbout(ids: String): Result<Thing<Listing<Thing<SubredditInfo>>>> {
        return invoke(Dispatchers.IO) {
            repository.getSubredditsAbout(
                "bearer " + tokenHolder!!.access_token!!,
                ids
            )
        }
    }


    fun fetchAuthToken(): TokenHolder {
        tokenHolder = sessionManager.fetchAuthToken()
        return tokenHolder!!
    }

    @InternalCoroutinesApi
    fun subscribe(lifecycleOwner: LifecycleOwner, doWork: (Boolean) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                State.getInstance().expired.collect { value -> doWork(value) }
            }
        }
    }

    suspend fun getMe(): Result<Thing<Account>> {
        return invoke(Dispatchers.IO) {
            repository.getMe(
                "bearer " + tokenHolder!!.access_token!!,
                "android:MWkPRiRX-p04R7LbMll9uw:v1.0.0 (by /u/MuratTabyldy)"
            )
        }
    }

    suspend fun getPrefs(scope: CoroutineScope) =
        suspendCancellableCoroutine { continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            repository.getPrefs("bearer " + tokenHolder!!.access_token!!)
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun savePrefs(scope: CoroutineScope, prefs: PrefsSave) =
        suspendCancellableCoroutine<Response<Unit>> { continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            repository.savePrefs(
                                "bearer " + tokenHolder!!.access_token!!,
                                "android:hampApp:v1.0.0 (by /u/MuratTabyldy)",
                                prefs
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun getTrophies(scope: CoroutineScope) =
        suspendCancellableCoroutine<Thing<Listing2<Thing<Trophie>>>> { continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            repository.getTrophies("bearer " + tokenHolder!!.access_token!!)
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }


    fun isTokenValid(): Boolean {
        val duration = getDurationBeforeExpires()
        return duration != null && !duration.isNegative
    }

    fun isTokenValid(token: TokenHolder): Boolean {
        val duration = getDurationBeforeExpires(token)
        return duration != null && !duration.isNegative
    }

    suspend fun getSubredditsHot(
        language: String?,
        before: String?,
        after: String?,
        limit: Int
    ): Result<Thing<Listing<Thing<Link>>>> {
        return invoke(Dispatchers.IO) {
            repository.getSubredditsHot(
                token = "bearer " + tokenHolder!!.access_token!!,
                language = language,
                beforeID = before,
                afterID = after,
                limit = limit
            )
        }
    }

    suspend fun getSubredditsNew(
        language: String?,
        before: String?,
        after: String?,
        limit: Int
    ): Result<Thing<Listing<Thing<Link>>>> {
        return invoke(Dispatchers.IO) {
            repository.getSubredditsNew(
                token = "bearer " + tokenHolder!!.access_token!!,
                language = language,
                beforeID = before,
                afterID = after,
                limit = limit
            )
        }
    }

    suspend fun getByID(scope: CoroutineScope, id: String) =
        suspendCancellableCoroutine{ continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            repository.getByID(
                                "bearer " + tokenHolder!!.access_token!!,
                                id
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun getByParameter(scope: CoroutineScope, subreddit: String, parameter: String) =
        suspendCancellableCoroutine { continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            repository.getBySome(
                                "bearer " + tokenHolder!!.access_token!!,
                                subreddit,
                                parameter
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun searchSubreddit(
        scope: CoroutineScope,
        query: String,
        limit: Int,
        sort: String
    ) =
        suspendCancellableCoroutine { continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            searchRepo.findSubreddit(
                                "bearer " + tokenHolder!!.access_token!!,
                                query, limit, sort
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun searchLink(query: String): Result<Thing<Listing<Thing<Link>>>> {
        return invoke(Dispatchers.IO) {
            searchRepo.search(
                "bearer " + tokenHolder!!.access_token!!, query, "relevance", 10
            )
        }
    }

    suspend fun getCollection(scope: CoroutineScope, id: String, include: Boolean) =
        suspendCancellableCoroutine{ continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            repository.getCollection(
                                "bearer " + tokenHolder!!.access_token!!,
                                id, include
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun getCollections(scope: CoroutineScope, name: String) =
        suspendCancellableCoroutine{ continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            repository.getCollections(
                                "bearer " + tokenHolder!!.access_token!!,
                                name
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun getUserInfo(scope: CoroutineScope, userName: String) =
        suspendCancellableCoroutine { continuation ->
            if (isTokenValid()) {
                scope.launch(handler) {
                    try {
                        val result = scope.async {
                            searchRepo.getInfoAboutUser(
                                "bearer " + tokenHolder!!.access_token!!,
                                userName
                            )
                        }
                        continuation.resume(result.await())

                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun getCollection2(
        collectionId: String,
        includeLinks: Boolean
    ): Result<Thing<Listing<Link>>> {
        return invoke(Dispatchers.IO) {
            searchRepo.getCollection(
                "bearer " + tokenHolder!!.access_token!!,
                collectionId, includeLinks
            )
        }
    }

    suspend fun getSubredditAbout(
        subreddit: String
    ): Result<Thing<SubredditInfo>> {
        return invoke(Dispatchers.IO) {
            repository.getSubredditAbout(
                "bearer " + tokenHolder!!.access_token!!,
                subreddit
            )
        }
    }

    suspend fun getSubredditList(
        scope: CoroutineScope,
        id: Array<String>?,
        names: Array<String>?,
        subreddit: String,
        url: String?
    ) =
        suspendCancellableCoroutine { continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            searchRepo.getSubredditInfo(
                                "bearer " + tokenHolder!!.access_token!!,
                                subreddit
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun getSubredditComments(
        scope: CoroutineScope,
        subredditName: String,
        id: String,
        article: String,
        comment: String?,
        context: Int?,
        depth: Int?,
        limit: Int?,
        showedits: Boolean,
        showmedia: Boolean,
        showmore: Boolean,
        showtitle: Boolean,
        sort: String,
        threaded: Boolean
    ) =
        suspendCancellableCoroutine { continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            searchRepo.getSubredditComments(
                                "bearer " + tokenHolder!!.access_token!!,
                                subredditName,
                                id,
                                article,
                                comment,
                                context,
                                depth,
                                limit,
                                showedits,
                                showmedia,
                                showmore,
                                showtitle,
                                sort,
                                threaded
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun getSubredditRules(
        scope: CoroutineScope,
        subreddit: String
    ) =
        suspendCancellableCoroutine{ continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            searchRepo.getRules(
                                "bearer " + tokenHolder!!.access_token!!,
                                subreddit
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun getSubredditEmojis(
        scope: CoroutineScope,
        subreddit: String
    ) =
        suspendCancellableCoroutine { continuation ->
            if (isTokenValid()) {
                scope.launch {
                    try {
                        val result = scope.async(handler) {
                            repository.getEmojis(
                                "bearer " + tokenHolder!!.access_token!!,
                                subreddit
                            )
                        }
                        continuation.resume(result.await())
                    } catch (t: Throwable) {
                        Log.d("error", "$t")
                    }
                }
            }
        }

    suspend fun vote(
        dir: Int,
        id: String,
        rank: Int?
    ): Result<Unit> {
        return invoke(Dispatchers.IO) {
            repository.vote(
                "bearer " + tokenHolder!!.access_token!!, dir, id, rank
            )
        }
    }

    suspend fun createOrEditSubreddit(
        scope: CoroutineScope,
        subredditCreator: SubredditCreator
    ) =
        suspendCancellableCoroutine { continuation ->
            if (isTokenValid()) {
                scope.launch(handler) {
                    val result = scope.async {
                        repository.createOrEditSubreddit(
                            "bearer " + tokenHolder!!.access_token!!,
                            subredditCreator.acceptFollowers,
                            subredditCreator.overrideSpam,
                            subredditCreator.overrideSpamLinks,
                            subredditCreator.overrideSpamSelfPosts,
                            subredditCreator.allOriginalContent,
                            subredditCreator.allowChatPostCreation,
                            subredditCreator.allowDiscovery,
                            subredditCreator.allowGalleries,
                            subredditCreator.allowImages,
                            subredditCreator.allowPolls,
                            subredditCreator.allowPostCrossposts,
                            subredditCreator.allowPredictionContributors,
                            subredditCreator.allowPredictions,
                            subredditCreator.allowPredictionsTournament,
                            subredditCreator.allowTalks,
                            subredditCreator.allowTop,
                            subredditCreator.allowVideos,
                            //json
                            subredditCreator.api_type,
                            //an integer between 0 and 3
                            subredditCreator.banEvasionThreshold,
                            subredditCreator.collapseDeletedComments,
                            subredditCreator.comment_contribution_settings,
                            subredditCreator.commentScoreHideMins,
                            subredditCreator.crowdControlChatLevel,
                            subredditCreator.crowdControlFilter,
                            //an integer between 0 and 3
                            subredditCreator.crowdControlLevel,
                            subredditCreator.crowdControlMode,
                            //an integer between 0 and 3
                            subredditCreator.crowdControlPostLevel,
                            //raw markdown text
                            subredditCreator.description,
                            subredditCreator.disableContributorRequests,
                            subredditCreator.excludeBannedModqueue,
                            subredditCreator.freeFormReports,
                            subredditCreator.gRecaptchaResponse,
                            //an integer between 0 and 3
                            subredditCreator.hatefulContentThresholdAbuse,
                            subredditCreator.hatefulContentThresholdIdentity,
                            subredditCreator.headerTitle,
                            subredditCreator.hideAds,
                            //a 6-digit rgb hex color, e.g. #AABBCC
                            subredditCreator.keyColor,
                            //one of (any, link, self)
                            subredditCreator.linkType,
                            subredditCreator.modmailHarassmentFilterEnabled,
                            //subreddit name
                            subredditCreator.name,
                            subredditCreator.new_pinned_post_pns_enabled,
                            subredditCreator.original_content_tag_enabled,
                            subredditCreator.over_18,
                            subredditCreator.sr,
                            subredditCreator.restrict_commenting,
                            subredditCreator.restrict_posting,
                            subredditCreator.title,
                            subredditCreator.submit_text,
                            subredditCreator.prediction_leaderboard_entry_type,
                            subredditCreator.public_description,
                            subredditCreator.user_flair_pns_enabled,
                            subredditCreator.welcome_message_enabled,
                            subredditCreator.welcome_message_text,
                            subredditCreator.should_archive_posts,
                            subredditCreator.show_media,
                            subredditCreator.show_media_preview,
                            subredditCreator.spoilers_enabled,
                            //one of (low, high, all)
                            subredditCreator.spam_comments,
                            subredditCreator.spam_links,
                            subredditCreator.spam_selfposts,
                            subredditCreator.submit_link_label,
                            subredditCreator.submit_text_label,
                            //one of (confidence, top, new, controversial, old, random, qa, live)
                            subredditCreator.suggested_comment_sort,
                            //an integer between 0 and 1
                            subredditCreator.toxicity_threshold_chat_level,
                            //one of (gold_restricted, archived, restricted, private, employees_only, gold_only, public, user)
                            subredditCreator.type,
                            //an integer between 0 and 36600 (default: 0)
                            subredditCreator.wiki_edit_age,
                            //an integer between 0 and 1000000000 (default: 0)
                            subredditCreator.wiki_edit_karma,
                            //one of (disabled, modonly, anyone)
                            subredditCreator.wikimode
                        )
                    }
                    continuation.resume(result.await())
                }
            }
        }

    suspend fun getAccountInfo(
        userName: String,
    ): Result<Thing<Account>> {
        return invoke(Dispatchers.IO) {
            searchRepo.getAccountInfo("bearer " + tokenHolder!!.access_token!!, userName)
        }
    }

    suspend fun getChildren(
        depth: Int?,
        id: String?,
        children: String,
        limitChildren: Boolean,
        linkId: String,
        sort: String,
        type: String?
    ): Result<FromMore> {
        return invoke(Dispatchers.IO) {
            repository.getChildren(
                "bearer " + tokenHolder!!.access_token!!,
                depth = depth,
                id = id,
                children = children,
                limitChildren = limitChildren,
                linkId = linkId,
                sort = sort,
                type = type
            )
        }
    }

    private suspend fun <T : Any> invoke(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): Result<T> {
        return withContext(dispatcher) {
            try {
                if (!isTokenValid()) {
                    throw TokenISInvalidException()
                }
                Result.Success(apiCall.invoke())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun getComments(
        link: String,
        comment: String?,
        context: Int?,
        depth: Int?,
        limit: Int?,
        showedits: Boolean,
        showmedia: Boolean,
        showmore: Boolean,
        showtitle: Boolean,
        sort: String,
        threaded: Boolean,
    ): Result<Array<Thing<Listing<Thing2>>>> {
        return invoke(Dispatchers.IO) {
            repository.getSubredditComments2(
                token = "bearer " + tokenHolder!!.access_token!!,
                link = link,
                comment = comment,
                context = context,
                depth = depth,
                limit = limit,
                showedits = showedits,
                showmedia = showmedia,
                showmore = showmore,
                showtitle = showtitle,
                sort = sort,
                threaded = threaded,
                detail = threaded
            )
        }
    }

    suspend fun subscribe(
        action: RedditApi.SubscibeType,
        skip: Boolean?,
        srName: String
    ): Result<Unit> {
        return invoke(Dispatchers.IO) {
            repository.subscribe(
                token = "bearer " + tokenHolder!!.access_token!!,
                action = action,
                skip = skip,
                srName
            )
        }
    }

    suspend fun postComment(
        thingId: String,
        text: String
    ): Result<Comm> {
        return invoke(Dispatchers.IO) {
            repository.postComment(
                token = "bearer " + tokenHolder!!.access_token!!, true, thingId, text
            )
        }
    }

    suspend fun getAccounts(ids: String): Result<UserHolder> {
        return invoke(Dispatchers.IO) {
            repository.getAccounts(
                token = "bearer " + tokenHolder!!.access_token!!,
                ids
            )
        }
    }

    suspend fun getFriends(
        before: String?,
        after: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean
    ): Result<Thing<Listing<Account>>> {
        return invoke(Dispatchers.IO) {
            repository.getFriends(
                token = "bearer " + tokenHolder!!.access_token!!,
                before = before,
                after = after,
                count = count,
                limit = limit,
                srDetail = srDetail,
                show = "all"
            )
        }
    }

    suspend fun getMineSubreddits(
        before: String?,
        after: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean
    ): Result<Thing<Listing<SubReddit>>> {
        return invoke(Dispatchers.IO) {
            repository.getSubreddits(
                token = "bearer " + tokenHolder!!.access_token!!,
                before = before,
                after = after,
                count = count,
                limit = limit,
                srDetail = srDetail,
                show = "all"
            )
        }
    }

    suspend fun getUserSubreddits(
        userName: String,
        before: String?,
        after: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean
    ): Result<Thing<Listing<Thing<Link>>>> {
        return invoke(Dispatchers.IO) {
            repository.getSubredditUser(
                token = "bearer " + tokenHolder!!.access_token!!,
                before = before,
                after = after,
                count = count,
                limit = limit,
                srDetail = srDetail,
                show = "all",
                username = userName,
                context = 2,
                type = RedditApi.Type.links,
                time = RedditApi.Time.all,
                sort = RedditApi.Sort.top
            )
        }
    }

    suspend fun getSavedSubreddit(
        username: String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        show: String,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort
    ): Result<Thing<Listing<Thing<Link>>>> {
        return invoke(Dispatchers.IO) {
            repository.getSubredditSaved(
                token = "bearer " + tokenHolder!!.access_token!!,
                username,
                after,
                before,
                count,
                limit,
                srDetail,
                show,
                RedditApi.Type.links,
                time,
                context,
                sort
            )
        }
    }

    suspend fun getCommentsMine(
        username: String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort
    ): Result<Thing<Listing<Thing<Comment>>>> {
        return invoke(Dispatchers.IO) {
            repository.getCommentsMine(
                token = "bearer " + tokenHolder!!.access_token!!,
                username = username,
                after = after,
                before = before,
                count = count,
                limit = limit,
                srDetail = srDetail,
                show = "given",
                type = RedditApi.Type.comments,
                time = time,
                context = context,
                sort = sort
            )
        }
    }

    suspend fun getCommentsSaved(
        username: String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort
    ): Result<Thing<Listing<Thing<Comment>>>> {
        return invoke(Dispatchers.IO) {
            repository.getCommentsSaved(
                token = "bearer " + tokenHolder!!.access_token!!,
                username = username,
                after = after,
                before = before,
                count = count,
                limit = limit,
                srDetail = srDetail,
                show = "given",
                type = RedditApi.Type.comments,
                time = time,
                context = context,
                sort = sort
            )
        }
    }

    suspend fun getSubredditSaved(
        username: String,
        after: String?,
        before: String?,
        count: Int?,
        limit: Int?,
        srDetail: Boolean?,
        time: RedditApi.Time,
        context: Int,
        sort: RedditApi.Sort
    ): Result<Thing<Listing<Thing<Link>>>> {
        return invoke(Dispatchers.IO) {
            repository.getSubredditSaved(
                token = "bearer " + tokenHolder!!.access_token!!,
                username = username,
                after = after,
                before = before,
                count = count,
                limit = limit,
                srDetail = srDetail,
                show = "given",
                type = RedditApi.Type.links,
                time = time,
                context = context,
                sort = sort
            )
        }
    }

    suspend fun save(
        category: String,
        fullname: String
    ): Result<Unit> {
        return invoke(Dispatchers.IO) {
            repository.save(token = "bearer " + tokenHolder!!.access_token!!, category, fullname)
        }
    }

    suspend fun unsave(
        fullname: String
    ): Result<Unit> {
        return invoke(Dispatchers.IO) {
            repository.unsave(token = "bearer " + tokenHolder!!.access_token!!, fullname)
        }
    }

    /*suspend fun getCatigories(
        fullname: String
    ): Result<String> {
        return invoke(Dispatchers.IO) {
            repository.getSavedCategories(token = "bearer " + tokenHolder!!.access_token!!)
        }
    }*/

    suspend fun sendMessage(subject: String, text: String, to: String): Result<String> {
        return invoke(Dispatchers.IO) {
            repository.sendMessage(
                token = "bearer " + tokenHolder!!.access_token!!,
                "json",
                subject, text, to
            )
        }
    }

    class TokenISInvalidException : Exception("Token has been expired or revoked")
}
