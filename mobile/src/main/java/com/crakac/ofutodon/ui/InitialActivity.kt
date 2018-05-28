package com.crakac.ofutodon.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.MastodonUtil

class InitialActivity: AppCompatActivity() {
    val TAG: String = "InitialActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MastodonUtil.existsCurrentAccount { account ->
            if (account == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Mastodon.initialize(account)
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finish()
        }
    }
}