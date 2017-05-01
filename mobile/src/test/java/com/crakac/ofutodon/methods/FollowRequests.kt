package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class FollowRequests: MastodonMethodTestBase(){
    @Test
    fun getFollowRequests(){
        val r = api.getFollowRequests(rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun authorizeRequest(){
        val r = api.authorizeFollowRequest(1).execute()
        Assert.assertTrue(r.isSuccessful)
    }
    @Test
    fun rejectRequest(){
        val r = api.rejectFollowRequest(1).execute()
        Assert.assertTrue(r.isSuccessful)
    }
}