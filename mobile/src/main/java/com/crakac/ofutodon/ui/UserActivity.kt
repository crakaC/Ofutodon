package com.crakac.ofutodon.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide

import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Account
import com.google.gson.Gson
import jp.wasabeef.glide.transformations.CropCircleTransformation

class UserActivity : AppCompatActivity() {

    @BindView(R.id.icon)
    lateinit var icon: ImageView

    @BindView(R.id.header)
    lateinit var header: ImageView

    @BindView(R.id.pager)
    lateinit var pager: ViewPager

    @BindView(R.id.fab)
    lateinit var fab: FloatingActionButton

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

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        setupUserInfo()
    }

    private fun setupUserInfo(){
        val account = Gson().fromJson(intent.getStringExtra(TARGET_ACCOUNT), Account::class.java)
        title = account.dispNameWithEmoji
        Glide.with(this).load(account.headerStatic).crossFade().centerCrop().into(header)
        Glide.with(this).load(account.avatar).bitmapTransform(CropCircleTransformation(this)).crossFade().into(icon)
        val adapter = MyFragmentPagerAdapter(supportFragmentManager)
        adapter.add(UserStatusFragment.newInstance(account))
        pager.adapter = adapter
    }
}
