package com.crakac.ofutodon.model.api

import android.os.Handler
import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.model.api.entity.Notification
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.model.api.entity.StreamingContent
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Created by Kosuke on 2017/05/04.
 */
class MastodonStreaming : WebSocketListener(){
    val TAG = "MastodonStreaming"
    val handler = Handler()
    val gson = Gson()
    var callBack: StreamingCallback? = null

    private var ws: WebSocket? = null

    interface StreamingCallback {
        fun onStatus(status: Status?)
        fun onNotification(notification: Notification?)
        fun onDelete(id: Long?)
    }

    fun connect() {
        val request = Request.Builder()
                .url("wss://mstdn.jp/api/v1/streaming/?access_token=${BuildConfig.DEBUG_TOKEN}&stream=user")
                .build()

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.HEADERS

        val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor {
                    val org = it.request()
                    val builder = org.newBuilder()
                    builder.addHeader("Authorization", "Bearer ${BuildConfig.DEBUG_TOKEN}")
                    val newRequest = builder.build()
                    it.proceed(newRequest)
                }.build()
        ws = client.newWebSocket(request, this)
    }

    fun close()
    {
        ws?.close(1000, "")
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        val message = gson.fromJson(text, StreamingContent::class.java)
        if (callBack == null) {
            System.out.println("callback is null!")
            return
        }
        when (message.eventType) {
            StreamingContent.Event.Update -> {
                val status = gson.fromJson(message.payload, Status::class.java)
                System.out.println(gson.toJson(status))
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
                System.out.println("unknown event")
            }
        }
    }
}
