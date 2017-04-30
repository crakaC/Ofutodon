package com.crakac.ofutodon

import com.crakac.ofutodon.api.C
import com.crakac.ofutodon.api.entity.AppCredentials
import org.junit.Assert
import org.junit.Test

class MastodonApiTest{
    val TEST_USER = "hoge@fuga.com"
    val TEST_PASSWORD = "hogehoge"

    @Test
    fun appRegisterTest() {
        val mastodon = TestUtil.createApi()
        var credential: AppCredentials? = null
        run {
            val response = mastodon.registerApplication("OfutodonTest", "urn:ietf:wg:oauth:2.0:oob", C.OAUTH_SCOPES, "https://example.com").execute()
            Assert.assertTrue(response.isSuccessful)
            credential = response.body()
        }
        credential?.let{
            val response = mastodon.fetchAccessTokenByPassword(
                    it.clientId,
                    it.clientSecret,
                    "password",
                    TEST_USER,
                    TEST_PASSWORD,
                    C.OAUTH_SCOPES
            ).execute()
            Assert.assertTrue(response.isSuccessful)
            System.out.println(response.body().scope)
        }
    }
}