package com.crakac.ofutodon.ui

import android.content.Context
import android.support.v4.content.ContextCompat
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

        holder.icon.setOnClickListener { v ->
            listener?.onIconClicked(v as ImageView, getItem(holder.adapterPosition))
        }

        holder.reply.setOnClickListener { _ ->
            listener?.onReplyClicked(getItem(holder.adapterPosition))
        }

        holder.boost.setOnClickListener { _ ->
            listener?.onBoostClicked(getItem(holder.adapterPosition))
        }
        holder.favorite.setOnClickListener { _ ->
            listener?.onFavoriteClicked(getItem(holder.adapterPosition))
        }

        holder.more.setOnClickListener { _ ->
            val popup = PopupMenu(context, holder.more)
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

        @BindView(R.id.reply)
        lateinit var reply: ImageView

        @BindView(R.id.boost)
        lateinit var boost: ImageView

        @BindView(R.id.unlisted)
        lateinit var unlisted: ImageView

        @BindView(R.id.followers_only)
        lateinit var followersOnly: ImageView

        @BindView(R.id.direct)
        lateinit var direct: ImageView

        @BindView(R.id.favorite)
        lateinit var favorite: ImageView

        @BindView(R.id.more)
        lateinit var more: ImageView

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

            if(status.isReblogged){
                boost.setColorFilter(ContextCompat.getColor(context, R.color.boosted))
            } else {
                boost.clearColorFilter()
            }

            if(status.isFavourited){
                favorite.setColorFilter(ContextCompat.getColor(context, R.color.favourited))
            } else {
                favorite.clearColorFilter()
            }

            when(status.visibility){
                Status.Visibility.Direct.value -> {
                    boost.visibility = View.GONE
                    unlisted.visibility = View.GONE
                    followersOnly.visibility = View.GONE
                    direct.visibility = View.VISIBLE
                }
                Status.Visibility.Private.value -> {
                    boost.visibility = View.GONE
                    unlisted.visibility = View.GONE
                    followersOnly.visibility = View.VISIBLE
                    direct.visibility = View.GONE
                }
                Status.Visibility.UnListed.value ->{
                    boost.visibility = View.VISIBLE
                    unlisted.visibility = View.VISIBLE
                    followersOnly.visibility = View.GONE
                    direct.visibility = View.GONE
                }
                else -> {
                    boost.visibility = View.VISIBLE
                    unlisted.visibility = View.GONE
                    followersOnly.visibility = View.GONE
                    direct.visibility = View.GONE
                }
            }
        }
    }

    interface OnRecyclerItemClickListener {
        fun onItemClicked(status: Status)
        fun onIconClicked(icon: ImageView, status: Status)
        fun onReplyClicked(status: Status)
        fun onBoostClicked(status: Status)
        fun onFavoriteClicked(status: Status)
        fun onMenuClicked(status: Status, menuId: Int)
    }
}
