package com.crakac.ofutodon.ui

import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.model.api.entity.StatusBuilder
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.util.ViewUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TootActivity : AppCompatActivity() {
    val TAG = "TootActivity"
    @BindView(R.id.container)
    lateinit var container: View

    @BindView(R.id.toot_text)
    lateinit var tootText: EditText

    @BindView(R.id.image_attachments_root)
    lateinit var imageAttachmentParent: LinearLayout

    var isPosting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot)
        ButterKnife.bind(this)
        FabTransform.setup(this, container)
    }

    override fun onBackPressed() {
        dismiss()
    }

    @OnClick(R.id.toot_background)
    fun dismiss() {
        finishAfterTransition()
    }

    @OnClick(R.id.toot)
    fun toot(v: View) {
        if (isPosting) return
        isPosting = true
        MastodonUtil.api?.postStatus(StatusBuilder(text = tootText.text.toString()))?.enqueue(
                object : Callback<Status> {
                    override fun onFailure(call: Call<Status>?, t: Throwable?) {
                        isPosting = false
                        Log.d(TAG, "Failed")
                    }

                    override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                        isPosting = false
                        if (response == null || !response.isSuccessful) {
                            Log.d(TAG, "Failed")
                            return
                        }
                        Log.d(TAG, "Success:" + response.body()?.content)
                        tootText.setText("")
                    }
                }
        )
    }

    @OnClick(R.id.add_photo)
    fun addPhoto() {
        val v = ImageView(this)
        val edge =  resources.getDimension(R.dimen.image_attachment).toInt()
        val p = LinearLayout.LayoutParams(edge, edge)
        v.layoutParams = p

        val pad = resources.getDimension(R.dimen.padding_micro).toInt()

        v.setPadding(pad, pad, pad, pad)
        v.setImageDrawable(getDrawable(R.mipmap.ic_launcher))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val drawable = v.drawable.current as BitmapDrawable
            Palette.from(drawable.bitmap).generate{ palette ->
                v.foreground = ViewUtil.createRipple(palette, 0.25f, 0.5f, getColor(R.color.mid_grey), true)
                v.setOnClickListener { _ ->
                    Log.d("Attachment", "Attachment Clicked")
                }
            }
        }
        imageAttachmentParent.addView(v)
    }

    @OnClick(R.id.toot_visibility)
    fun toggleTootVisibility(){

    }

    @OnClick(R.id.content_warning)
    fun toggleContentWarning(){

    }

    @OnClick(R.id.nsfw)
    fun toggleNotSafeForWark(){

    }
}
