package com.crakac.ofutodon.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.util.TextUtil
import jp.wasabeef.glide.transformations.CropCircleTransformation
import java.util.*

class StatusAdapter(val context: Context) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {
    val TAG: String = "StatusAdapter"
    val statusArray = ArrayList<Status>()

    var listener: OnRecyclerItemClickListener? = null

    fun getItem(position: Int): Status {
        return statusArray[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    fun getPosition(item: Status): Int {
        return statusArray.indexOf(item)
    }

    fun addTop(status: Status) {
        statusArray.add(0, status)
        notifyDataSetChanged()
    }

    fun addTop(statuses: Collection<Status>) {
        statusArray.addAll(0, statuses)
        notifyDataSetChanged()
    }

    fun addBottom(statuses: Collection<Status>) {
        statusArray.addAll(statuses)
        notifyDataSetChanged()
    }

    fun removeById(id: Long) {
        val target = statusArray.find { it.id == id }
        target?.let {
            statusArray.remove(target)
            notifyDataSetChanged()
        }
    }

    val isEmpty: Boolean
        get() {
            return statusArray.isEmpty()
        }

    override fun onBindViewHolder(holder: StatusViewHolder?, position: Int) {
        val item = getItem(position)
        holder?.setData(context, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): StatusViewHolder {
        val holder = StatusViewHolder(View.inflate(context, R.layout.status, null))
        holder.itemView.setOnClickListener { _ ->
            val status = getItem(holder.adapterPosition)
            listener?.onItemClicked(status)
        }
        holder.option.setOnClickListener { _ ->
            val popup = PopupMenu(context, holder.option)
            popup.inflate(R.menu.home)
            popup.setOnMenuItemClickListener { item ->
                val status = getItem(holder.adapterPosition)
                val menuItemId = item.itemId
                listener?.onMenuClicked(status, menuItemId)
                Log.d(TAG, "menu item clicked!")
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }

        return holder
    }

    override fun getItemCount(): Int {
        return statusArray.size
    }

    override fun onViewRecycled(holder: StatusViewHolder?) {
        if(holder is StatusViewHolder){
            holder.icon.setImageBitmap(null)
        }
    }

    class StatusViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        @BindView(R.id.displayName)
        lateinit var name: TextView
        @BindView(R.id.status)
        lateinit var content: TextView
        @BindView(R.id.icon)
        lateinit var icon: ImageView
        @BindView(R.id.createdAt)
        lateinit var createdAt: TextView
        @BindView(R.id.option)
        lateinit var option: TextView

        init {
            ButterKnife.bind(this, v)
        }

        fun setData(context: Context, status: Status) {
            name.text = status.account?.dispNameWithEmoji
            content.text = status.spannedContent
            Glide.with(context)
                    .load(status.account?.avatar)
                    .centerCrop()
                    .crossFade()
                    .bitmapTransform(CropCircleTransformation(context))
                    .into(icon)

            createdAt.text = TextUtil.parseCreatedAt(status.createdAt)
        }
    }

    interface OnRecyclerItemClickListener {
        fun onItemClicked(status: Status)
        fun onMenuClicked(status: Status, menuId: Int)
    }
}
