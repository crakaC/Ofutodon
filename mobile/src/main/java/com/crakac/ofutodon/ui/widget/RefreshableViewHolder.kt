package com.crakac.ofutodon.ui.widget

import android.support.v7.widget.RecyclerView
import android.view.View

open class RefreshableViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    open fun refresh() {}
}
