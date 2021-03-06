package com.crakac.ofutodon.api

import android.os.Handler
import android.util.Log
import com.crakac.ofutodon.api.entity.Notification
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.api.entity.StreamingContent
import com.crakac.ofutodon.db.User
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Created by Kosuke on 2017/05/04.
 */
class MastodonStreaming(val user: User) : WebSocketListener() {
    val TAG = "MastodonStreaming"
    val handler = Handler()
    val gson = Gson()
    var callBack: StreamingCallback? = null

    var isConnected = false
    private set

    private var ws: WebSocket? = null

    interface StreamingCallback {
        fun onStatus(status: Status?)
        fun onNotification(notification: Notification?)
        fun onDelete(id: Long?)
    }

    fun connect() {
        val token = user.token
        val request = Request.Builder()
                .url("wss://${user.domain}/initialize/v1/streaming/?stream=public:local")
                .build()

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.HEADERS

        val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor {
                    val org = it.request()
                    val builder = org.newBuilder()
                    builder.addHeader("Authorization", "Bearer $token")
                    val newRequest = builder.build()
                    it.proceed(newRequest)
                }.build()
        ws = client.newWebSocket(request, this)
    }

    fun close() {
        ws?.close(1000, "")
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        val message = gson.fromJson(text, StreamingContent::class.java)
        when (message.eventType) {
            StreamingContent.Event.Update -> {
                val status = gson.fromJson(message.payload, Status::class.java)
                handler.post {
                    callBack?.onStatus(status)
                }
            }
            StreamingContent.Event.Notification -> {
                val notification = gson.fromJson(message.payload, Notification::class.java)
                handler.post {
                    callBack?.onNotification(notification)
                }
            }
            StreamingContent.Event.Delete -> {
                handler.post {
                    callBack?.onDelete(message.payload?.toLong())
                }
            }
            else -> {
                Log.d(TAG, "unknown event")
            }
        }
    }

    override fun onOpen(webSocket: WebSocket?, response: Response?) {
        isConnected = true
        Log.d(TAG, "open connection")
    }

    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
        isConnected = false
        Log.d(TAG, "closing")
    }
}
