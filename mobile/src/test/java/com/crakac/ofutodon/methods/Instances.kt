package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class Instances: MastodonMethodTestBase() {
    @Test
    fun instanceInfo(){
        val r = noTokenApi.getInstanceInformation().execute()
        Assert.assertTrue(r.isSuccessful)
    }
}