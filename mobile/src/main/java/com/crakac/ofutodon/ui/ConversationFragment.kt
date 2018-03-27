package com.crakac.ofutodon.ui

import android.os.Bundle
import android.view.View
import com.crakac.ofutodon.model.api.entity.ConversationContext
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.ui.adapter.RefreshableAdapter
import com.crakac.ofutodon.ui.adapter.StatusAdapter
import com.google.gson.Gson

class ConversationFragment: MastodonApiFragment<Status, ConversationContext>() {

    companion object {
        val STATUS_ID = "status_id"
        val STATUS = "status"
        fun newInstance(status: Status): ConversationFragment{
            return ConversationFragment().apply {
                arguments = Bundle().apply {
                    putLong(STATUS_ID, status.id)
                    putString(STATUS, Gson().toJson(status))

                }
            }
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

    override fun createAdapter(context: android.content.Context): RefreshableAdapter<Status> =
            StatusAdapter(context, false)

    override fun onRefreshSuccess(response: ConversationContext) {
        adapter.addTop(response.ancestors)
        adapter.addBottom(response.descendants)
    }

    override fun onLoadMoreSuccess(response: ConversationContext) {
        adapter.addBottom(response.descendants)
    }
}