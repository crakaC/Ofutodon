package com.crakac.ofutodon.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.graphics.Palette
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Attachment
import com.crakac.ofutodon.util.GlideApp
import com.crakac.ofutodon.util.ViewUtil

class InlineImagePreview(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private val TAG: String = "InlineImagePreview"
    val PREVIEW_MAX_NUM = 4

    var medias: List<Attachment>? = null

    private val images: ArrayList<@JvmSuppressWildcards ImageView> = ArrayList(PREVIEW_MAX_NUM)

    private val leftContainer: LinearLayout
    private val rightContainer: LinearLayout

    val separators: List<@JvmSuppressWildcards View>
    val hideMediaButton: View
    val mediaMask: View
    val cwText: TextView

    var listener: OnClickPreviewListener? = null

    init {
        View.inflate(context, R.layout.inline_preview, this)
        arrayOf(R.id.image1, R.id.image2, R.id.image3, R.id.image4).forEach { id ->
            val preview = findViewById<ImageView>(id)
            preview.setOnClickListener { v ->
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

        hideMediaButton = findViewById(R.id.hide_image_button)
        cwText = findViewById<TextView>(R.id.spoiler_text)
        mediaMask = findViewById(R.id.nsfw_mask)

        hideMediaButton.setOnClickListener { _ -> mediaMask.visibility = View.VISIBLE }
        mediaMask.setOnClickListener { v -> v.visibility = View.GONE }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val margin = resources.getDimensionPixelSize(R.dimen.spacing_micro)
        val marginParam = layoutParams as MarginLayoutParams
        marginParam.topMargin = margin
        marginParam.bottomMargin = margin
        layoutParams = marginParam
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(widthMeasureSpec) * 9 / 16f + 0.5f).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, height)
    }

    fun setMedia(attachments: List<Attachment>, isSensitive: Boolean) {
        if (isSensitive) {
            mediaMask.visibility = View.VISIBLE
            cwText.setText(R.string.sensitive_media)
        } else {
            mediaMask.visibility = View.GONE
            cwText.setText(R.string.hidden_insensitive_media)
        }

        medias = attachments
        images.forEach { e -> e.visibility = View.GONE }
        attachments.forEachIndexed { index, attachment ->
            val v = images[index]
            GlideApp.with(context).asBitmap().load(attachment.previewUrl).centerCrop().listener(object : RequestListener<Bitmap> {
                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val bitmap = resource ?: return false
                        Palette.from(bitmap).generate { palette ->
                            v.foreground = ViewUtil.createRipple(palette, 0.25f, 0.5f, context.getColor(R.color.mid_grey), true)
                        }
                    }
                    return false
                }

                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean = false
            }).into(v)

            v.visibility = View.VISIBLE
        }
        rightContainer.visibility = if (attachments.size >= 2) View.VISIBLE else View.GONE
        separators.forEach { e -> e.visibility = View.GONE }
        for (i in 0..Math.min(attachments.size - 2, PREVIEW_MAX_NUM)) {
            separators[i].visibility = View.VISIBLE
        }
    }

    fun onClickPreview(v: View) {
        listener?.run {
            val index = images.indexOf(v)
            onClick(index)
        }
    }

    fun setOnPreviewClickListener(listener: OnClickPreviewListener) {
        this.listener = listener
    }

    interface OnClickPreviewListener {
        fun onClick(attachmentIndex: Int) {}
    }
}