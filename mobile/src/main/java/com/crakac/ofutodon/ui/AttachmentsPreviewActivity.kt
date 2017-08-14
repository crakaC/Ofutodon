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
        ButterKnife.bind(this)
        when (PreviewAction.valueOf(intent.action)) {
            PreviewAction.Preview -> {
                setupWithUris()
            }
            PreviewAction.Detail -> {
                setupWithTargetStatus()
            }
        }
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
