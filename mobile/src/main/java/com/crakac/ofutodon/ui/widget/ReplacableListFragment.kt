package com.crakac.ofutodon.ui.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Account
import com.crakac.ofutodon.model.api.entity.Tag
import com.crakac.ofutodon.ui.SearchActivity
import com.crakac.ofutodon.ui.UserActivity
import com.crakac.ofutodon.util.GlideApp
import com.crakac.ofutodon.util.HtmlUtil

class ReplacableListFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_simple_list, null)
        recyclerView = v.findViewById(R.id.recyclerView)
        val layoutManager = FastScrollLinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        val divider = DividerItemDecoration(requireContext(), layoutManager.orientation).apply {
            setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        }
        recyclerView.addItemDecoration(divider)
        return v
    }

    fun setAdapter(adapter: ReplacableAdapter<*, *>) {
        recyclerView.adapter = adapter
    }

    abstract class ReplacableAdapter<T, H : RecyclerView.ViewHolder>(context: Context): RecyclerView.Adapter<H>(){
        private val items = ArrayList<T>()
        protected val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        override fun getItemCount(): Int = items.count()
        fun getItem(position: Int): T = items[position]
        fun addBottom(newItems: Collection<T>){
            val oldSize = itemCount
            items.addAll(newItems)
            notifyItemRangeInserted(oldSize, newItems.size)
        }
    }

    class AccountAdapter(context: Context) : ReplacableAdapter<Account, AccountViewHolder>(context) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
            return AccountViewHolder(inflater.inflate(R.layout.list_item_account_searched, null))
        }

        override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
            val account = getItem(position)
            holder.setAccount(account)
            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, UserActivity::class.java)
                UserActivity.setUserInfo(intent, account)
                it.context.startActivity(intent)
            }
        }
    }

    class HashtagAdapter(context: Context) : ReplacableAdapter<Tag, HashtagViewHolder>(context) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagViewHolder {
            return HashtagViewHolder(inflater.inflate(R.layout.list_item_account_searched, null))
        }

        override fun onBindViewHolder(holder: HashtagViewHolder, position: Int) {
            holder.set(getItem(position))
            holder.itemView.setOnClickListener{
                it.context.startActivity(Intent(it.context, SearchActivity::class.java))
            }
        }
    }

    class AccountViewHolder(v: View) : RecyclerView.ViewHolder(v){
        var icon: ImageView = v.findViewById(R.id.icon)
        var displayName: TextView = v.findViewById(R.id.display_name)
        var acct: TextView = v.findViewById(R.id.acct)
        var followButton: ImageView = v.findViewById(R.id.follow_button)

        fun setAccount(account: Account){
            GlideApp.with(itemView.context.applicationContext).load(account.avatar).into(icon)
            displayName.text = HtmlUtil.emojify(displayName, account.displayName, account.emojis)
            acct.text = "@${account.acct}"
        }
    }

    class HashtagViewHolder(v: View) : RecyclerView.ViewHolder(v){
        var tag: TextView = v.findViewById(R.id.tag)
        fun set(tag: Tag){
            this.tag.text = "#${tag.name}"
        }
    }
}