package com.crakac.ofutodon.api

import android.app.Notification
import com.crakac.ofutodon.api.entity.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Kosuke on 2017/04/27.
 */
interface MastodonAPI {
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

    @FormUrlEncoded
    @PATCH("/api/v1/accounts/update_credentials")
    fun updateAccountCredentials(
            @Field("display_name")
            displayName: String,
            @Field("note")
            note: String? = null,
            @Field("avatar")
            avator: String? = null,
            @Field("header")
            header: String? = null
    ): Call<Account>

    @GET("/api/v1/accounts/{id}/followers")
    fun getFollowers(
            @Path("id")
            id: Long,
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Account>>

    @GET("/api/v1/accounts/{id}/following")
    fun getFollowings(
            @Path("id")
            id: Long,
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Account>>

    @GET("/api/v1/accounts/{id}/statuses")
    fun getStatuses(
            @Path("id")
            id: Long,
            @Query("only_media")
            onlyMedia: Boolean,
            @Query("exclude_replies")
            excludeReplies: Boolean,
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Status>>

    @POST("/api/v1/accounts/{id}/follow")
    fun follow(
            @Path("id")
            id: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/unfollow")
    fun unfollow(
            @Path("id")
            id: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/block")
    fun block(
            @Path("id")
            id: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/unblock")
    fun unblock(
            @Path("id")
            id: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/mute")
    fun mute(
            @Path("id")
            id: Long
    ): Call<Relationship>

    @POST("/api/v1/accounts/{id}/unmute")
    fun unmute(
            @Path("id")
            id: Long
    ): Call<Relationship>

    @GET("/api/v1/accounts/relationships")
    fun getRelationships(
            @Query("id")
            id: Long
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
    fun getBlockAccounts(
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Account>>

    @GET("/api/v1/favourites")
    fun getFavourites(
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Status>>

    @GET("/api/v1/follow_requests")
    fun getFollowRequests(
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Account>>

    @FormUrlEncoded
    @POST("/api/v1/follow_requests/{id}/authorize")
    fun authorizeFollowRequest(
            @Path("id")
            id: Long
    )

    @FormUrlEncoded
    @POST("/api/v1/follow_requests/{id}/reject")
    fun rejectFollowRequest(
            @Path("id")
            id: Long
    )

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
    fun getMutes(
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Account>>

    @GET("/api/v1/notifications")
    fun getNotifications(
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Notification>>

    @GET("/api/v1/notifications/{id}")
    fun getNotification(
            @Path("id")
            id: Long
    ): Call<Notification>

    @POST("/api/v1/notifications/clear")
    fun crearNotification()

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
            comment: String
    ): Call<Report>

    @GET("/api/v1/search")
    fun search(
            @Query("q")
            query: String,
            @Query("resolve")
            resolveNonLocalAccount: Boolean
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
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Account>>

    @GET("/api/v1/statuses/{id}/favourited_by")
    fun getFavouritedBy(
            @Path("id")
            id: Long,
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Account>>

    @FormUrlEncoded
    @POST("/api/v1/statuses")
    fun postStatus(
            @Field("status")
            text: String,
            @Field("in_reply_to_id")
            replyTo: Long,
            @Field("media_ids")
            mediaIds: String,
            @Field("sensitive")
            isSensitive: Boolean,
            @Field("spoiler_text")
            spoilerText: String,
            @Field("visibility")
            visibility: Status.Visibility
    ): Call<Status>

    @DELETE("/api/v1/statuses/{id}")
    fun deleteStatus(
            @Path("id")
            id: Long
    )

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
            localOnly: Boolean,
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Status>>

    @GET("/api/v1/timelines/public")
    fun getPublicTimeline(
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Status>>

    @GET("/api/v1/timelines/tag/{hashtag}")
    fun getHashtagTimeline(
            @Path("hashtag")
            tag: String,
            @Query("local")
            localOnly: Boolean,
            @Query("max_id")
            maxId: Long,
            @Query("since_id")
            sinceId: Long,
            @Query("limit")
            limit: Int
    ): Call<List<Status>>
}