package com.crakac.ofutodon.model.api

import android.net.Uri
import com.crakac.ofutodon.db.AppDatabase
import com.crakac.ofutodon.db.User
import com.crakac.ofutodon.model.api.entity.AccessToken
import com.crakac.ofutodon.model.api.entity.AppCredentials
import com.crakac.ofutodon.util.PrefsUtil
import retrofit2.Call

class MastodonUtil private constructor() {
    companion object {
        private var cached: MastodonApi? = null
        val api get() = cached
        val TAG: String = "MastodonUtil"
        fun api(domain: String, accessToken: String? = null): MastodonApi {
            cached = MastodonBuilder().setHost(domain).setAccessToken(accessToken).build()
            return cached!!
        }

        fun api(user: User): MastodonApi{
            cached = MastodonBuilder(user).build()
            return cached!!
        }

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

        fun hasAccessToken(domain: String, callBack: (String) -> Unit) {
            AppDatabase.execute {
                val users = AppDatabase.instance.userDao().getAll()
                AppDatabase.uiThread {
                    if (users.isEmpty()) {
                        callBack("")
                    } else {
                        callBack(users.first().token)
                    }
                }
            }
        }

        fun existsCurrentAccount(callBack: (account: User?) -> Unit) {
            AppDatabase.execute {
                val user = AppDatabase.instance.userDao().getCurrentUser(PrefsUtil.getInt(com.crakac.ofutodon.util.C.CURRENT_USER_ID, 0))
                AppDatabase.uiThread {
                    callBack(user)
                }
            }
        }

        fun getAccessToken(domain: String): String? {
            return PrefsUtil.getString("$domain.${C.ACCESS_TOKEN}")
        }

        fun getClientId(domain: String): String? {
            return PrefsUtil.getString("$domain.${C.CLIENT_ID}")
        }

        fun getClientSecret(domain: String): String? {
            return PrefsUtil.getString("$domain.${C.CLIENT_SECRET}")
        }

        fun registerApplication(domain: String, clientName: String, redirectUris: String, website: String): Call<AppCredentials> {
            return api(domain).registerApplication(
                    clientName,
                    redirectUris,
                    C.OAUTH_SCOPES,
                    website
            )
        }

        fun fetchAccessToken(domain: String, redirectUri: String, code: String): Call<AccessToken> {
            val id = getClientId(domain)!!
            val secret = getClientSecret(domain)!!
            return api(domain).fetchAccessToken(id, secret, redirectUri, code, C.AUTHORIZATION_CODE)
        }
    }
}