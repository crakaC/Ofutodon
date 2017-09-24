package com.crakac.ofutodon.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Account
import com.crakac.ofutodon.util.AnimUtils
import com.google.gson.Gson
import jp.wasabeef.glide.transformations.CropCircleTransformation

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
    lateinit var titleContainer: LinearLayout
    lateinit var userName: TextView
    lateinit var userDescription: TextView

    lateinit var account: Account

    companion object {
        val TARGET_ACCOUNT = "account"
        fun setUserInfo(intent: Intent, account: Account) {
            intent.putExtra(TARGET_ACCOUNT, Gson().toJson(account))
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
        userDescription = findViewById(R.id.user_description)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayHomeAsUpEnabled(true)
        }

        appBar.addOnOffsetChangedListener(this)

        account = Gson().fromJson(intent.getStringExtra(TARGET_ACCOUNT), Account::class.java)
        setupAccountInfo()

        val adapter = MyFragmentPagerAdapter(supportFragmentManager)
        adapter.add(UserStatusFragment.newInstance(account, getString(R.string.tab_toots)))
        adapter.add(UserStatusFragment.newInstance(account, getString(R.string.tab_media), onlyMedia = true))
        pager.adapter = adapter

        val tab = findViewById<TabLayout>(R.id.tab)
        tab.setupWithViewPager(pager)

        AnimUtils.startAlphaAnimation(titleText, 0, View.INVISIBLE)
    }

    private fun setupAccountInfo() {
        titleText.text = account.dispNameWithEmoji
        userName.text = account.dispNameWithEmoji
        userDescription.text = account.noteWithEmoji
        Glide.with(this).load(account.headerStatic).placeholder(R.drawable.placeholder).centerCrop().crossFade().into(header)
        Glide.with(this).load(account.avatar).bitmapTransform(CropCircleTransformation(this)).crossFade().into(icon)
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
}
