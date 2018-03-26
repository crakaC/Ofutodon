package com.crakac.ofutodon.ui

import android.net.Uri
import android.widget.ProgressBar
import com.crakac.ofutodon.util.GlideApp
import com.github.chrisbanes.photoview.PhotoView

class UploadedMediaPreviewAdapter(val attachments: ArrayList<Uri>): PreviewAdapter(){
    val TAG: String = "UploadedMediaPreviewAdapter"

    override fun getCount(): Int {
        return attachments.count()
    }

    override fun setupPreview(photoView: PhotoView, progress: ProgressBar, position: Int) {
        val uri = attachments[position]
        GlideApp.with(photoView.context).load(uri).fitCenter().into(photoView)
    }
}