package com.crakac.ofutodon.methods

import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.model.api.C
import com.crakac.ofutodon.model.api.entity.AppCredentials
import org.junit.Assert
import org.junit.Test

class Apps : MastodonMethodTestBase(){
    @Test
    fun register() {
        var credential: AppCredentials? = null
        run {
            val response = noTokenApi.registerApplication("OfutodonTest", "urn:ietf:wg:oauth:2.0:oob", C.OAUTH_SCOPES, "https://example.com").execute()
            Assert.assertTrue(response.isSuccessful)
            credential = response.body()
        }
        credential?.let{
            val response = noTokenApi.fetchAccessTokenByPassword(
                    it.clientId,
                    it.clientSecret,
                    "password",
                    BuildConfig.TEST_USER,
                    BuildConfig.TEST_PASSWORD,
                    C.OAUTH_SCOPES
            ).execute()
            Assert.assertTrue(response.isSuccessful)
            System.out.println(response.body()!!.scope)
        }
    }
}