package com.crakac.ofutodon.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Html
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.ViewTarget
import com.crakac.ofutodon.R

class GlideImageGetter(val context: Context, val textView: TextView) : Html.ImageGetter, Drawable.Callback {
    var targets: HashSet<ImageGetterViewTarget>

    companion object {
        fun get(view: View): GlideImageGetter? {
            return view.getTag(R.id.drawable_callback_tag) as GlideImageGetter?
        }
    }

    init {
        clear()
        targets = HashSet()
        textView.setTag(R.id.drawable_callback_tag, this)
    }

    fun clear() {
        val prev = get(textView) ?: return
        for (target in prev.targets) {
            Glide.clear(target)
        }
    }

    override fun getDrawable(url: String?): Drawable {
        val drawable = UrlDrawable()
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(ImageGetterViewTarget(textView, drawable, this))
        return drawable
    }

    override fun unscheduleDrawable(who: Drawable?, what: Runnable?) {
    }

    override fun invalidateDrawable(who: Drawable?) {
        textView.invalidate()
    }

    override fun scheduleDrawable(who: Drawable?, what: Runnable?, `when`: Long) {
    }

    fun addTarget(viewTarget: ImageGetterViewTarget) {
        targets.add(viewTarget)
    }

    class ImageGetterViewTarget(val textView: TextView, val drawable: UrlDrawable, val imageGetter: GlideImageGetter) : ViewTarget<TextView, GlideDrawable>(textView) {

        init {
            imageGetter.addTarget(this)
        }

        override fun onResourceReady(resource: GlideDrawable, glideAnimation: GlideAnimation<in GlideDrawable>) {
            val rect = Rect(0, 0, (textView.context.resources.displayMetrics.density * 12f * 2).toInt(), (textView.context.resources.displayMetrics.density * 12f * 2).toInt())
            resource.bounds = rect
            drawable.bounds = rect
            drawable.setDrawable(resource)
            if (resource.isAnimated) {
                drawable.callback = get(getView())
                resource.setLoopCount(GlideDrawable.LOOP_FOREVER)
                resource.start()
            }
            getView().text = getView().text
            getView().invalidate()
        }
    }

    class UrlDrawable : Drawable(), Drawable.Callback {
        var urlDrawable: GlideDrawable? = null

        fun setDrawable(drawable: GlideDrawable) {
            if (urlDrawable != null) {
                urlDrawable?.callback = null
            }
            drawable.callback = this
            urlDrawable = drawable
        }

        override fun draw(canvas: Canvas) {
            urlDrawable?.draw(canvas)
        }

        override fun setAlpha(alpha: Int) {
            urlDrawable?.alpha = alpha
        }

        override fun getOpacity(): Int {
            return urlDrawable?.opacity ?: 0
        }

        override fun setColorFilter(colorFilter: ColorFilter) {
            urlDrawable?.colorFilter = colorFilter
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            callback?.unscheduleDrawable(who, what)
        }

        override fun invalidateDrawable(who: Drawable) {
            callback?.invalidateDrawable(who)
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            callback?.unscheduleDrawable(who, what)
        }
    }
}