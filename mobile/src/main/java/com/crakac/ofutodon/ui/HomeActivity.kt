package com.crakac.ofutodon.ui

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.Mastodon
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.AccessToken
import com.crakac.ofutodon.model.api.entity.AppCredentials
import com.crakac.ofutodon.transition.FabTransform
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

    @BindView(R.id.tab)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.pager)
    lateinit var pager: ViewPager

    @BindView(R.id.fab)
    lateinit var fab: View

    @BindView(R.id.drawer_layout)
    lateinit var drawer: DrawerLayout

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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        //fragments
        if (adapter == null) {
            adapter = MyFragmentPagerAdapter(supportFragmentManager)
            adapter?.add(StatusFragment())
            adapter?.add(StatusFragment())
            adapter?.add(StatusFragment())
        }

        pager.adapter = adapter
        tabLayout.setupWithViewPager(pager)


        PrefsUtil.getString("${instanceDomain}.${ACCESS_TOKEN}").let {
            mastodon = MastodonUtil.createMastodonApi(instanceDomain, it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString("instanceDomain", instanceDomain)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
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
            if (domain == null || id == null || secret == null) {
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
        mastodon?.registerApplication(getString(R.string.app_name), oauthRedirectUri, OAUTH_SCOPES, "http://crakac.com")
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

    fun startAuthorize(domain: String, clientId: String) {
        // Save target domain since keeping
        PrefsUtil.putString(C.OAUTH_TARGET_DOMAIN, domain)
        val uri = MastodonUtil.createAuthenticationUri(domain, clientId, oauthRedirectUri)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    @OnClick(R.id.fab)
    fun onClickFab(fab: View) {
        val login = Intent(this , TootActivity::class.java)
        FabTransform.addExtras(login, ContextCompat.getColor(this, R.color.colorAccent), R.drawable.ic_menu_send)
        val options = ActivityOptions.makeSceneTransitionAnimation(this, fab, getString(R.string.transition_name_login));
        startActivityForResult(login, 128, options.toBundle())

//        if (alreadyHasAppCredential()) {
//            Snackbar.make(fab, "Already has App Credentials", Snackbar.LENGTH_SHORT).show()
//            val clientId = PrefsUtil.getString("${instanceDomain}.${CLIENT_ID}")!!
//            startAuthorize(instanceDomain, clientId)
//        } else {
//            registerApplication()
//        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_N){
            onClickFab(fab)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun onLoginSuccess(domain: String, accessToken: String) {
        //save access token in preferences
        PrefsUtil.putString("${domain}.${ACCESS_TOKEN}", accessToken)
        Snackbar.make(fab, "Login Success: $domain", Snackbar.LENGTH_SHORT).show()
    }
}
