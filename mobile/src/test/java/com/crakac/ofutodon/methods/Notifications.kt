package com.crakac.ofutodon.methods

import com.crakac.ofutodon.api.Range
import com.crakac.ofutodon.api.entity.Notification
import org.junit.Assert
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Notifications : MastodonMethodTestBase() {
    @Test
    fun getNotifications() {
        val lock = CountDownLatch(1)
        api.getNotifications(Range().q).enqueue(
                object : Callback<List<Notification>> {
                    override fun onResponse(call: Call<List<Notification>>?, response: Response<List<Notification>>?) {
                        lock.countDown()
                    }

                    override fun onFailure(call: Call<List<Notification>>?, t: Throwable?) {
                        lock.countDown()
                    }
                }
        )
        lock.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun getNotification() {
        val lock = CountDownLatch(1)
        api.getNotification(5762361).enqueue(
                object : Callback<Notification> {
                    override fun onResponse(call: Call<Notification>?, response: Response<Notification>?) {
                        val notification = response?.body()
                        Assert.assertNotNull(notification)

                        lock.countDown()
                    }

                    override fun onFailure(call: Call<Notification>?, t: Throwable?) {
                        lock.countDown()
                    }
                }
        )
        lock.await(5, TimeUnit.SECONDS)
    }

//    @Test
//    fun clear(){
//        val r = api.clearNotification().execute()
//        Assert.assertTrue(r.isSuccessful)
//    }
}