package com.crakac.ofutodon.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crakac.ofutodon.model.api.MastodonUtil

class InitialActivity: AppCompatActivity() {
    val TAG: String = "InitialActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MastodonUtil.existsCurrentAccount { account ->
            if (account == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                MastodonUtil.initialize(account)
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finish()
        }
    }
}