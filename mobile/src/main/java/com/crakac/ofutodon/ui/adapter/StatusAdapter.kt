package com.crakac.ofutodon.ui.adapter

import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.ui.widget.InlineImagePreview
import com.crakac.ofutodon.ui.widget.OnClickStatusListener
import com.crakac.ofutodon.ui.widget.StatusViewHolder


class StatusAdapter(val showBottomLoading: Boolean = true) : RefreshableAdapter<Status>(), StatusChangeListener{
    companion object {
        private val statusChangeListeners = ArrayList<StatusChangeListener>()
        fun register(adapter: StatusChangeListener){
            statusChangeListeners.add(adapter)
        }
        fun unregister(adapter: StatusChangeListener){
            statusChangeListeners.remove(adapter)
        }
        fun update(status: Status){
            for(listener in statusChangeListeners){
                listener.onUpdate(status)
            }
        }
    }

    val TAG: String = "StatusAdapter"
    val footer = Status(-1)

    var statusListener: OnClickStatusListener? = null

    override fun getItem(position: Int): Status {
        if (isFooter(position)) {
            return footer
        }
        return super.getItem(position)
    }

    override fun getItemCount(): Int {
        return when {
            isEmpty -> 0
            showBottomLoading -> super.getItemCount() + 1
            else -> super.getItemCount()
        }
    }

    private fun isFooter(position: Int): Boolean {
        return showBottomLoading && position >= itemCount - 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isFooter(position)) return
        if (holder is StatusViewHolder) {
            val status = getItem(position)
            holder.setStatus(status)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HolderType.Footer.rawValue) {
            return FooterViewHolder(View.inflate(parent.context, R.layout.footer, null))
        }
        return StatusViewHolder(View.inflate(parent.context, R.layout.status, null)).apply {
            itemView.setOnClickListener { statusListener?.onItemClicked(getItem(adapterPosition)) }
            icon.setOnClickListener { v ->
                statusListener?.onIconClicked(v as ImageView, getItem(adapterPosition))
            }
            reply.setOnClickListener { _ ->
                statusListener?.onReplyClicked(reply, getItem(adapterPosition))
            }
            boost.setOnClickListener { _ ->
                statusListener?.onBoostClicked(boost, getItem(adapterPosition))
            }
            favorite.setOnClickListener { _ ->
                statusListener?.onFavoriteClicked(favorite, getItem(adapterPosition))
            }
            more.setOnClickListener { _ ->
                val popup = PopupMenu(context!!, more)
                popup.inflate(R.menu.status_popup)
                popup.setOnMenuItemClickListener { item ->
                    val menuItemId = item.itemId
                    statusListener?.onMenuClicked(getItem(adapterPosition), menuItemId)
                    return@setOnMenuItemClickListener true
                }
                popup.show()
            }
            preview.setOnPreviewClickListener(object : InlineImagePreview.OnClickPreviewListener {
                override fun onClick(attachmentIndex: Int) {
                    statusListener?.onClickAttachment(getItem(adapterPosition), attachmentIndex)
                }
            })
            readMore.setOnClickListener { _ ->
                val status = getItem(adapterPosition)
                val st = if (status.reblog != null) status.reblog!! else status
                st.hasExpanded = !st.hasExpanded
                toggleReadMore(st)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItemId(position) < 0) {
            HolderType.Footer.rawValue
        } else {
            HolderType.Status.rawValue
        }
    }

    private enum class HolderType(val rawValue: Int) {
        Status(3),
        Footer(4)
    }

    override fun onUpdate(status: Status) {
        update(status)
    }

    class FooterViewHolder(v: View) : RecyclerView.ViewHolder(v), Refreshable
}