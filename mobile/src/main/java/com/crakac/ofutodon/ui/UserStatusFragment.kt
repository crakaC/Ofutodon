package com.crakac.ofutodon.ui

import android.os.Bundle
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Account

class UserStatusFragment: StatusFragment() {
    companion object {
        val ACCOUNT_ID = "account_id"
        fun newInstance(account: Account): UserStatusFragment{
            val f = UserStatusFragment()
            val args = Bundle()
            args.putLong(ACCOUNT_ID, account.id)
            f.arguments = args
            return f
        }
    }

    var accountId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountId = arguments.getLong(ACCOUNT_ID)
    }

    override fun getTitle(): String {
        return "トゥート"
    }

    val TAG: String = "UserStatusFragment"
    override fun onRefresh() {
        MastodonUtil.api?.getStatuses(accountId, range = prevRange.q)?.enqueue(onStatus)
    }

    override fun onLoadMore() {
        MastodonUtil.api?.getStatuses(accountId, range = nextRange.q)?.enqueue(onNextStatus)
    }
}