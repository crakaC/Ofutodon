package com.crakac.ofutodon.api

import android.net.Uri
import com.crakac.ofutodon.api.entity.AccessToken
import com.crakac.ofutodon.api.entity.AppCredentials
import com.crakac.ofutodon.db.AppDatabase
import com.crakac.ofutodon.db.User
import com.crakac.ofutodon.util.PrefsUtil
import retrofit2.Call

class MastodonUtil private constructor() {
    companion object {
        const val TAG: String = "MastodonUtil"
        fun createAuthenticationUri(domain: String, redirectUri: String): Uri {
            return Uri.Builder()
                    .scheme("https")
                    .authority(domain)
                    .path(C.ENDPOINT_AUTHORIZE)
                    .appendQueryParameter(C.CLIENT_ID, getClientId(domain))
                    .appendQueryParameter(C.REDIRECT_URI, redirectUri)
                    .appendQueryParameter(C.RESPONSE_TYPE, "code")
                    .appendQueryParameter(C.SCOPE, C.OAUTH_SCOPES)
                    .build()
        }

        fun saveAppCredential(domain: String, credentials: AppCredentials) {
            PrefsUtil.putString("$domain.${C.CLIENT_ID}", credentials.clientId)
            PrefsUtil.putString("$domain.${C.CLIENT_SECRET}", credentials.clientSecret)
            PrefsUtil.putLong("$domain.${C.CREDENTIAL_ID}", credentials.id)
        }

        fun hasAppCredential(domain: String): Boolean {
            val clientId = getClientId(domain)
            val clientSecret = PrefsUtil.getString("$domain.${C.CLIENT_SECRET}")
            return clientId != null && clientSecret != null
        }

        fun existsCurrentAccount(callBack: (account: User?) -> Unit) {
            AppDatabase.execute {
                val user = AppDatabase.instance.userDao().getUser(PrefsUtil.getInt(com.crakac.ofutodon.util.C.CURRENT_USER_ID, 0))
                AppDatabase.uiThread {
                    callBack(user)
                }
            }
        }

        fun getClientId(domain: String): String? {
            return PrefsUtil.getString("$domain.${C.CLIENT_ID}")
        }

        fun getClientSecret(domain: String): String? {
            return PrefsUtil.getString("$domain.${C.CLIENT_SECRET}")
        }

        fun registerApplication(domain: String, clientName: String, redirectUris: String, website: String): Call<AppCredentials> {
            return Mastodon.initialize(domain).registerApplication(
                    clientName,
                    redirectUris,
                    C.OAUTH_SCOPES,
                    website
            )
        }

        fun fetchAccessToken(domain: String, redirectUri: String, code: String): Call<AccessToken> {
            val id = getClientId(domain)!!
            val secret = getClientSecret(domain)!!
            return Mastodon.initialize(domain).fetchAccessToken(id, secret, redirectUri, code, C.AUTHORIZATION_CODE)
        }
    }
}