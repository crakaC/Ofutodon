package com.crakac.ofutodon.ui

import android.os.Bundle
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Account
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

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
    override fun onRefreshRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getStatuses(accountId, range = prevRange.q)
    }

    override fun onLoadMoreRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getStatuses(accountId, range = nextRange.q)
    }
}