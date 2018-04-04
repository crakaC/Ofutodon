package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.ui.adapter.RefreshableAdapter
import com.crakac.ofutodon.ui.adapter.StatusAdapter

/**
 * Created by Kosuke on 2017/04/26.
 */
abstract class StatusFragment<T> : MastodonApiFragment<Status, T>() {
    override fun createAdapter(): RefreshableAdapter<Status> {
        val adapter = StatusAdapter(requireActivity())
        adapter.statusListener = StatusClickListener(requireActivity())
        return adapter
    }
}