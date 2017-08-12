package com.crakac.ofutodon.ui

import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Attachment
import com.github.chrisbanes.photoview.PhotoView
import java.lang.Exception

class AttachmentPreviewAdapter(val attachments: List<Attachment>) : PagerAdapter() {
    val TAG: String = "AttachmentPreview"

    @BindView(R.id.image)
    lateinit var photoView: PhotoView

    @BindView(R.id.progress)
    lateinit var progress: ProgressBar

    override fun getCount(): Int {
        return attachments.count()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = View.inflate(container.context, R.layout.preview_item, null)
        container.addView(v)
        ButterKnife.bind(this, v)
        val attachment = attachments[position]
        val url = if (attachment.url.isNotEmpty()) attachment.url else attachment.remoteUrl
        Glide.with(container.context).load(url).listener(object : RequestListener<String, GlideDrawable> {
            override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                progress.visibility = View.GONE
                return false
            }

            override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                return true
            }
        }).fitCenter().crossFade().into(photoView)
        Log.d(TAG, "load:$url")
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}