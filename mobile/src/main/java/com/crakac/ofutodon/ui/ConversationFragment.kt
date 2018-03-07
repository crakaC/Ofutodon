package com.crakac.ofutodon.ui

import android.os.Bundle
import android.view.View
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Context
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.ui.widget.RefreshableAdapter
import com.google.gson.Gson
import retrofit2.Call

class ConversationFragment(): MastodonApiFragment<Status, Context>() {

    companion object {
        val STATUS_ID = "status_id"
        val STATUS = "status"
        fun newInstance(status: Status): ConversationFragment{
            val f = ConversationFragment()
            val args = Bundle()
            args.putLong(STATUS_ID, status.id)
            args.putString(STATUS, Gson().toJson(status))
            f.arguments = args
            return f
        }
    }

    override val TAG: String = "ConversationFragment"

    private var statusId: Long = 0
    private lateinit var status: Status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusId = arguments!!.getLong(STATUS_ID)
        status = Gson().fromJson(arguments!!.getString(STATUS), Status::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.addTop(status)
    }

    override fun createAdapter(context: android.content.Context): RefreshableAdapter<Status> {
        return StatusAdapter(context)
    }

    override fun onRefreshSuccess(response: Context) {
        adapter.addTop(response.ancestors)
        adapter.addBottom(response.descendants)
    }

    override fun onLoadMoreSuccess(response: Context) {
        adapter.addBottom(response.descendants)
    }

    override fun onRefreshRequest(): Call<Context>? {
        return MastodonUtil.api?.getStatusContext(statusId)
    }

    override fun onLoadMoreRequest(): Call<Context>? {
        return MastodonUtil.api?.getStatusContext(statusId)
    }
}