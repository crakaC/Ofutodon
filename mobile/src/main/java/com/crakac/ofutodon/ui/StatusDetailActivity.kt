package com.crakac.ofutodon.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.google.gson.Gson

class StatusDetailActivity : AppCompatActivity() {

    companion object {
        val STATUS = "status"
        fun setStatus(intent: Intent, status: Status){
            intent.putExtra(STATUS, Gson().toJson(status))
        }
    }

    private lateinit var targetStatus: Status
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_detail)
        targetStatus = Gson().fromJson(intent.extras.getString(STATUS), Status::class.java)
    }
}
