package com.crakac.ofutodon.methods

import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.api.entity.StatusBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert
import org.junit.Test
import java.io.File

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
            val r = api.favouriteStatus(postedStatus!!.id).execute()
            Assert.assertTrue(r.isSuccessful)

            val u = api.unfavouriteStatus(postedStatus!!.id).execute()
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
        val f = File(javaClass.getResource("/lgtm.webm").path)
        val reqFile = RequestBody.create(MediaType.parse("video/webm"), f)
        val body = MultipartBody.Part.createFormData("file", f.name, reqFile)
        val attachment = api.uploadMediaAttachment(body).execute().body()
        val r = api.postStatus(StatusBuilder(text = "( ˘ω˘)ｽﾔｧ", mediaIds = listOf(attachment!!.id))).execute()
        Assert.assertTrue(r.isSuccessful)
    }
}