package com.crakac.ofutodon.ui.adapter

import android.util.Log
import android.widget.ProgressBar
import com.crakac.ofutodon.model.api.entity.Attachment
import com.crakac.ofutodon.util.GlideApp
import com.github.chrisbanes.photoview.PhotoView

class AttachmentPreviewAdapter(val attachments: List<Attachment>) : PreviewAdapter(){
    val TAG: String = "AttachmentPreview"

    override fun getCount(): Int {
        return attachments.count()
    }

    override fun setupPreview(photoView: PhotoView, progress: ProgressBar, position: Int) {
        val attachment = attachments[position]
        val url = if (attachment.url.isNotEmpty()) attachment.url else attachment.remoteUrl
        GlideApp.with(photoView.context).load(url).listener(PreviewLoadListener(photoView, progress)).into(photoView)
        Log.d(TAG, "load:$url")
    }
}