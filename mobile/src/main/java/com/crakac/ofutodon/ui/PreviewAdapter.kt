package com.crakac.ofutodon.ui

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crakac.ofutodon.R
import com.github.chrisbanes.photoview.PhotoView
import java.lang.Exception

abstract class PreviewAdapter: PagerAdapter() {
    lateinit var photoView: PhotoView

    lateinit var progress: ProgressBar

    val dismissProgressOnReady = object : RequestListener<Any?, GlideDrawable> {
        override fun onResourceReady(resource: GlideDrawable?, model: Any?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            progress.visibility = View.GONE
            return false
        }

        override fun onException(e: Exception?, model: Any?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
            return true
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = View.inflate(container.context, R.layout.preview_item, null)
        container.addView(v)
        photoView = v.findViewById(R.id.image)
        progress = v.findViewById(R.id.progress)
        setupPreview(container, position)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    abstract fun setupPreview(container: ViewGroup, position: Int)
}