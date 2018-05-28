package com.crakac.ofutodon.api

import com.crakac.ofutodon.db.User

class MastodonApi(delegate: MastodonService, val userAccount: User? = null) : MastodonService by delegate {
    var currentId = 0L
        private set

    init {
        currentId = userAccount?.userId ?: 0L
    }
}