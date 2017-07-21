package com.crakac.ofutodon.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.model.api.entity.StatusBuilder
import com.crakac.ofutodon.transition.FabTransform
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TootActivity : AppCompatActivity() {
    val TAG = "TootActivity"
    @BindView(R.id.container)
    lateinit var container: View

    @BindView(R.id.toot_text)
    lateinit var tootText: EditText

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

}
