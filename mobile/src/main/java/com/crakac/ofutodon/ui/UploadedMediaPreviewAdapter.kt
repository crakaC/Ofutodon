package com.crakac.ofutodon.ui

import android.net.Uri
import android.view.ViewGroup
import com.bumptech.glide.Glide

class UploadedMediaPreviewAdapter(val attachments: ArrayList<Uri>): PreviewAdapter(){
    val TAG: String = "UploadedMediaPreviewAdapter"

    override fun getCount(): Int {
        return attachments.count()
    }

    override fun setupPreview(container: ViewGroup, position: Int) {
        val uri = attachments[position]
        Glide.with(container.context).loadFromMediaStore(uri).listener(dismissProgressOnReady).fitCenter().crossFade().into(photoView)
    }
}