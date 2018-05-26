package com.crakac.ofutodon.ui.adapter

import android.support.v7.widget.RecyclerView
import com.crakac.ofutodon.model.api.entity.Identifiable
import java.util.TreeSet
import kotlin.collections.ArrayList

abstract class RefreshableAdapter<T : Identifiable> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items = ArrayList<T>()
    private val ids = TreeSet<Long>()

    open fun getItem(position: Int): T = items[position]

    override fun getItemId(position: Int): Long = getItem(position).id

    open fun getPosition(item: T): Int = items.indexOf(item)

    /**
     * return -1 if not found
     */
    open fun getPositionById(id: Long): Int = items.indexOfFirst { e -> e.id == id }

    fun getItemById(id: Long): T? = items.firstOrNull { e -> e.id == id }

    fun contains(id: Long): Boolean = ids.contains(id)

    open fun addTop(item: T) {
        items.add(0, item)
        ids.add(item.id)
        notifyItemInserted(0)
    }

    open fun addTop(newItems: Collection<T>) {
        this.items.addAll(0, newItems)
        ids.addAll(newItems.map { e -> e.id })
        notifyItemRangeInserted(0, newItems.size)
    }

    open fun addBottom(newItems: Collection<T>) {
        val oldSize = itemCount
        this.items.addAll(newItems)
        ids.addAll(newItems.map { e -> e.id })
        notifyItemRangeInserted(oldSize, newItems.size)
    }

    open fun remove(position: Int): T {
        return items.removeAt(position).apply {
            notifyItemRemoved(position)
        }
    }

    fun removeById(id: Long) {
        getItemById(id)?.let { item ->
            val pos = getPosition(item)
            remove(pos)
            ids.remove(id)
        }
    }

    fun replace(position: Int, item: T) {
        items[position] = item
        notifyItemChanged(position)
    }

    fun update(item: T) {
        val position = getPositionById(item.id)
        if (position < 0) return
        replace(position, item)
    }

    val isEmpty: Boolean
        get() = items.isEmpty()


    override fun getItemCount(): Int = items.size

    interface Refreshable {
        fun refresh() {}
    }
}