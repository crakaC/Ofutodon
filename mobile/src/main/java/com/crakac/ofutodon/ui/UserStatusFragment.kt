package com.crakac.ofutodon.ui

import android.os.Bundle
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Account
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

class UserStatusFragment : StatusFragment() {
    companion object {
        val ACCOUNT_ID = "account_id"
        val ONLY_MEDIA = "only_media"
        val TITLE = "title"
        fun newInstance(account: Account, title: String, onlyMedia: Boolean = false): UserStatusFragment {
            val f = UserStatusFragment()
            val args = Bundle()
            args.putLong(ACCOUNT_ID, account.id)
            args.putBoolean(ONLY_MEDIA, onlyMedia)
            args.putString(TITLE, title)
            f.titleName = title
            f.arguments = args
            return f
        }
    }

    private var accountId: Long = 0
    private var isOnlyMedia: Boolean = false
    private var titleName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountId = arguments.getLong(ACCOUNT_ID)
        isOnlyMedia = arguments.getBoolean(ONLY_MEDIA)
        titleName = arguments.getString(TITLE)
    }

    override fun getTitle() = titleName

    val TAG: String = "UserStatusFragment"
    override fun onRefreshRequest(): Call<List<Status>>? {
        return if(isOnlyMedia){
            MastodonUtil.api?.getStatuses(accountId ,onlyMedia = true, range = prevRange.q)
        } else {
            MastodonUtil.api?.getStatuses(accountId, range = prevRange.q)
        }
    }

    override fun onLoadMoreRequest(): Call<List<Status>>? {
        if (isLoadingNext || nextRange.maxId == null)
            return null

        return if(isOnlyMedia){
            MastodonUtil.api?.getStatuses(accountId, onlyMedia = true, range = nextRange.q)
        } else {
            MastodonUtil.api?.getStatuses(accountId, range = nextRange.q)
        }
    }
}