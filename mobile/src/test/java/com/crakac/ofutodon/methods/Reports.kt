package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class Reports : MastodonMethodTestBase(){
    @Test
    fun getReports() {
        val r = api.getReports().execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun postReports(){
        val r = api.report(1, arrayListOf(-1, -2), null).execute()
        Assert.assertTrue(r.isSuccessful)
    }
}