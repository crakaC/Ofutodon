package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class Follows : MastodonMethodTestBase(){
    @Test
    fun remoteFollow(){
        val r = api.followRemoteUser("hoge@example.com").execute()
        Assert.assertTrue(r.isSuccessful)
    }
}