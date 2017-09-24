package com.crakac.ofutodon.ui.widget

import android.content.Context
import android.os.Build
import android.support.v7.graphics.Palette
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Attachment
import com.crakac.ofutodon.util.ViewUtil
import java.lang.Exception

class InlineImagePreview(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val TAG: String = "InlineImagePreview"
    val PREVIEW_MAX_NUM = 4

    var medias: List<Attachment>? = null

    private val images: ArrayList<@JvmSuppressWildcards ImageView> = ArrayList(PREVIEW_MAX_NUM)

    private val leftContainer: LinearLayout
    private val rightContainer: LinearLayout

    val separators: List<@JvmSuppressWildcards View>

    var listener: OnClickPreviewListener? = null

    init {
        View.inflate(context, R.layout.inline_preview, this)
        arrayOf(R.id.image1, R.id.image2, R.id.image3, R.id.image4).forEachIndexed { i, id ->
            val preview = findViewById<ImageView>(id)
            preview.setOnClickListener{ v ->
                onClickPreview(v)
            }
            images.add(preview)
        }
        leftContainer = findViewById(R.id.left)
        rightContainer = findViewById(R.id.right)

        val separatorIds = arrayOf(R.id.separator_vertical, R.id.separator_right, R.id.separator_left)
        separators = ArrayList(separatorIds.size)
        separatorIds.forEach { id ->
            separators.add(findViewById<View>(id))
        }

        val padding = resources.getDimensionPixelSize(R.dimen.spacing_micro)
        setPadding(0, padding, 0, padding)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(widthMeasureSpec) * 9 / 16f + 0.5f).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, height)
    }

    fun setMedia(attachments: List<Attachment>) {
        medias = attachments
        images.forEach { e -> e.visibility = View.GONE }
        attachments.forEachIndexed { index, attachment ->
            val v = images[index]
            Glide.with(context).load(attachment.previewUrl).listener(object : RequestListener<String, GlideDrawable> {
                override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val drawable = resource!!.current as GlideBitmapDrawable
                        Palette.from(drawable.bitmap).generate { palette ->
                            v.foreground = ViewUtil.createRipple(palette, 0.25f, 0.5f, context.getColor(R.color.mid_grey), true)
                        }
                    }
                    return false
                }
            }
            ).centerCrop().crossFade().into(v)

            v.visibility = View.VISIBLE
        }
        rightContainer.visibility = if (attachments.size >= 2) View.VISIBLE else View.GONE
        separators.forEach { e -> e.visibility = View.GONE }
        for(i in 0..Math.min(attachments.size - 2, PREVIEW_MAX_NUM)){
            separators[i].visibility = View.VISIBLE
        }
    }

    fun onClickPreview(v: View){
        listener?.let {
            val index = images.indexOf(v)
            it.onClick(index)
        }
    }

    fun setOnPreviewClickListener(listener: OnClickPreviewListener){
        this.listener = listener
    }

    interface OnClickPreviewListener{
        fun onClick(attachmentIndex: Int){}
    }
}