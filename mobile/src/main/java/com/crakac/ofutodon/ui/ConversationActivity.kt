package com.crakac.ofutodon.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.google.gson.Gson

class ConversationActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar

    companion object {
        val STATUS = "status"
        fun setStatus(intent: Intent, status: Status) {
            intent.putExtra(STATUS, Gson().toJson(status))
        }

        fun getStatus(intent: Intent): Status {
            return Gson().fromJson(intent.getStringExtra(STATUS), Status::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val f = ConversationFragment.newInstance(getStatus(intent))
        supportFragmentManager.beginTransaction().replace(R.id.fragment, f).commit()
    }
}
