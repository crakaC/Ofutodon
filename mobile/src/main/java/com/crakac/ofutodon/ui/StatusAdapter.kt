package com.crakac.ofutodon.ui

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import jp.wasabeef.glide.transformations.CropCircleTransformation
import java.text.SimpleDateFormat
import java.util.*

class StatusAdapter(val context: Context) : BaseAdapter() {
    val TAG: String = "StatusAdapter"
    val inflater: LayoutInflater = LayoutInflater.from(context)
    val statusArray = ArrayList<Status>()

    override fun getItem(position: Int): Status {
        return statusArray[position]
    }

    override fun getItemId(position: Int): Long {
        return statusArray[position].id
    }

    override fun getCount(): Int {
        return statusArray.size
    }

    fun getPosition(item: Status): Int{
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

    fun removeById(id: Long){
        val target = statusArray.find { it.id == id }
        target?.let {
            statusArray.remove(target)
            notifyDataSetChanged()
        }
    }

    class Holder(v: View) {
        @BindView(R.id.displayName)
        lateinit var name: TextView
        @BindView(R.id.status)
        lateinit var content: TextView
        @BindView(R.id.icon)
        lateinit var icon: ImageView
        @BindView(R.id.createdAt)
        lateinit var createdAt: TextView

        init {
            ButterKnife.bind(this, v)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val holder: Holder?
        var view: View? = convertView

        if (view != null) {
            holder = view.tag as Holder?
        } else {
            view = inflater.inflate(R.layout.status, null)
            holder = Holder(view)
            view.tag = holder
        }

        holder?.let {
            val status = getItem(position).reblog ?: getItem(position)
            it.name.text = status.account?.dispNameWithEmoji
            it.content.text = status.spannedContent
            Glide.with(context)
                    .load(status.account?.avatar)
                    .centerCrop()
                    .crossFade()
                    .bitmapTransform(CropCircleTransformation(context))
                    .into(it.icon)

            it.createdAt.text = parseCreatedAt(status.createdAt)
        }
        return view
    }

    fun parseCreatedAt(source: String): String{
        val format = "yyyy-MM-dd'T'HH:mm:ss.sss";
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val date = sdf.parse(source)

        val simpleFormat = "MM/dd HH:mm:ss"
        return  DateFormat.format(simpleFormat, date).toString()
    }
}
