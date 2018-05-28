package com.crakac.ofutodon.methods

import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.api.entity.Notification
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.api.entity.StreamingContent
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Streaming : MastodonMethodTestBase() {

    val gson = GsonBuilder().create()

    @Test
    fun streamingPublicTimeline() {

        val lock = CountDownLatch(10)
        val request = Request.Builder()
                .url("ws:/localhost/initialize/v1/streaming/?access_token=${BuildConfig.LOCAL_TOKEN}&stream=public:local")
                .build()
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
                .addInterceptor (logger)
                .addInterceptor {
                    val org = it.request()
                    val builder = org.newBuilder()
                    builder.addHeader("Authorization", "Bearer ${BuildConfig.LOCAL_TOKEN}")
                    val newRequest = builder.build()
                    it.proceed(newRequest)
                }.build()

        client.newWebSocket(request,
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket?, response: Response?) {
                        System.out.println("socketopen")
                    }

                    override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
                        System.out.println(t?.printStackTrace())
                    }

                    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
                        webSocket?.close(code, reason)
                        System.out.println("socket closing")
                    }

                    override fun onMessage(webSocket: WebSocket?, text: String?) {
                        val message = gson.fromJson(text, StreamingContent::class.java)
                        System.out.println(message.toString())
                        System.out.println(message.event)
                        System.out.println(message.payload)
                        when(message.event){
                            "update" -> {
                                val status = gson.fromJson(message.payload, Status::class.java)
                                onNewStatus(status)

                            }
                            "notification" -> {
                                val notification = gson.fromJson(message.payload, Notification::class.java)
                                onNotification(notification)
                            }
                            "delete" -> {
                                onDelete(message.payload?.toLong())
                            }
                            else -> {
                                System.out.println("unknown event")
                            }
                        }
                        lock.countDown()
                    }

                    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
                        System.out.println("on binary message")
                    }

                    override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
                        System.out.println("socket closed")
                    }
                })
        lock.await(30, TimeUnit.SECONDS)
    }

    fun onNewStatus(status: Status){
        System.out.println(gson.toJson(status))
    }

    fun onNotification(notification: Notification){
        System.out.println(gson.toJson(notification))
    }

    fun onDelete(id: Long?){

    }
}