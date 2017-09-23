package com.crakac.ofutodon.ui

import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

class ConversationFragment(): StatusFragment() {
    override val TAG: String = "ConversationFragment"



    override fun onRefreshRequest(): Call<List<Status>>? {
        return null
    }

    override fun onLoadMoreRequest(): Call<List<Status>>? {
        return null
    }

    override fun getTitle(): String = getString(R.string.conversation)
}