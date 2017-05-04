package com.crakac.ofutodon

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bumptech.glide.Glide
import com.crakac.ofutodon.api.MastodonStreaming
import com.crakac.ofutodon.api.MastodonUtil
import com.crakac.ofutodon.api.Range
import com.crakac.ofutodon.api.entity.Notification
import com.crakac.ofutodon.api.entity.Status
import jp.wasabeef.glide.transformations.CropCircleTransformation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Kosuke on 2017/04/26.
 */
class StatusFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, MastodonStreaming.StreamingCallback {

    @BindView(R.id.listView)
    lateinit var listView: ListView
    @BindView(R.id.swipeRefresh)
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var unbinder: Unbinder
    lateinit var adapter: StatusAdapter

    var nextRange: Range? = null
    var prevRange: Range? = null

    var streaming: MastodonStreaming? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)
        unbinder = ButterKnife.bind(this, view)
        adapter = StatusAdapter(activity)
        listView.adapter = adapter
        swipeRefresh.setOnRefreshListener(this)
        streaming = MastodonStreaming()
        streaming?.callBack = this
        streaming?.connect()
        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
        streaming?.callBack = null
        streaming?.close()
    }

    fun getTitle(): String = "Test!"


    fun addStatuses(statuses: List<Status>) {
        adapter.addTop(statuses)
        adapter.notifyDataSetChanged()
    }

    fun addStatus(status: Status) {
        adapter.addTop(status)
        adapter.notifyDataSetChanged()
    }

    override fun onRefresh() {
        val mastodon = MastodonUtil.api
        if (mastodon == null) {
            swipeRefresh.isRefreshing = false
            return
        }
        mastodon.getHomeTileline().enqueue(
                object : Callback<List<Status>> {
                    override fun onFailure(call: Call<List<Status>>?, t: Throwable?) {
                        swipeRefresh.isRefreshing = false
                    }

                    override fun onResponse(call: Call<List<Status>>?, response: Response<List<Status>>?) {
                        swipeRefresh.isRefreshing = false
                        if (response == null || !response.isSuccessful) {
                            return
                        }
                        addStatuses(response.body())
                    }
                }
        )
    }

    override fun onStatus(status: Status?) {
        status?.let {
            addStatus(status)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onNotification(notification: Notification?) {
    }

    override fun onDelete(id: Long?) {
    }

    class StatusAdapter(val context: Context) : BaseAdapter() {
        val inflater = LayoutInflater.from(context)
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

        fun addTop(status: Status) {
            statusArray.add(0, status)
            notifyDataSetChanged()
        }

        fun addTop(statuses: Collection<Status>) {
            statusArray.addAll(0, statuses)
            notifyDataSetChanged()
        }

        fun addBottom(vararg status: Status) {
            notifyDataSetChanged()
            statusArray.addAll(status)
        }

        class Holder(v: View) {
            @BindView(R.id.displayName)
            lateinit var name: TextView
            @BindView(R.id.status)
            lateinit var content: TextView
            @BindView(R.id.icon)
            lateinit var icon: ImageView

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
                val status = getItem(position)
                it.name.text = status.account?.dispNameWithEmoji
                it.content.text = status.spannedContent
                Glide.with(context)
                        .load(status.account?.avatar)
                        .centerCrop()
                        .crossFade()
                        .bitmapTransform(CropCircleTransformation(context))
                        .into(it.icon)
            }
            return view
        }
    }
}