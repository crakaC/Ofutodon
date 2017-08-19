package com.crakac.ofutodon.methods

import com.crakac.ofutodon.model.api.MastodonUtil
import org.junit.Assert
import org.junit.Test

class MastodonAccountTest {
    val TAG: String = "MastodonAccountTest"
    @Test
    fun acct(){
        val account = MastodonUtil.Account("localhost", 0, "test", "")
        Assert.assertEquals(account.acct, "localhost@test")
    }
}