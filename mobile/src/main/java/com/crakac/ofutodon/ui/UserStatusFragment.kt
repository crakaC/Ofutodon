package com.crakac.ofutodon.ui

import android.os.Bundle
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.entity.Account
import com.crakac.ofutodon.api.entity.Status
import retrofit2.Call

class UserStatusFragment : TimelineFragment() {
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
        accountId = arguments?.getLong(ACCOUNT_ID) ?: 0
        isOnlyMedia = arguments?.getBoolean(ONLY_MEDIA) ?: false
        titleName = arguments?.getString(TITLE) ?: ""
    }

    override fun getTitle() = titleName

    override val TAG: String = "UserStatusFragment"
    override fun onRefreshRequest(): Call<List<Status>> {
        return if(isOnlyMedia){
            Mastodon.api.getStatuses(accountId ,onlyMedia = true, range = prev)
        } else {
            Mastodon.api.getStatuses(accountId, range = prev)
        }
    }

    override fun onLoadMoreRequest(): Call<List<Status>> {
        return if(isOnlyMedia){
            Mastodon.api.getStatuses(accountId, onlyMedia = true, range = next)
        } else {
            Mastodon.api.getStatuses(accountId, range = next)
        }
    }
}