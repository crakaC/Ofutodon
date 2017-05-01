package com.crakac.ofutodon.methods

import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.api.entity.StatusBuilder
import org.junit.Assert
import org.junit.Test

class Status : MastodonMethodTestBase(){

    val statusId = BuildConfig.DEBUG_STATUS_ID
    @Test
    fun getStatus(){
        val r = api.getStatus(statusId).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun getContext(){
        val r = api.getStatusContext(statusId).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun getCard(){
        val r = api.getCard(statusId).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun favouritedBy(){
        val r = api.getFavouritedBy(statusId, rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun rebloggedBy(){
        val r = api.getRebloggedBy(statusId, rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun post(){
        var postedStatus: Status? = null

        //post
        run{
            val r = api.postStatus(StatusBuilder(text = "ハローハロー")).execute()
            Assert.assertTrue(r.isSuccessful)
            postedStatus = r.body()
        }

        //reblog, unreblog
        run{
            val r = api.reblogStatus(postedStatus!!.id).execute()
            Assert.assertTrue(r.isSuccessful)

            val u = api.unreblogStatus(postedStatus!!.id).execute()
            Assert.assertTrue(u.isSuccessful)
        }

        //fav, unfav
        run{
            val r = api.favouritStatus(postedStatus!!.id).execute()
            Assert.assertTrue(r.isSuccessful)

            val u = api.unfavouritStatus(postedStatus!!.id).execute()
            Assert.assertTrue(u.isSuccessful)
        }

        // delete
        run{
            val r = api.deleteStatus(postedStatus!!.id).execute()
            Assert.assertTrue(r.isSuccessful)
        }
    }

    @Test
    fun postWithMedia(){
        val attachment = listOf(416073L)
        val r = api.postStatus(StatusBuilder(text = "システムオールレッド", mediaIds = attachment, visibility = "direct")).execute()
        Assert.assertTrue(r.isSuccessful)
    }
}