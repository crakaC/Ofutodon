package com.crakac.ofutodon.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick
import com.crakac.ofutodon.R

class TootActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot)
        ButterKnife.bind(this)
    }

    override fun onBackPressed() {
        dismiss()
    }

    @OnClick(R.id.toot_background)
    fun dismiss(){
        finishAfterTransition()
    }
}
