package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class Blocks : MastodonMethodTestBase(){

    @Test
    fun getBlocks(){
        val r = api.getBlockingAccounts(rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

}