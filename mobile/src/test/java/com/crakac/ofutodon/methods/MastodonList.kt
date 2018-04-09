package com.crakac.ofutodon.methods

import org.junit.Assert
import org.junit.Test

class MastodonList: MastodonMethodTestBase() {
    val TAG: String = "MastodonList"

    @Test
    fun getLists(){
        val r = api.getLists().execute()
        Assert.assertTrue(r.isSuccessful)

        val r2 = api.getList(1).execute()
        Assert.assertTrue(r2.isSuccessful)
    }

    @Test
    fun getAccountsInList(){
        val r = api.getAccountsInList(1).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun getListsOfAccount(){
        val r = api.getListsOfAccount(895).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun addAndRemoveAcount(){
        var r = api.addAccountToList(1, arrayListOf(2, 3)).execute()
        Assert.assertTrue(r.isSuccessful)
        r = api.removeAccountFromList(1, arrayListOf(2,3)).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun createAndDeleteList(){
        val r = api.createList("test").execute()
        Assert.assertTrue(r.isSuccessful)
        val list = r.body()!!
        Assert.assertEquals("test", list.title)

        val r2 = api.updateListName(list.id, "hoge").execute()
        Assert.assertEquals("hoge", r2.body()!!.title)

        val res = api.deleteList(list.id).execute()
        Assert.assertTrue(res.isSuccessful)
    }
}