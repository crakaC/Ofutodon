package com.crakac.ofutodon

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
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
import com.crakac.ofutodon.api.MastodonAPI
import com.crakac.ofutodon.api.entity.AccessToken
import com.crakac.ofutodon.api.entity.AppCredentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val TAG: String = "HomeActivity"
    val ENDPOINT_AUTHORIZE: String = "/oauth/authorize"
    val OAUTH_SCOPE: String = "read write follow"
    val OAUTH_TARGET_DOMAIN: String = "oauth_target_domain"

    @BindView(R.id.pagerTab)
    lateinit var pagerTab: PagerTabStrip

    @BindView(R.id.pager)
    lateinit var pager: ViewPager

    @BindView(R.id.fab2)
    lateinit var fab2: FloatingActionButton

    var adapter: MyFragmentPagerAdapter? = null
    var instanceDomain: String = "friends.nico"

    var mastodon: MastodonAPI? = null

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

        //mastodon API
        mastodon = createMastodonApi()
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
        if (intent == null) return

        if (intent.data.scheme != getString(R.string.oauth_scheme)) {
            return
        }
        val code = intent.data.getQueryParameter("code")
        val error = intent.data.getQueryParameter("error")

        if(code != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val domain = prefs.getString(OAUTH_TARGET_DOMAIN, "")
            val id = prefs.getString("${domain}.client_id", "")
            val secret = prefs.getString("${domain}.client_secret", "")
            createMastodonApi().fetchOAuthToken(id, secret, oauthRedirectUri, code, "authorization_code")?.enqueue(object : Callback<AccessToken> {
                override fun onFailure(call: Call<AccessToken>?, t: Throwable?) {
                    Log.w(TAG, "fetchOAuthTokenFailed")
                }

                override fun onResponse(call: Call<AccessToken>?, response: Response<AccessToken>?) {
                    if (response == null || !response.isSuccessful) {
                        Log.w(TAG, "fetchOAuthToken is not successful")
                        return
                    }

                    PreferenceManager.getDefaultSharedPreferences(this@HomeActivity).edit()
                            .putString("${instanceDomain}.access_token", response.body().accessToken)
                            .apply()
                }
            })
        }
    }

    fun createMastodonApi(): MastodonAPI {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl("https://${instanceDomain}")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(MastodonAPI::class.java)
    }

    fun saveAppCredential(credentials: AppCredentials, domain: String) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("${domain}.client_id", credentials.clientId)
                .putString("${domain}.client_secret", credentials.clientSecret)
                .putLong("${domain}.id", credentials.id)
                .apply()
    }

    fun alreadyHasAppCredential(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val clientId = prefs.getString("${instanceDomain}.client_id", null)
        val clientSecret = prefs.getString("${instanceDomain}.client_secret", null)
        return clientId != null && clientSecret != null
    }

    fun registerApplication() {
        mastodon?.registerApplication(getString(R.string.app_name), oauthRedirectUri, OAUTH_SCOPE, "http://crakac.com")
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
                PreferenceManager.getDefaultSharedPreferences(this@HomeActivity).edit()
                        .putString(OAUTH_TARGET_DOMAIN, instanceDomain)
                        .apply()
                val uri = Uri.Builder()
                        .scheme("https")
                        .authority(instanceDomain)
                        .path(ENDPOINT_AUTHORIZE)
                        .appendQueryParameter("client_id", credential.clientId)
                        .appendQueryParameter("redirect_uri", credential.redirectUri)
                        .appendQueryParameter("response_type", "code")
                        .appendQueryParameter("scope", OAUTH_SCOPE)
                        .build()
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        })
    }

    @OnClick(R.id.fab2)
    fun onClickFab(v: View) {

    }
}
