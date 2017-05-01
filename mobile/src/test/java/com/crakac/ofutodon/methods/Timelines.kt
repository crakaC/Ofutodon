package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class Timelines: MastodonMethodTestBase(){

    @Test
    fun getHomeTimeline(){
        val r = api.getHomeTileline(false, rangeLong.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun publicTimeline(){
        val r = noTokenApi.getPublicTimeline(rangeLong.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun hashtagTimeline(){
        val r = noTokenApi.getHashtagTimeline("test", null, rangeLong.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

}