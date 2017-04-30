package com.crakac.ofutodon

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.view.PagerTabStrip
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.MastodonUtil
import com.crakac.ofutodon.api.entity.AccessToken
import com.crakac.ofutodon.api.entity.Account
import com.crakac.ofutodon.api.entity.AppCredentials
import com.crakac.ofutodon.util.C
import com.crakac.ofutodon.util.PrefsUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val TAG: String = "HomeActivity"
    val CLIENT_ID: String = "client_id"
    val CLIENT_SECRET: String = "client_secret"
    val ACCESS_TOKEN: String = "access_token"
    val OAUTH_SCOPES: String = "read write follow"
    val AUTHORIZATION_CODE: String = "authorization_code"

    @BindView(R.id.pagerTab)
    lateinit var pagerTab: PagerTabStrip

    @BindView(R.id.pager)
    lateinit var pager: ViewPager

    @BindView(R.id.fab2)
    lateinit var fab2: FloatingActionButton

    var adapter: MyFragmentPagerAdapter? = null
    var instanceDomain: String = "friends.nico"

    var mastodon: Mastodon? = null

    var oauthRedirectUri: String = ""
        get() {
            return "${getString(R.string.oauth_scheme)}://${getString(R.string.oauth_redirect_host)}"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ButterKnife.bind(this)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            if (alreadyHasAppCredential()) {
                Snackbar.make(view, "Already has App Credentials", Snackbar.LENGTH_SHORT).show()
                val clientId = PrefsUtil.getString("${instanceDomain}.${CLIENT_ID}")!!
                startAuthorize(instanceDomain, clientId)
            } else {
                registerApplication()
            }
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        //fragments
        if (adapter == null) {
            adapter = MyFragmentPagerAdapter(supportFragmentManager)
            for (i in 1..3) {
                adapter?.add(StatusFragment())
            }
        }
        pager.adapter = adapter
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString("instanceDomain", instanceDomain)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
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
            val id = PrefsUtil.getString("${domain}.${CLIENT_ID}")
            val secret = PrefsUtil.getString("${domain}.${CLIENT_SECRET}")
            if(domain == null || id == null || secret == null){
                Log.d(TAG, "Authentication Failed due to some Preferences are null")
                // TODO show dialog
                return
            }
            MastodonUtil.createMastodonApi(domain).fetchAccessToken(id, secret, oauthRedirectUri, code, AUTHORIZATION_CODE)
                    .enqueue(object : Callback<AccessToken> {
                override fun onResponse(call: Call<AccessToken>?, response: Response<AccessToken>?) {
                    if (response == null || !response.isSuccessful) {
                        Log.w(TAG, "fetchOAuthToken is not successful")
                        return
                    }

                    onLoginSuccess(domain, response.body().accessToken)
                    PrefsUtil.remove(domain)
                }
                override fun onFailure(call: Call<AccessToken>?, t: Throwable?) {
                    Log.w(TAG, "fetchOAuthTokenFailed")
                    PrefsUtil.remove(domain)
                }
            })
        }
    }

    fun saveAppCredential(credentials: AppCredentials, domain: String) {
        PrefsUtil.putString("${domain}.${CLIENT_ID}", credentials.clientId)
        PrefsUtil.putString("${domain}.${CLIENT_SECRET}", credentials.clientSecret)
        PrefsUtil.putLong("${domain}.id", credentials.id)
    }

    fun alreadyHasAppCredential(): Boolean {
        val clientId = PrefsUtil.getString("${instanceDomain}.${CLIENT_ID}")
        val clientSecret = PrefsUtil.getString("${instanceDomain}.${CLIENT_SECRET}")
        return clientId != null && clientSecret != null
    }

    fun registerApplication() {
        mastodon?.registerApplication(getString(R.string.app_name), oauthRedirectUri,  OAUTH_SCOPES, "http://crakac.com")
                ?.enqueue(object : Callback<AppCredentials> {
                    override fun onFailure(call: Call<AppCredentials>?, t: Throwable?) {
                    }

                    override fun onResponse(call: Call<AppCredentials>?, response: Response<AppCredentials>?) {
                        if (response == null || !response.isSuccessful) {
                            Snackbar.make(findViewById(R.id.fab), "Something wrong", Snackbar.LENGTH_SHORT).show()
                            return;
                            //TODO Retry
                        }

                        val credential = response.body()
                        saveAppCredential(credential, instanceDomain)
                        startAuthorize(instanceDomain, credential.clientId)
                    }
                })
    }

    @OnClick(R.id.fab2)
    fun onClickFab(v: View) {
        //mastodon API
        PrefsUtil.getString("${instanceDomain}.${ACCESS_TOKEN}").let {
            mastodon = MastodonUtil.createMastodonApi(instanceDomain, it)
            mastodon?.getCurrentAccount()?.enqueue(
                object: Callback<Account>{
                    override fun onResponse(call: Call<Account>?, response: Response<Account>?) {
                        if(response == null || !response.isSuccessful){
                            Log.w(TAG, "fetch account failed")
                            return
                        }
                    }
                    override fun onFailure(call: Call<Account>?, t: Throwable?) {
                    }
                }
            )
        }
    }

    fun startAuthorize(domain: String, clientId: String) {
        // Save target domain since keeping
        PrefsUtil.putString(C.OAUTH_TARGET_DOMAIN, domain)
        val uri = MastodonUtil.createAuthenticationUri(domain, clientId, oauthRedirectUri)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    fun onLoginSuccess(domain: String, accessToken: String){
        //save access token in preferences
        PrefsUtil.putString("${domain}.${ACCESS_TOKEN}", accessToken)
        Snackbar.make(fab2, "Login Success: $domain", Snackbar.LENGTH_SHORT).show()
    }
}
