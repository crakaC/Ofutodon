package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class Favourites: MastodonMethodTestBase(){

    @Test
    fun getFavourites(){
        val r = api.getFavourites(rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

}