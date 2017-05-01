package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class Search: MastodonMethodTestBase(){

    @Test
    fun searchTest(){
        val r = api.search("hoge").execute()
        Assert.assertTrue(r.isSuccessful)
    }
}