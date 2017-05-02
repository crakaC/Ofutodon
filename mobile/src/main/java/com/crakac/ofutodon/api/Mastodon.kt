package com.crakac.ofutodon.api

import com.crakac.ofutodon.api.entity.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Kosuke on 2017/04/27.
 */
interface Mastodon {
    companion object {
        val ENDPOINT_AUTHORIZE: String = "/oauth/authorize"
        val OAUTH_SCOPES: String = "read write follow"
    }

    @GET("/api/v1/accounts/{id}")
    fun getAccount(
            @Path("id")
            id: Long
    ): Call<Account>

    @FormUrlEncoded
    @POST("oauth/token")
    fun fetchAccessToken(
            @Field("client_id")
            clientId: String,
            @Field("client_secret")
            clientSecret: String,
            @Field("redirect_uri")
            redirectUri: String,
            @Field("code")
            code: String,
            @Field("grant_type")
            grantType: String
    ): Call<AccessToken>

    @FormUrlEncoded
    @POST("oauth/token")
    fun fetchAccessTokenByPassword(
            @Field("client_id")
            clientId: String,
            @Field("client_secret")
            clientSecret: String,
            @Field("grant_type")
            grantType: String,
            @Field("username")
            userName: String,
            @Field("password")
            password: String,
            @Field("scope")
            scope: String
    ): Call<AccessToken>
    @GET("/api/v1/accounts/verify_credentials")
    fun getCurrentAccount(): Call<Account>

    @PATCH("/api/v1/accounts/update_credentials")
    fun updateAccountCredentials(
            @Body
            credentials: AccountCredentials?
    ): Call<Account>

    @GET("/api/v1/accounts/{id}/followers")
    fun getFollowers(
            @Path("id")
            accountId: Long,
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Account>>

    @GET("/api/v1/accounts/{id}/following")
    fun getFollowings(
            @Path("id")
            accountId: Long,
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Account>>

    @GET("/api/v1/accounts/{id}/statuses")
    fun getStatuses(
            @Path("id")
            accountId: Long,
            @Query("only_media")
            onlyMedia: Boolean? = null,
            @Query("exclude_replies")
            excludeReplies: Boolean? = null,
            @QueryMap
            range: Map<String, String> = emptyMap()
    ): Call<List<Status>>

    @POST("/api/v1/accounts/{id}/follow")
    fun follow(
            @Path("id")
            accountId: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/unfollow")
    fun unfollow(
            @Path("id")
            accountId: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/block")
    fun block(
            @Path("id")
            accountId: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/unblock")
    fun unblock(
            @Path("id")
            accountId: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/mute")
    fun mute(
            @Path("id")
            accountId: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/unmute")
    fun unmute(
            @Path("id")
            accountId: Long
    ): Call<Relationship>

    @GET("/api/v1/accounts/relationships")
    fun getRelationships(
            @Query("id")
            vararg accountIds: Long
    ): Call<List<Relationship>>

    @GET("/api/v1/accounts/search")
    fun searchAccounts(
            @Query("q")
            query: String,
            @Query("limit")
            limit: Int
    ): Call<List<Account>>

    @FormUrlEncoded
    @POST("/api/v1/apps")
    fun registerApplication(
            @Field("client_name")
            clientName: String,
            @Field("redirect_uris")
            redirectUris: String,
            @Field("scopes")
            scopes: String,
            @Field("website")
            website: String
    ): Call<AppCredentials>

    @GET("/api/v1/blocks")
    fun getBlockingAccounts(
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Account>>

    @GET("/api/v1/favourites")
    fun getFavourites(
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Status>>

    @GET("/api/v1/follow_requests")
    fun getFollowRequests(
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Account>>

    @POST("/api/v1/follow_requests/{id}/authorize")
    fun authorizeFollowRequest(
            @Path("id")
            id: Long
    ): Call<Unit>

    @POST("/api/v1/follow_requests/{id}/reject")
    fun rejectFollowRequest(
            @Path("id")
            id: Long
    ): Call<Unit>

    @FormUrlEncoded
    @POST("/api/v1/follows")
    fun followRemoteUser(
            @Field("uri")
            uri: String
    ): Call<Account>

    @GET("/api/v1/instance")
    fun getInstanceInformation(): Call<Instance>

    @Multipart
    @POST("/api/v1/media")
    fun uploadMediaAttachment(
            @Part
            file: MultipartBody.Part
    ): Call<Attachment>

    @GET("/api/v1/mutes")
    fun getMutingAccounts(
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Account>>

    @GET("/api/v1/notifications")
    fun getNotifications(
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Notification>>

    @GET("/api/v1/notifications/{id}")
    fun getNotification(
            @Path("id")
            id: Long
    ): Call<Notification>

    @POST("/api/v1/notifications/clear")
    fun clearNotification(): Call<Unit>

    @GET("/api/v1/reports")
    fun getReports(): Call<List<Report>>

    @FormUrlEncoded
    @POST("/api/v1/reports")
    fun report(
            @Field("account_id")
            id: Long,
            @Field("status_ids")
            statusIds: List<Long>,
            @Field("comment")
            comment: String?
    ): Call<Report>

    @GET("/api/v1/search")
    fun search(
            @Query("q")
            query: String,
            @Query("resolve")
            resolveNonLocalAccount: Boolean? = null
    ): Call<Results>

    @GET("/api/v1/statuses/{id}")
    fun getStatus(
            @Path("id")
            id: Long
    ): Call<Status>

    @GET("/api/v1/statuses/{id}/context")
    fun getStatusContext(
            @Path("id")
            id: Long
    ): Call<Context>

    @GET("/api/v1/statuses/{id}/card")
    fun getCard(
            @Path("id")
            id: Long
    ): Call<Card>

    @GET("/api/v1/statuses/{id}/reblogged_by")
    fun getRebloggedBy(
            @Path("id")
            id: Long,
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Account>>

    @GET("/api/v1/statuses/{id}/favourited_by")
    fun getFavouritedBy(
            @Path("id")
            id: Long,
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Account>>

    @POST("/api/v1/statuses")
    fun postStatus(
            @Body
            param: StatusBuilder
    ): Call<Status>

    @DELETE("/api/v1/statuses/{id}")
    fun deleteStatus(
            @Path("id")
            id: Long
    ): Call<Unit>

    @POST("/api/v1/statuses/{id}/reblog")
    fun reblogStatus(
            @Path("id")
            id: Long
    ): Call<Status>

    @POST("/api/v1/statuses/{id}/unreblog")
    fun unreblogStatus(
            @Path("id")
            id: Long
    ): Call<Status>

    @POST("/api/v1/statuses/{id}/favourite")
    fun favouritStatus(
            @Path("id")
            id: Long
    ): Call<Status>

    @POST("/api/v1/statuses/{id}/unfavourite")
    fun unfavouritStatus(
            @Path("id")
            id: Long
    ): Call<Status>

    @GET("/api/v1/timelines/home")
    fun getHomeTileline(
            @Query("local")
            localOnly: Boolean? = null,
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Status>>

    @GET("/api/v1/timelines/public")
    fun getPublicTimeline(
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Status>>

    @GET("/api/v1/timelines/tag/{hashtag}")
    fun getHashtagTimeline(
            @Path("hashtag")
            tag: String,
            @Query("local")
            localOnly: Boolean?,
            @QueryMap
            pager: Map<String, String> = emptyMap()
    ): Call<List<Status>>
}