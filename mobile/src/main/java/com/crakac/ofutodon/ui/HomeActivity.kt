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
import android.view.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.Mastodon
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.AccessToken
import com.crakac.ofutodon.model.api.entity.Account
import com.crakac.ofutodon.model.api.entity.AppCredentials
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.util.C
import com.crakac.ofutodon.util.PrefsUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val TAG: String = "HomeActivity"

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

        drawer.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        if (MastodonUtil.hasAccessToken(instanceDomain)) {
            val token = MastodonUtil.getAccessToken(instanceDomain)
            mastodon = MastodonUtil.api(instanceDomain, token)
        } else {
            if (MastodonUtil.hasAppCredential(instanceDomain)) {
                Snackbar.make(fab, "Already has App Credentials", Snackbar.LENGTH_SHORT).show()
                startAuthorize(instanceDomain)
            } else {
                registerApplication()
            }
        }

        //fragments
        if (adapter == null) {
            adapter = MyFragmentPagerAdapter(supportFragmentManager)
            adapter!!.add(HomeTimelineFragment())
            adapter!!.add(LocalTimelineFragment())
        }

        pager.adapter = adapter
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                val fragment = adapter?.instantiateItem(pager, position) as StatusFragment?
                fragment?.updateRelativeTime()
            }
        })
        tabLayout.setupWithViewPager(pager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                val fragment = adapter?.instantiateItem(pager, tab.position) as StatusFragment?
                fragment?.scrollToTop()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {}
        })

        drawer.setOnApplyWindowInsetsListener { v, insets ->
            // inset the toolbar down by the status bar height
            val lpToolbar = toolbar
                    .layoutParams as ViewGroup.MarginLayoutParams
            lpToolbar.topMargin += insets.systemWindowInsetTop
            lpToolbar.leftMargin += insets.systemWindowInsetLeft
            lpToolbar.rightMargin += insets.systemWindowInsetRight
            toolbar.layoutParams = lpToolbar

            // inset the fab for the navbar
            val lpFab = fab
                    .layoutParams as ViewGroup.MarginLayoutParams
            lpFab.bottomMargin += insets.systemWindowInsetBottom // portrait
            lpFab.rightMargin += insets.systemWindowInsetRight // landscape
            fab.layoutParams = lpFab

            // clear this listener so insets aren't re-applied
            drawer.setOnApplyWindowInsetsListener(null)

            insets.consumeSystemWindowInsets()
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
            if (domain == null) {
                Log.e(TAG, "target domain is null")
                //TODO Show dialog
                return
            }
            MastodonUtil.fetchAccessToken(domain, oauthRedirectUri, code).enqueue(fetchAccessTokenCallback)
        }
    }

    val fetchAccessTokenCallback = object : Callback<AccessToken> {
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
            Log.w(TAG, "fetchOAuthTokenFailed")
        }
    }

    fun onFetchAccessTokenSuccess(domain: String, accessToken: String) {
        //save access token
        MastodonUtil.saveAccessToken(domain, accessToken)

        MastodonUtil.api(domain, accessToken)

        //verify credentials
        MastodonUtil.api?.getCurrentAccount()?.enqueue(object : Callback<Account> {
            override fun onResponse(call: Call<Account>?, response: Response<Account>?) {
                if (response == null || !response.isSuccessful) {
                    Log.w(TAG, "fetchCurrentAccount Failed")
                    return
                }
                Snackbar.make(fab, "Login Success: $domain", Snackbar.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Account>?, t: Throwable?) {
                Snackbar.make(fab, "Login Failed: $domain", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    fun registerApplication() {
        MastodonUtil.registerApplication(instanceDomain, getString(R.string.app_name), oauthRedirectUri, "http://crakac.com")
                .enqueue(object : Callback<AppCredentials> {
                    override fun onFailure(call: Call<AppCredentials>?, t: Throwable?) {
                    }

                    override fun onResponse(call: Call<AppCredentials>?, response: Response<AppCredentials>?) {
                        if (response == null || !response.isSuccessful) {
                            Snackbar.make(findViewById(R.id.fab), "Something wrong", Snackbar.LENGTH_SHORT).show()
                            return;
                            //TODO Retry
                        }

                        response.body()?.let { credential ->
                            MastodonUtil.saveAppCredential(instanceDomain, credential)
                        }
                        startAuthorize(instanceDomain)
                    }
                })
    }

    fun startAuthorize(domain: String) {
        // Save target domain since keeping
        PrefsUtil.putString(C.OAUTH_TARGET_DOMAIN, domain)
        val uri = MastodonUtil.createAuthenticationUri(domain, oauthRedirectUri)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    @OnClick(R.id.fab)
    fun onClickFab(fab: View) {
        val tootIntent = Intent(this, TootActivity::class.java)
        FabTransform.addExtras(tootIntent, ContextCompat.getColor(this, R.color.colorAccent), R.drawable.ic_message)
        val options = ActivityOptions.makeSceneTransitionAnimation(this, fab, getString(R.string.transition_name_toot_dialog));
        startActivity(tootIntent, options.toBundle())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_N) {
            onClickFab(fab)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
