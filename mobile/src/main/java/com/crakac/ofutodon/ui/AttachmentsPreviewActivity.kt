package com.crakac.ofutodon.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.crakac.ofutodon.R
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.ui.adapter.AttachmentPreviewAdapter
import com.crakac.ofutodon.ui.adapter.UploadedMediaPreviewAdapter
import com.google.gson.Gson

class AttachmentsPreviewActivity : Activity() {
    enum class PreviewAction(val value: String) {
        Preview("preview"),
        Detail("detail")
    }

    lateinit var pager: ViewPager

    var targetStatus: Status? = null

    companion object {
        private val TARGET = "target"
        private val INDEX = "index"

        fun setup(intent: Intent, status: Status, index: Int = 0) {
            intent.action = PreviewAction.Detail.toString()
            intent.putExtra(TARGET, Gson().toJson(status))
            intent.putExtra(INDEX, index)
        }
        fun setup(intent: Intent, uris: ArrayList<Uri>, index: Int = 0){
            intent.action = PreviewAction.Preview.toString()
            intent.putExtra(TARGET, uris)
            intent.putExtra(INDEX, index)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachments_preview)
        pager = findViewById(R.id.pager)
        when (PreviewAction.valueOf(intent.action)) {
            PreviewAction.Preview -> {
                setupWithUris()
            }
            PreviewAction.Detail -> {
                setupWithTargetStatus()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    private fun setupWithTargetStatus() {
        targetStatus = Gson().fromJson(intent.extras.getString(TARGET), Status::class.java)
        val attachments = if(targetStatus!!.reblog != null){
             targetStatus!!.reblog!!.mediaAttachments
        } else {
            targetStatus!!.mediaAttachments
        }
        pager.adapter = AttachmentPreviewAdapter(attachments)
        pager.currentItem = intent.extras.getInt(INDEX, 0)
    }

    private fun setupWithUris(){
        val uris = intent.extras.getParcelableArrayList<Uri>(TARGET) as ArrayList<Uri>
        pager.adapter = UploadedMediaPreviewAdapter(uris)
        pager.currentItem = intent.extras.getInt(INDEX, 0)
    }
}
