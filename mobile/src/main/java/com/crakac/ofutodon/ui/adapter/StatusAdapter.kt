package com.crakac.ofutodon.ui.adapter

import android.content.Context
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


class StatusAdapter(context: Context, val showBottomLoading: Boolean = true) : RefreshableAdapter<Status>(context) {
    val TAG: String = "StatusAdapter"
    val dummy = Status(-1)

    var statusListener: OnClickStatusListener? = null

    override fun getItem(position: Int): Status {
        if (isFooter(position)) {
            return dummy
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
        if(isFooter(position)) return
        val item = getItem(position)
        (holder as StatusViewHolder).setData(context, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HolderType.Footer.rawValue) {
            return FooterViewHolder(View.inflate(context, R.layout.dummy_status, null))
        }
        val holder = StatusViewHolder(View.inflate(context, R.layout.status, null))
        holder.itemView.setOnClickListener { _ ->
            val status = getItem(holder.adapterPosition)
            statusListener?.onItemClicked(status)
        }

        holder.icon.setOnClickListener { v ->
            statusListener?.onIconClicked(v as ImageView, getItem(holder.adapterPosition))
        }

        holder.reply.setOnClickListener { _ ->
            statusListener?.onReplyClicked(holder.reply, getItem(holder.adapterPosition))
        }

        holder.boost.setOnClickListener { _ ->
            statusListener?.onBoostClicked(holder.boost, getItem(holder.adapterPosition))
        }

        holder.favorite.setOnClickListener { _ ->
            statusListener?.onFavoriteClicked(holder.favorite, getItem(holder.adapterPosition))
        }

        holder.more.setOnClickListener { _ ->
            val status = getItem(holder.adapterPosition)
            val popup = PopupMenu(context, holder.more)
            popup.inflate(R.menu.status_popup)
            popup.setOnMenuItemClickListener { item ->
                val menuItemId = item.itemId
                statusListener?.onMenuClicked(status, menuItemId)
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }

        holder.preview.setOnPreviewClickListener(object : InlineImagePreview.OnClickPreviewListener {
            override fun onClick(attachmentIndex: Int) {
                statusListener?.onClickAttachment(getItem(holder.adapterPosition), attachmentIndex)
            }
        })

        holder.readMore.setOnClickListener { _ ->
            val status = getItem(holder.adapterPosition)
            val st = if(status.reblog != null) status.reblog!! else status
            st.hasExpanded = !st.hasExpanded
            holder.toggleReadMore(context, st)
        }
        return holder
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is StatusViewHolder) {
            holder.icon.setImageBitmap(null)
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
    class FooterViewHolder(v: View) : RecyclerView.ViewHolder(v)
}