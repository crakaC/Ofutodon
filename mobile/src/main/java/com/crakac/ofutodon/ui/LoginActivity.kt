package com.crakac.ofutodon.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import com.crakac.ofutodon.R
import com.crakac.ofutodon.db.AppDatabase
import com.crakac.ofutodon.db.User
import com.crakac.ofutodon.model.api.MastodonCallback
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.AccessToken
import com.crakac.ofutodon.model.api.entity.Account
import com.crakac.ofutodon.model.api.entity.AppCredentials
import com.crakac.ofutodon.util.C
import com.crakac.ofutodon.util.PrefsUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    companion object {
        val TAG = "LoginActivity"
        val ACTION_ADD_ACCOUNT = "add_account"
    }

    lateinit var domainEditText: EditText
    lateinit var loginButton: Button
    private val instanceDomain
        get() = domainEditText.text.toString()
    private val oauthRedirectUri
        get() = "${getString(R.string.oauth_scheme)}://${getString(R.string.oauth_redirect_host)}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        domainEditText = findViewById(R.id.domain)
        savedInstanceState?.let {
            domainEditText.setText(it.getString("instanceDomain"))
        }
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        domainEditText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                inputManager.hideSoftInputFromWindow(domainEditText.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        loginButton = findViewById(R.id.login)
        loginButton.setOnClickListener {
            registerApplication()
        }

        MastodonUtil.existsCurrentAccount { account ->
            // 既にアカウントが存在している状態で初期画面を開いたらHomeActivityに自動的に遷移する
            if (account != null && intent.action != ACTION_ADD_ACCOUNT) {
                MastodonUtil.api(account)
                startHomeActivity()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString("instanceDomain", instanceDomain)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null || intent.data == null) return

        if (intent.data.scheme != getString(R.string.oauth_scheme)) {
            return
        }
        val code = intent.data.getQueryParameter("code")
        val error = intent.data.getQueryParameter("error")

        if (code != null) {
            val domain = PrefsUtil.getString(C.OAUTH_TARGET_DOMAIN)
            if (domain == null) {
                Log.e(TAG, "target domain is null")
                //TODO Show dialog
                return
            }
            MastodonUtil.fetchAccessToken(domain, oauthRedirectUri, code).enqueue(fetchAccessTokenCallback)
        }
    }

    private val fetchAccessTokenCallback = object : Callback<AccessToken> {
        override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>?) {
            if (response == null || !response.isSuccessful) {
                Log.w(TAG, "fetchOAuthToken is not successful")
                return
            }
            val domain = call.request().url().host()
            response.body()?.accessToken?.let { token ->
                onFetchAccessTokenSuccess(domain, token)
            }
            PrefsUtil.remove(C.OAUTH_TARGET_DOMAIN)
        }

        override fun onFailure(call: Call<AccessToken>?, t: Throwable?) {
        }
    }

    private fun onFetchAccessTokenSuccess(domain: String, accessToken: String) {
        MastodonUtil.api(domain, accessToken)
        //verify credentials
        MastodonUtil.api?.getCurrentAccount()?.enqueue(object : MastodonCallback<Account> {
            override fun onSuccess(result: Account) {
                val user = User().apply {
                    this.name = result.username
                    this.userId = result.id
                    this.avator = result.avatarStatic
                    this.domain = domain
                    this.token = accessToken
                }
                AppDatabase.execute {
                    AppDatabase.instance.userDao().insert(user)
                    val newUser = AppDatabase.instance.userDao().select(user.userId, user.domain)
                    PrefsUtil.putInt(C.CURRENT_USER_ID, newUser.id)
                    AppDatabase.uiThread {
                        startHomeActivity()
                    }
                }
            }

            override fun onFailure(call: Call<Account>?, t: Throwable?) {
            }
        })
    }

    private fun registerApplication() {
        MastodonUtil.registerApplication(instanceDomain, getString(R.string.app_name), oauthRedirectUri, getString(R.string.website))
                .enqueue(object : MastodonCallback<AppCredentials> {
                    override fun onSuccess(result: AppCredentials) {
                        MastodonUtil.saveAppCredential(instanceDomain, result)
                        startAuthorize(instanceDomain)
                    }

                    override fun onFailure(call: Call<AppCredentials>?, t: Throwable?) {
                        Snackbar.make(domainEditText.rootView, "Something wrong", Snackbar.LENGTH_SHORT).show()
                    }
                })
    }

    private fun startAuthorize(domain: String) {
        // Save target domain since keeping
        PrefsUtil.putString(C.OAUTH_TARGET_DOMAIN, domain)
        val uri = MastodonUtil.createAuthenticationUri(domain, oauthRedirectUri)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.action = HomeActivity.ACTION_RELOAD
        startActivity(intent)
        finish()
    }
}