package com.crakac.ofutodon.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crakac.ofutodon.R
import com.crakac.ofutodon.db.AppDatabase
import com.crakac.ofutodon.db.User
import com.crakac.ofutodon.util.GlideApp

class UserAccountAdapter(val context: Context) : BaseAdapter() {
    private val users = ArrayList<User>()
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        AppDatabase.execute {
            users.addAll(AppDatabase.instance.userDao().getAll())
            AppDatabase.uiThread {
                notifyDataSetChanged()
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = convertView ?: inflater.inflate(R.layout.list_item_user_account, null)
        val icon = v.findViewById<ImageView>(R.id.avatar_icon)
        val userName = v.findViewById<TextView>(R.id.user_name)
        val user = getItem(position)
        userName.text = context.getString(R.string.full_user_name).format(user.name, user.domain)
        GlideApp.with(v.context).load(user.avator).apply(RequestOptions().transform(RoundedCorners(4))).into(icon)
        v.setOnClickListener{
            onClickUserListener?.invoke(user)
        }
        return v
    }

    override fun getItem(position: Int) = users[position]
    override fun getItemId(position: Int) = users[position].id.toLong()
    override fun getCount() = users.size

    var onClickUserListener: ((user: User) -> Unit)? = null

}