package com.crakac.ofutodon.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.google.gson.Gson

class AttachmentsPreviewActivity : AppCompatActivity() {
    enum class PreviewAction(val value: String) {
        Preview("preview"),
        Detail("detail")
    }

    @BindView(R.id.pager)
    lateinit var pager: ViewPager

    var targetStatus: Status? = null

    companion object {
        private val ACTION_ATTACHMENTS_PREVIEW = "from_preview"
        private val ACTION_STATUS = "from_status"
        private val TARGET_STATUS = "status"
        private val INDEX = "index"

        fun setPreviewInfo(intent: Intent, uris: Array<Uri>) {
            intent.action = PreviewAction.Preview.toString()
        }

        fun setup(intent: Intent, status: Status, index: Int = 0) {
            intent.action = PreviewAction.Detail.toString()
            intent.putExtra(TARGET_STATUS, Gson().toJson(status))
            intent.putExtra(INDEX, index)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachments_preview)
        ButterKnife.bind(this)
        when (PreviewAction.valueOf(intent.action)) {
            PreviewAction.Preview -> {
            }
            PreviewAction.Detail -> {
                setupPreviewWithTargetStatus()
            }
        }
    }

    private fun setupPreviewWithTargetStatus() {
        targetStatus = Gson().fromJson(intent.extras.getString(TARGET_STATUS), Status::class.java)
        val attachments = if(targetStatus!!.reblog != null){
             targetStatus!!.reblog!!.mediaAttachments
        } else {
            targetStatus!!.mediaAttachments
        }
        val adapter = AttachmentPreviewAdapter(attachments)
        val index = intent.extras.getInt(INDEX, 0)
        pager.adapter = adapter
        pager.currentItem = index
    }
}
