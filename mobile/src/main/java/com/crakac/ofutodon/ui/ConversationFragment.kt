package com.crakac.ofutodon.ui

import android.os.Bundle
import android.view.View
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.entity.ConversationContext
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.ui.adapter.RefreshableAdapter
import com.crakac.ofutodon.ui.adapter.StatusAdapter
import com.google.gson.Gson
import retrofit2.Call

class ConversationFragment : MastodonApiFragment<Status, ConversationContext>() {

    companion object {
        val STATUS_ID = "status_id"
        val STATUS = "status"
        fun newInstance(status: Status): ConversationFragment {
            return ConversationFragment().apply {
                arguments = Bundle().apply {
                    putLong(STATUS_ID, status.id)
                    putString(STATUS, Gson().toJson(status))

                }
            }
        }
    }
    override var isSwipeRefreshEnabled  = false
    override var isLoadMoreEnabled = false

    private var statusId: Long = 0
    private lateinit var status: Status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusId = arguments!!.getLong(STATUS_ID)
        status = Gson().fromJson(arguments!!.getString(STATUS), Status::class.java)
    }

    override fun createAdapter(): RefreshableAdapter<Status> {
        val adapter = StatusAdapter(false)
        StatusAdapter.register(adapter)
        adapter.statusListener = StatusClickListener(requireActivity())
        return adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.addTop(status)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        StatusAdapter.unregister(adapter as StatusAdapter)
        (adapter as StatusAdapter).statusListener = null
    }

    override fun onRefreshRequest(): Call<ConversationContext> = Mastodon.api.getStatusContext(statusId)

    override fun onRefreshSuccess(response: ConversationContext) {
        adapter.addTop(response.ancestors)
        adapter.addBottom(response.descendants)
    }

    override fun onLoadMoreSuccess(response: ConversationContext) {
        adapter.addBottom(response.descendants)
    }

}