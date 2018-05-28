package com.crakac.ofutodon.ui

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.crakac.ofutodon.R
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.MastodonCallback
import com.crakac.ofutodon.api.entity.Account
import com.crakac.ofutodon.db.AppDatabase
import com.crakac.ofutodon.db.User
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.ui.adapter.MyFragmentPagerAdapter
import com.crakac.ofutodon.ui.adapter.UserAccountAdapter
import com.crakac.ofutodon.util.C
import com.crakac.ofutodon.util.GlideApp
import com.crakac.ofutodon.util.HtmlUtil
import com.crakac.ofutodon.util.PrefsUtil

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val ACTION_RELOAD = "action_reload"
    }
    val TAG: String = "HomeActivity"

    private val tabLayout by lazy { findViewById<TabLayout>(R.id.tab) }

    private val pager by lazy { findViewById<ViewPager>(R.id.pager) }

    private val fab by lazy { findViewById<View>(R.id.fab) }

    private val drawer by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val drawerList by lazy { findViewById<ListView>(R.id.drawer_list) }

    var adapter: MyFragmentPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = Mastodon.api.userAccount?.domain
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
            adapter!!.add(NotificationFragment())
            adapter!!.add(HomeTimelineFragment())
            adapter!!.add(LocalTimelineFragment())
        }

        pager.adapter = adapter
        pager.currentItem = 1
        pager.offscreenPageLimit = 10
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                val fragment = adapter?.instantiateItem(pager, position) as MastodonApiFragment<*, *>?
                fragment?.refreshItem()
            }
        })
        tabLayout.setupWithViewPager(pager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                val fragment = adapter?.instantiateItem(pager, tab.position) as MastodonApiFragment<*, *>?
                fragment?.scrollToTop()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {}
        })

        drawer.setOnApplyWindowInsetsListener { _, insets ->
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

        fab.setOnClickListener { v ->
            onClickFab(v)
        }

        val userAdapter = UserAccountAdapter(this, { adapter ->
            drawerList.adapter = adapter
        })
        userAdapter.onClickUserListener = { user ->
            switchAccount(user)
        }

        val header = findViewById<ImageView>(R.id.header)
        val avatar = findViewById<ImageView>(R.id.avatar_icon)
        val userName = findViewById<TextView>(R.id.user_name)
        val displayName = findViewById<TextView>(R.id.display_name)
        val userAccount = Mastodon.api.userAccount
        displayName.text = userAccount?.getDisplayNameWithEmoji()
        userName.text = getString(R.string.full_user_name).format(userAccount?.name, userAccount?.domain)
        Mastodon.api.getCurrentAccount().enqueue(object : MastodonCallback<Account> {
            override fun onSuccess(result: Account) {
                GlideApp.with(applicationContext).load(result.avatar).into(avatar)
                GlideApp.with(applicationContext).load(result.headerStatic).centerCrop().into(header)
                userName.text = getString(R.string.full_user_name).format(result.username, userAccount?.domain)
                displayName.text = HtmlUtil.emojify(displayName, result.displayName, result.emojis)
                userAccount?.let{
                    it.displayName = result.displayName
                    AppDatabase.execute {
                        AppDatabase.instance.userDao().update(it)
                    }
                }
            }
        })
        drawerList.addFooterView(View.inflate(this, R.layout.list_item_add_user, null).apply {
            setOnClickListener {
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                intent.action = LoginActivity.ACTION_ADD_ACCOUNT
                startActivity(intent)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        drawer.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
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

        if (id == R.id.search) {
            startActivity(Intent(this, SearchActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun onClickFab(fab: View) {
        val tootIntent = Intent(this, TootActivity::class.java)
        FabTransform.addExtras(tootIntent, ContextCompat.getColor(this, R.color.colorAccent), android.R.color.white, R.drawable.ic_edit)
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

    override fun onNewIntent(intent: Intent?) {
        if(intent?.action == ACTION_RELOAD){
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    fun switchAccount(user: User){
        PrefsUtil.putInt(C.CURRENT_USER_ID, user.id)
        AppDatabase.execute {
            val u = AppDatabase.instance.userDao().getUser(user.id)
            Mastodon.initialize(u)
            AppDatabase.uiThread {
                startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
                finish()
            }
        }
    }
}
