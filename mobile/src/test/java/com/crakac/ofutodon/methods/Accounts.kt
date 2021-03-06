package com.crakac.ofutodon.methods

import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.api.Link
import com.crakac.ofutodon.api.Pageable
import com.crakac.ofutodon.api.entity.AccountCredentials
import org.junit.Assert
import org.junit.Test

class Accounts : MastodonMethodTestBase(){

    @Test
    fun getAccount(){
        val r = api.getAccount(BuildConfig.DEBUG_ACCOUNT_ID).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun getCurrentAccount(){
        val r = api.getCurrentAccount().execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun updateCredentials(){
        run {
            val credentials = AccountCredentials("しらか")
            val r = api.updateAccountCredentials(credentials).execute()
            Assert.assertTrue(r.isSuccessful)
        }
    }

    @Test
    fun getFollowers(){
        //no range
        run {
            val r = api.getFollowers(BuildConfig.DEBUG_ACCOUNT_ID).execute()
            Assert.assertTrue(r.isSuccessful)
        }

        run {
            val r = api.getFollowers(BuildConfig.DEBUG_ACCOUNT_ID, rangeInt.q).execute()
            Assert.assertTrue(r.isSuccessful)
        }
    }

    @Test
    fun getFollowings(){
        run{
            val r = api.getFollowings(BuildConfig.DEBUG_ACCOUNT_ID).execute()
            Assert.assertTrue(r.isSuccessful)
        }
        run{
            val r = api.getFollowings(BuildConfig.DEBUG_ACCOUNT_ID, rangeInt.q).execute()
            Assert.assertTrue(r.isSuccessful)
        }
    }

    @Test
    fun getStatuses(){
        run {
            val r = api.getStatuses(BuildConfig.DEBUG_ACCOUNT_ID).execute()
            Assert.assertTrue(r.isSuccessful)
            val pageable = Pageable(r.body()!!, Link.parse(r.headers().get("link")))
            run {
                val r = api.getStatuses(BuildConfig.DEBUG_ACCOUNT_ID, range = pageable.nextRange(10).q).execute()
                Assert.assertTrue(r.isSuccessful)
            }
            run {
                val r = api.getStatuses(BuildConfig.DEBUG_ACCOUNT_ID, range = pageable.prevRange(10).q).execute()
                Assert.assertTrue(r.isSuccessful)
            }
        }

        run{
            val r = api.getStatuses(BuildConfig.DEBUG_ACCOUNT_ID, true, true, rangeLong.q).execute()
            Assert.assertTrue(r.isSuccessful)
            val statuses = r.body()!!
            statuses.forEach {
                Assert.assertTrue(it.mediaAttachments != null)
            }
        }
    }
/*
    @Test
    fun follow(){
        run {
            val r = initialize.follow(35079).execute()
            Assert.assertTrue(r.isSuccessful)
        }
        run{
            val r = initialize.unfollow(35079).execute()
            Assert.assertTrue(r.isSuccessful)
        }
    }

    @Test
    fun block(){
        run {
            val r = initialize.block(35079).execute()
            Assert.assertTrue(r.isSuccessful)
        }
        run{
            val r = initialize.unblock(35079).execute()
            Assert.assertTrue(r.isSuccessful)
        }
    }

    @Test
    fun mute(){
        run {
            val r = initialize.mute(35079).execute()
            Assert.assertTrue(r.isSuccessful)
        }
        run{
            val r = initialize.unmute(35079).execute()
            Assert.assertTrue(r.isSuccessful)
        }
    }
*/
    @Test
    fun getRelationships(){
        val r = api.getRelationships(1, BuildConfig.DEBUG_ACCOUNT_ID).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun searchAccounts(){
        val r = api.searchAccounts("test", 10).execute()
        Assert.assertTrue(r.isSuccessful)
        val accounts = r.body()!!
        Assert.assertTrue(accounts.size <= 10)
    }

    @Test
    fun getBlockingAccounts(){
        val r = api.getBlockingAccounts(rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun getFavorites(){
        val r = api.getFavourites(rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }

    @Test
    fun getFollowRequests(){
        val r = api.getFollowRequests(rangeInt.q).execute()
        Assert.assertTrue(r.isSuccessful)
    }
}