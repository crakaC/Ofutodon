package com.crakac.ofutodon.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.BindViews
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Attachment

class InlineImagePreview(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val TAG: String = "InlineImagePreview"

    var medias: List<Attachment>? = null

    @BindViews(R.id.image1, R.id.image2, R.id.image3, R.id.image4)
    lateinit var images: List<@JvmSuppressWildcards ImageView>

    @BindView(R.id.left)
    lateinit var leftContainer: LinearLayout

    @BindView(R.id.right)
    lateinit var rightContainer: LinearLayout

    @BindViews(R.id.separator_vertical, R.id.separator_right, R.id.separator_left)
    lateinit var separators: List<@JvmSuppressWildcards View>

    init {
        val v = View.inflate(context, R.layout.inline_preview, this)
        ButterKnife.bind(v)
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
            Glide.with(context).load(attachment.previewUrl).centerCrop().crossFade().into(v)
            v.visibility = View.VISIBLE
        }
        rightContainer.visibility = if (attachments.size >= 2) View.VISIBLE else View.GONE
        separators.forEach { e -> e.visibility = View.GONE }
        for(i in 0..(attachments.size - 2)){
            separators[i].visibility = View.VISIBLE
        }
    }
}