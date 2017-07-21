package com.crakac.ofutodon.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.crakac.ofutodon.R
import com.crakac.ofutodon.transition.FabTransform

class TootActivity : AppCompatActivity() {

    @BindView(R.id.container)
    lateinit var container: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot)
        ButterKnife.bind(this)
        FabTransform.setup(this, container)
    }

    override fun onBackPressed() {
        dismiss()
    }

    @OnClick(R.id.toot_background)
    fun dismiss(){
        finishAfterTransition()
    }
}
