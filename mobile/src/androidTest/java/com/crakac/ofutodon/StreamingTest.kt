package com.crakac.ofutodon

import android.support.test.runner.AndroidJUnit4
import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class StreamingTest {
    val TAG: String = "StreamingTest"

    @Test
    @Throws(Exception::class)
    fun streamingPublicTimeline() {
        val lock = CountDownLatch(10)
        val request = Request.Builder()
                .url("ws://localhost/initialize/v1/streaming/?access_token=${BuildConfig.LOCAL_TOKEN}&stream=public:local")
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

        val ws = client.newWebSocket(request,
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket?, response: Response?) {
                        System.out.println("socketopen")
                        Log.d(TAG, "socketopen")
                    }

                    override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
                        System.out.println("failed")
                        Log.d(TAG, "failed")
                    }

                    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
                        webSocket?.close(code, reason)
                        System.out.println("socket closing")
                        Log.d(TAG, "closing")
                    }

                    override fun onMessage(webSocket: WebSocket?, text: String?) {
                        System.out.println(text)
                        Log.d(TAG, text)
                        lock.countDown()
                    }

                    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
                        System.out.println("on binary message")
                        Log.d(TAG, "binary message")
                    }

                    override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
                        System.out.println("socket closed")
                        Log.d(TAG, "closed")
                    }
                })
        lock.await(10, TimeUnit.SECONDS)
        client.dispatcher().executorService().shutdown()

    }
}