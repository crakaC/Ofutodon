package com.crakac.ofutodon.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.crakac.ofutodon.R
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.MastodonCallback
import com.crakac.ofutodon.api.MastodonUtil
import com.crakac.ofutodon.api.entity.AccessToken
import com.crakac.ofutodon.api.entity.Account
import com.crakac.ofutodon.api.entity.AppCredentials
import com.crakac.ofutodon.db.AppDatabase
import com.crakac.ofutodon.db.User
import com.crakac.ofutodon.util.C
import com.crakac.ofutodon.util.PrefsUtil
import retrofit2.Call

class LoginActivity : AppCompatActivity() {
    companion object {
        val TAG = "LoginActivity"
        val ACTION_ADD_ACCOUNT = "add_account"
        val REQUEST_AUTHORIZE = 1001 // 特に意味はない
    }

    lateinit var domainEditText: EditText
    lateinit var loginButton: Button
    lateinit var progress: ProgressBar
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
        domainEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                inputManager.hideSoftInputFromWindow(domainEditText.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        domainEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                loginButton.isEnabled = s.count() > 0
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })
        loginButton = findViewById(R.id.login)
        loginButton.setOnClickListener {
            if (MastodonUtil.hasAppCredential(instanceDomain)) {
                startAuthorize(instanceDomain)
            } else {
                registerApplication()
            }
        }
        progress = findViewById(R.id.progress)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString("instanceDomain", instanceDomain)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setLoading(false)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setLoading(false)
        if (intent == null || intent.data == null) return

        if (intent.data.scheme != getString(R.string.oauth_scheme)) {
            return
        }
        val code = intent.data.getQueryParameter("code")
        val error = intent.data.getQueryParameter("error")

        if (error != null) {
            //TODO do something
            return
        }

        if (code != null) {
            val domain = PrefsUtil.getString(C.OAUTH_TARGET_DOMAIN)
            if (domain == null) {
                Log.e(TAG, "target domain is null")
                //TODO Show dialog
                return
            }
            setLoading(true)
            MastodonUtil.fetchAccessToken(domain, oauthRedirectUri, code).enqueue(fetchAccessTokenCallback)
        }
    }

    private val fetchAccessTokenCallback = object : MastodonCallback<AccessToken> {
        override fun onSuccess(result: AccessToken) {
            onFetchAccessTokenSuccess(instanceDomain, result.accessToken)
            PrefsUtil.remove(C.OAUTH_TARGET_DOMAIN)
        }
    }

    private fun onFetchAccessTokenSuccess(domain: String, accessToken: String) {
        //verify credentials
        Mastodon.initialize(domain, accessToken).getCurrentAccount().enqueue(object : MastodonCallback<Account> {
            override fun onSuccess(result: Account) {
                val user = User(name = result.username,
                        displayName = result.displayName,
                        userId = result.id,
                        avator = result.avatarStatic,
                        domain = domain,
                        token = accessToken)
                AppDatabase.execute {
                    val oldUser = AppDatabase.instance.userDao().select(user.userId, user.domain)
                    if (oldUser != null) {
                        user.id = oldUser.id
                        AppDatabase.instance.userDao().update(user) //update token
                    } else {
                        AppDatabase.instance.userDao().insert(user)
                    }
                    val newUser = AppDatabase.instance.userDao().select(user.userId, user.domain)!!
                    PrefsUtil.putInt(C.CURRENT_USER_ID, newUser.id)
                    AppDatabase.uiThread {
                        Mastodon.initialize(newUser)
                        startHomeActivity()
                    }
                }
            }

            override fun onFailure(call: Call<Account>?, t: Throwable?) {
                setLoading(false)
                Snackbar.make(domainEditText, "Something wrong", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerApplication() {
        setLoading(true)
        MastodonUtil.registerApplication(instanceDomain, getString(R.string.app_name), oauthRedirectUri, getString(R.string.website)).enqueue(object : MastodonCallback<AppCredentials> {
            override fun onSuccess(result: AppCredentials) {
                MastodonUtil.saveAppCredential(instanceDomain, result)
                startAuthorize(instanceDomain)
            }

            override fun onFailure(call: Call<AppCredentials>?, t: Throwable?) {
                Snackbar.make(domainEditText, "Something wrong", Snackbar.LENGTH_SHORT).show()
                setLoading(false)
            }
        })
    }

    private fun startAuthorize(domain: String) {
        // Save target domain since keeping
        PrefsUtil.putString(C.OAUTH_TARGET_DOMAIN, domain)
        val uri = MastodonUtil.createAuthenticationUri(domain, oauthRedirectUri)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivityForResult(intent, REQUEST_AUTHORIZE)
    }

    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.action = HomeActivity.ACTION_RELOAD
        startActivity(intent)
        finish()
    }

    private fun setLoading(isEnable: Boolean) {
        if (isEnable) {
            loginButton.visibility = View.GONE
            progress.visibility = View.VISIBLE
            domainEditText.isEnabled = false
        } else {
            loginButton.visibility = View.VISIBLE
            progress.visibility = View.GONE
            domainEditText.isEnabled = true
        }
    }
}