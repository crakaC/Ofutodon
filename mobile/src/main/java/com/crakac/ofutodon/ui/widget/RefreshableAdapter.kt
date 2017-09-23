package com.crakac.ofutodon.ui.widget

import android.content.Context
import android.support.v7.widget.RecyclerView

abstract class RefreshableAdapter<T>(val context: Context) : RecyclerView.Adapter<RefreshableViewHolder>() {
    protected val items = ArrayList<T>()

    open fun getItem(position: Int): T {
        return items[position]
    }

    open fun getPosition(item: T): Int {
        return items.indexOf(item)
    }

    open fun addTop(item: T) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    open fun addTop(newItems: Collection<T>) {
        this.items.addAll(0, newItems)
        notifyItemRangeInserted(0, newItems.size)
    }

    open fun addBottom(newItems: Collection<T>) {
        val oldSize = itemCount
        this.items.addAll(newItems)
        notifyItemRangeInserted(oldSize, newItems.size)
    }

    open fun remove(position: Int): T {
        return items.removeAt(position).apply {
            notifyItemRemoved(position)
        }
    }

    fun replace(position: Int, item: T) {
        items[position] = item
        notifyItemChanged(position)
    }

    val isEmpty: Boolean
        get() {
            return items.isEmpty()
        }


    override fun getItemCount(): Int {
        return items.size
    }
}