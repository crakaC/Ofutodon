package com.crakac.ofutodon.ui

import android.graphics.drawable.Drawable
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crakac.ofutodon.R
import com.github.chrisbanes.photoview.PhotoView

abstract class PreviewAdapter : PagerAdapter() {

    class PreviewLoadListener(val photoView: PhotoView, val progress: View): RequestListener<Drawable> {
        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            photoView.attacher.update()
            progress.visibility = View.GONE
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            return true
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = View.inflate(container.context, R.layout.preview_item, null)
        val photoView: PhotoView = v.findViewById(R.id.image)
        val progress: ProgressBar = v.findViewById(R.id.progress)
        container.addView(v)
        setupPreview(photoView, progress, position)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    abstract fun setupPreview(photoView: PhotoView, progress: ProgressBar, position: Int)
}