package com.crakac.ofutodon.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Account
import com.crakac.ofutodon.util.AnimUtils
import com.google.gson.Gson
import jp.wasabeef.glide.transformations.CropCircleTransformation

class UserActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    private val PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f
    private val PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f
    private val ALPHA_ANIMATIONS_DURATION = 200L


    private var isTheTitleVisible = false
    private var isTheTitleContainerVisible = true

    @BindView(R.id.icon)
    lateinit var icon: ImageView

    @BindView(R.id.header)
    lateinit var header: ImageView

    @BindView(R.id.pager)
    lateinit var pager: ViewPager

    @BindView(R.id.fab)
    lateinit var fab: FloatingActionButton

    @BindView(R.id.app_bar)
    lateinit var appBar: AppBarLayout

    @BindView(R.id.title_text)
    lateinit var titleText: TextView

    @BindView(R.id.title_container)
    lateinit var titleContainer: LinearLayout

    @BindView(R.id.user_name)
    lateinit var userName: TextView

    @BindView(R.id.user_description)
    lateinit var userDescription: TextView

    companion object {
        val TARGET_ACCOUNT = "account"
        fun setUserInfo(intent: Intent, account: Account){
            intent.putExtra(TARGET_ACCOUNT, Gson().toJson(account))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        ButterKnife.bind(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        appBar.addOnOffsetChangedListener(this)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        setupUserInfo()
        AnimUtils.startAlphaAnimation(titleText, 0, View.INVISIBLE)
    }

    private fun setupUserInfo(){
        val account = Gson().fromJson(intent.getStringExtra(TARGET_ACCOUNT), Account::class.java)
        titleText.text = account.dispNameWithEmoji
        userName.text = account.dispNameWithEmoji
        userDescription.text = account.noteWithEmoji
        Glide.with(this).load(account.headerStatic).crossFade().centerCrop().into(header)
        Glide.with(this).load(account.avatar).bitmapTransform(CropCircleTransformation(this)).crossFade().into(icon)
        val adapter = MyFragmentPagerAdapter(supportFragmentManager)
        adapter.add(UserStatusFragment.newInstance(account))
        pager.adapter = adapter
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = (Math.abs(offset) / maxScroll).toFloat()
        handleAlphaOnTitle(percentage)
        handleToolbarTitleVisibility(percentage)
    }

    private fun handleAlphaOnTitle(percentage: Float){
        if(percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS){
            if(isTheTitleContainerVisible){
                AnimUtils.startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE )
                isTheTitleContainerVisible = false
            }
        } else {
            if(!isTheTitleContainerVisible){
                AnimUtils.startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE)
                isTheTitleContainerVisible = true
            }
        }

    }

    private fun handleToolbarTitleVisibility(percentage: Float){
        if(percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR){
            if(!isTheTitleVisible){
                AnimUtils.startAlphaAnimation(titleText, ALPHA_ANIMATIONS_DURATION, View.VISIBLE )
                isTheTitleVisible = true
            }
        } else {
            if(isTheTitleVisible){
                AnimUtils.startAlphaAnimation(titleText, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE)
                isTheTitleVisible = false
            }
        }

    }
}
