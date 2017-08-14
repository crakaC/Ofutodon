package com.crakac.ofutodon.ui

import android.util.Log
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Attachment

class AttachmentPreviewAdapter(val attachments: List<Attachment>) : PreviewAdapter(){
    val TAG: String = "AttachmentPreview"

    override fun getCount(): Int {
        return attachments.count()
    }

    override fun setupPreview(container: ViewGroup, position: Int) {
        val attachment = attachments[position]
        val url = if (attachment.url.isNotEmpty()) attachment.url else attachment.remoteUrl
        Glide.with(container.context).load(url).listener(dismissProgressOnReady).fitCenter().crossFade().into(photoView)
        Log.d(TAG, "load:$url")
    }
}