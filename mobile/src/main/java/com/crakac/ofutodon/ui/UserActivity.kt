package com.crakac.ofutodon.ui

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.crakac.ofutodon.R
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.entity.Account
import com.crakac.ofutodon.api.entity.Relationship
import com.crakac.ofutodon.ui.adapter.MyFragmentPagerAdapter
import com.crakac.ofutodon.ui.widget.ContentMovementMethod
import com.crakac.ofutodon.ui.widget.FollowButton
import com.crakac.ofutodon.util.AnimUtils
import com.crakac.ofutodon.util.GlideApp
import com.crakac.ofutodon.util.HtmlUtil
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    private val PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.67f
    private val PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.33f
    private val ALPHA_ANIMATIONS_DURATION = 200L

    private var isTheTitleVisible = false
    private var isTheTitleContainerVisible = true

    lateinit var icon: ImageView
    lateinit var header: ImageView
    lateinit var pager: ViewPager
    lateinit var appBar: AppBarLayout
    lateinit var titleText: TextView
    lateinit var titleContainer: RelativeLayout
    lateinit var userName: TextView
    lateinit var userAcct: TextView
    lateinit var userDescription: TextView
    lateinit var followedText: TextView
    lateinit var followButton: FollowButton
    lateinit var lockIcon: ImageView

    lateinit var account: Account

    companion object {
        const val TARGET_ACCOUNT = "account"
        const val ACCOUNT_ID = "account_id"
        const val ACTION_WITHOUT_ACCOUNT_INFO = "action_without_account_info"
        fun setUserInfo(intent: Intent, account: Account) {
            intent.putExtra(TARGET_ACCOUNT, Gson().toJson(account))
        }

        fun setAccountId(intent: Intent, id: Long) {
            intent.action = UserActivity.ACTION_WITHOUT_ACCOUNT_INFO
            intent.putExtra(UserActivity.ACCOUNT_ID, id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        icon = findViewById(R.id.icon)
        header = findViewById(R.id.header)
        pager = findViewById(R.id.pager)
        appBar = findViewById(R.id.app_bar)
        titleText = findViewById(R.id.title_text)
        titleContainer = findViewById(R.id.title_container)
        userName = findViewById(R.id.user_name)
        userAcct = findViewById(R.id.user_acct)
        userDescription = findViewById(R.id.user_description)
        followButton = findViewById(R.id.follow_button)
        followedText = findViewById(R.id.is_folowee)
        lockIcon = findViewById(R.id.locked_icon)

        followButton.setOnClickListener { _ ->
            if (followButton.isLoading) return@setOnClickListener
            if (followButton.isFollowing) {
                unFollow()
            } else {
                follow()
            }
        }

        header.setColorFilter(ContextCompat.getColor(this, R.color.header_mask), PorterDuff.Mode.SRC_ATOP)
        userDescription.movementMethod = ContentMovementMethod.instance

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayHomeAsUpEnabled(true)
        }

        appBar.addOnOffsetChangedListener(this)

        if (intent.action == ACTION_WITHOUT_ACCOUNT_INFO) {
            Mastodon.api.getAccount(intent.getLongExtra(ACCOUNT_ID, 0)).enqueue(object : Callback<Account> {
                override fun onFailure(call: Call<Account>?, t: Throwable?) {
                }

                override fun onResponse(call: Call<Account>?, response: Response<Account>?) {
                    if(response == null || !response.isSuccessful){
                        Toast.makeText(this@UserActivity, R.string.something_wrong, Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                    account = response.body()!!
                    setupAccountInfo()
                }
            })
        } else {
            account = Gson().fromJson(intent.getStringExtra(TARGET_ACCOUNT), Account::class.java)
            setupAccountInfo()
        }

        AnimUtils.startAlphaAnimation(titleText, 0, View.INVISIBLE)
    }

    private fun setupAccountInfo() {
        titleText.text = HtmlUtil.emojify(titleText, account.displayName, account.emojis)
        userName.text = HtmlUtil.emojify(titleText, account.displayName, account.emojis)
        userAcct.text = "@${account.unicodeAcct}"
        lockIcon.visibility = if (account.locked) View.VISIBLE else View.GONE
        userDescription.text = HtmlUtil.fromHtml(account.note)
        GlideApp.with(this).load(account.headerStatic).placeholder(R.color.colorPrimaryDark).into(header)
        GlideApp.with(this).load(account.avatar).circleCrop().into(icon)
        followButton.isLoading = true
        Mastodon.api.getRelationships(account.id).enqueue(
                object : Callback<List<Relationship>> {
                    override fun onFailure(call: Call<List<Relationship>>?, t: Throwable?) {
                    }

                    override fun onResponse(call: Call<List<Relationship>>?, response: Response<List<Relationship>>?) {
                        if (response == null || !response.isSuccessful) return
                        val relationship = response.body()?.first() ?: return
                        followButton.isLoading = false
                        followButton.isEnabled = true
                        followButton.isFollowing = relationship.following
                        followedText.visibility = if (relationship.followedBy) View.VISIBLE else View.INVISIBLE
                    }
                })
        val adapter = MyFragmentPagerAdapter(supportFragmentManager)
        adapter.add(UserStatusFragment.newInstance(account, getString(R.string.tab_toots)))
        adapter.add(UserStatusFragment.newInstance(account, getString(R.string.tab_media), onlyMedia = true))
        pager.adapter = adapter

        val tab = findViewById<TabLayout>(R.id.tab)
        tab.setupWithViewPager(pager)
        tab.getTabAt(0)!!.text = getString(R.string.tab_toots) + "\n%,d".format(account.statusesCount)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = (Math.abs(offset) / maxScroll).toFloat()
        handleAlphaOnTitle(percentage)
        handleToolbarTitleVisibility(percentage)
    }

    private fun handleAlphaOnTitle(percentage: Float) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (isTheTitleContainerVisible) {
                AnimUtils.startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE)
                isTheTitleContainerVisible = false
            }
        } else {
            if (!isTheTitleContainerVisible) {
                AnimUtils.startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE)
                isTheTitleContainerVisible = true
            }
        }
    }

    private fun handleToolbarTitleVisibility(percentage: Float) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (!isTheTitleVisible) {
                AnimUtils.startAlphaAnimation(titleText, ALPHA_ANIMATIONS_DURATION, View.VISIBLE)
                isTheTitleVisible = true
            }
        } else {
            if (isTheTitleVisible) {
                AnimUtils.startAlphaAnimation(titleText, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE)
                isTheTitleVisible = false
            }
        }
    }

    private fun follow() {
        Mastodon.api.follow(account.id).enqueue(relationshipCallback)
    }

    private fun unFollow() {
        Mastodon.api.unfollow(account.id).enqueue(relationshipCallback)
    }

    private val relationshipCallback = object : Callback<Relationship> {
        override fun onFailure(call: Call<Relationship>?, t: Throwable?) {
        }

        override fun onResponse(call: Call<Relationship>?, response: Response<Relationship>?) {
            if (response == null || !response.isSuccessful) return
            val relationship = response.body() ?: return
            followButton.isFollowing = relationship.following
        }
    }
}
