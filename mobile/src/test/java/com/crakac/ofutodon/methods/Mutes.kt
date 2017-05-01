package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class Mutes : MastodonMethodTestBase() {

    @Test
    fun getMutings(){
        val r = api.getMutingAccounts(rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }
}